# Lesson plans — Rust's Borrow Checker Finally Makes Sense

_Companion to `OLD.txt` ("a lesson plan is a system, not a prompt"). The
source lecture is the Byte Without Bite video "Rust's Borrow Checker Finally
Makes Sense" (5:15). This is the "reframe the borrow checker" unit: it turns
the compiler error from an argument into a proof, so learners stop asking
"how do I make this compile?" and start asking "what safety guarantee can the
compiler not prove yet?" Each session names the system inputs (goal, learners,
prior knowledge, evidence) and then the sequence + activities. The examples in
`exercises.md`/`solutions.md` are verified against `rustc` 1.91.1.*

## Unit learning goal

> Learners can explain what the borrow checker *proves* (no reference outlives
> its data; memory is not mutated while another part of the program relies on
> it) and can diagnose an error by asking the three expert questions — who owns
> this data, who currently has permission to access it, and how long that
> permission needs to last — instead of pasting in lifetime annotations.

## Learner profile

Rust learners who have already met the borrow checker the "wrong way": they
wrote code that *looks* safe, got an error, and concluded the compiler was
fighting them. They know the rules as slogans ("many readers or one writer")
but cannot predict which code will compile. Level: post-basics (they have seen
ownership and borrowing in Lessons 8–9 of the root Rust course). They may be
juniors or experienced developers from C/C++/Python, so the pain point is
shared but the confidence differs.

## Prior knowledge

- Ownership rules: one owner, drop at scope end, moves invalidate the old
  owner (root `lessons/lesson-08-ownership`).
- References `&T` / `&mut T` and slicing (root `lessons/lesson-09-borrowing`).
- Writing and reading `rustc`/`cargo` output — `E0308`, `E0382`, `E0502`,
  `E0499` will appear.
- *Not* assumed: lifetime annotations, `'a`, non-lexical lifetimes, `RefCell`,
  `Mutex`, `Rc`, `Arc` — all introduced in this unit.

---

## Session 1 — Why the compiler "argues" (level: understand)

- **Goal**: the borrow checker tries to prove two things before the program
  ever runs — (1) no reference can outlive the data it points to, and (2)
  memory cannot be mutated while another part of the program is relying on it.
- **Evidence (EES)**: learner quotes both guarantees and maps each classic
  error to the guarantee it enforces; learner paraphrases "once that clicks,
  the borrow checker stops feeling random".
- **Prior knowledge activated**: the feeling of "the compiler is arguing with
  me"; basic `rustc` error reading.
- **Sequence**: the hook (a week of Rust → the error that feels like an
  argument) → what the checker proves → the reframe (it's proving, not
  fighting) → classify real error messages.
- **Activities**: pick 2–3 `rustc` errors from the repo's `lessons/` and
  label which guarantee (outlive / mutate-while-relied-upon) each one enforces.
- **Accessibility**: error-map handout (error code → which guarantee); the
  "argument vs proof" analogy gives a verbal anchor.

## Session 2 — Many readers OR one writer, never both (level: analyze)

- **Goal**: state and apply the core rule — Rust allows either many shared
  references or one mutable reference, but not both at the same time.
- **Evidence (EES)**: for a given snippet, learner says which combination is
  present and predicts whether the aliasing-plus-mutation hazard applies.
- **Prior knowledge activated**: `&T` = "I want to read this value"; `&mut T` =
  "I need temporary *exclusive* access" — the word *exclusive* explains most
  borrow-checker errors.
- **Sequence**: shared = read, mutable = exclusive → why aliasing + mutation
  is dangerous → the rule → "could one reference read or write memory the
  other assumes is stable?" as the diagnostic question.
- **Activities**: classify snippets as (many readers), (one writer), or
  (reader + writer); rewrite the (reader + writer) cases.
- **Accessibility**: table of the three allowed/forbidden shapes with a
  check/cross column; visual "two arms vs one hand" diagram.

## Session 3 — The vector trap: why `push` breaks `&v[0]` (level: analyze)

- **Goal**: explain the concrete failure mode behind the rule — a `Vec`
  reallocates on growth, so a held reference to `v[0]` would point at memory
  that no longer belongs to anything.
- **Evidence (EES)**: learner traces `let first = &v[0]; v.push(..)` and
  names the moment the reference becomes invalid; then fixes it by NLL or by
  shortening the borrow's last use.
- **Prior knowledge activated**: `Vec` push/grow semantics; slice/reference
  validity (`lessons/lesson-13-collections`).
- **Sequence**: the trap (borrow element, push, use) → why push is the danger
  (heap reallocation) → the error `E0502` → fixes (don't hold across the push;
  push first, then borrow).
- **Activities**: reproduce the trap; predict before running; apply the two
  fixes and verify both compile.
- **Accessibility**: step-by-step memory diagram showing the heap copy during
  growth.

## Session 4 — Lifetimes: relationships, not lifespans (level: analyze/challenge)

- **Goal**: understand that a lifetime isn't about keeping data alive — it
  describes how long a reference is valid *relative to other values*; and that
  `'a` annotations just spell out that relationship.
- **Evidence (EES)**: learner explains why `fn dangle() -> &String` cannot
  even be written (the returned reference would outlive the local it points
  at), and writes a two-input/one-output signature that compiles by tying the
  output's lifetime to an input.
- **Prior knowledge activated**: dangling references are prevented
  (`lessons/lesson-09-borrowing` §5); basic function signatures.
- **Sequence**: "a lifetime isn't a lifespan" → the dangling local ✓ → why the
  signature won't compile → `fn longest<'a>(x: &'a str, y: &'a str) -> &'a str`
  → "you're describing the relationship so the compiler can prove the returned
  reference can't outlive the data behind it".
- **Activities**: predict which of several annotated signatures compile;
  annotate the classic `longest`.
- **Accessibility**: side-by-side "data lives / reference is valid" timeline
  diagram; the annotation read aloud in plain English.

## Session 5 — Non-lexical lifetimes & the three questions (level: challenge)

- **Goal**: know that a borrow ends after its *last use*, not at the closing
  brace (non-lexical lifetimes, NLL), and adopt the three diagnosing questions
  experts ask: who owns this data? who has permission? how long does the
  permission need to last?
- **Evidence (EES)**: learner takes a snippet that fails, applies the
  three-question diagnosis, and fixes it — with the answer "very often the fix
  isn't an annotation at all; it's shortening a borrow, changing ownership,
  restructuring a function, or borrowing a smaller piece of a value."
- **Prior knowledge activated**: everything from Sessions 1–4.
- **Sequence**: the NLL trick (print through `&v`, stop using it, mutate later
  in the same block — no error) → the three questions as a diagnostic → the
  fix-menu (shorten / change ownership / restructure / borrow a smaller piece)
  → when annotations actually are the answer.
- **Activities**: diagnose-and-fix clinic on 3–4 failing snippets; rank fixes
  by "is an annotation needed?".
- **Accessibility**: decision flowchart (three questions → fix menu); worked
  example written as a transcript-style inner monologue.

## Session 6 — When shared mutation is genuinely needed (level: challenge)

- **Goal**: know the four escapes and *why each one makes the control
  explicit*: `RefCell<T>` (checks at runtime), `Mutex<T>` (lock across
  threads), `Rc<T>` (multiple ownership, single-threaded), `Arc<T>` (shared
  thread-safe ownership). Rust isn't banning shared mutable state — it's
  demanding the control be visible in the type, not buried in a bug report six
  months from now.
- **Evidence (EES)**: learner maps a scenario to the right wrapper and states
  exactly which rule is moved from compile time to runtime (or to a lock).
- **Prior knowledge activated**: interior mutability conceptually; threads at a
  high level only.
- **Sequence**: "sometimes you genuinely need shared mutation" → the four tools
  one by one, each with the promise it makes and the cost it pays → the
  philosophy clause → the closing reframe (from "make this compile" to "what
  can't the compiler prove yet?").
- **Activities**: matching game (scenario → wrapper); predict what `RefCell`
  pays vs `Mutex`.
- **Accessibility**: four-row comparison table (tool, thread-safe?, enforcement
  point, price).

---

## Assessment evidence (unit-wide)

| what | where | pass bar |
|---|---|---|
| Quote the two guarantees | Session 1 activity | both guarantees named; each classic error mapped |
| Apply many-readers/one-writer | Session 2 classify | all three shapes classified correctly |
| Explain the vector trap | Session 3 trace | pinpoints the reallocated reference; both fixes compile |
| Sign a `longest`-style fn | Session 4 | `fn longest<'a>` style compiles; `dangle` rejected |
| Diagnose-and-fix | Session 5 clinic | fix chosen from the menu, annotation only if needed |
| Choose the right wrapper | Session 6 mapping | wrapper matches scenario + thread-safety + enforcement point |

## Teacher decisions baked in

- All examples compile with the repo's pinned `rustc` 1.91.1 (`rustc --edition
  2021`); solutions in `solutions.md` were verified by actually compiling —
  do not weaken this step for the class.
- The classroom should *run* the failing snippets too (the error messages,
  especially `E0502` and `E0106`, are part of the teaching material).
- Sequence matters: Sessions 1–3 are the reframe; 4–6 give the language
  (lifetimes, NLL) and the escape hatches. Do not jump to annotations before
  Session 4.
- The "exclusive" word from the video is the throughline — reuse it in every
  session.