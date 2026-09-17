# education.md — framing notes (Rust's Borrow Checker Finally Makes Sense)

## Why this unit exists

Most developers meet the borrow checker the wrong way: they write code that
looks safe, Rust throws an error, and it feels like the compiler is fighting
them. The Byte Without Bite video (5:15) makes the opposite case: the borrow
checker is *proving* two things before the program ever runs — **no reference
can outlive the data it points to**, and **memory cannot be mutated while
another part of the program is relying on it**. Once that clicks, the errors
stop feeling random. This unit exists to give learners that click, then give
them the language (lifetimes, NLL, interior mutability) that the rest of the
Rust course leaves implicit.

## Pedagogical spine (from OLD.txt: a lesson plan is a system)

- **Understand → analyze → challenge** across the six sessions: reframe (S1) →
  rule (S2) → concrete trap (S3) → lifetimes (S4) → NLL + diagnostic (S5) →
  escape hatches (S6).
- **EES**: every session ends in a reproducible artifact — a classified error,
  a compiling signature, a fix chosen from the menu — verifiable by running
  `rustc`.
- **The "exclusive" throughline** (from the video): a mutable reference means
  *temporary exclusive access*. The word *exclusive* explains most borrow
  errors, so it recurs in every session.
- **Verification-first**: absolutely nothing ships unless it compiles. Every
  example and solution in this unit was compiled and run against `rustc`
  1.91.1 (`cargo` 1.91.1), the same toolchain pinned by the root Rust course.

## What NOT to teach first

- Do **not** start with lifetime annotations. The video's discipline, and this
  unit's, is: when the borrow checker complains, *don't reach for annotations
  first* — ask who owns it, who has permission, and how long the permission
  needs to last. Annotations come only in Session 4 and only to *describe a
  relationship the compiler can't infer*.
- Do **not** present `RefCell`/`Mutex`/`Rc`/`Arc` as "circumventing the borrow
  checker". They are *controls made explicit*: each one moves the guarantee
  (runtime check, lock, ownership count) into the type so the failure is
  visible at compile time or well-documented at runtime.
- Don't skip the vector trap (Session 3) on the grounds it's "one example" —
  it is the concrete reason the many-readers/one-writer rule exists (aliasing
  + mutation is dangerous when `push` can relocate the buffer).

## Verification log (what actually compiled, rustc 1.91.1)

- `fn dangle() -> &String` → **E0106** (missing lifetime specifier) — cannot
  even be written; exactly as the video says ("you can't even write the
  signature without the compiler stopping you").
- `let first = &v[0]; v.push(4); println!("{}", first);` → **E0502** (cannot
  borrow `v` as mutable because it is also borrowed as immutable) — the held
  reference forces the error even though the real hazard is reallocation.
- Two `&mut x` at once → **E0499** (cannot borrow `x` as mutable more than once
  at a time).
- Use after move (`println!("{}", s1)` post `let _s2 = s1;`) → **E0382**.
- Non-lexical lifetimes: `let first = &v[0]; println!("{}", first); v.push(4);`
  compiles cleanly — the borrow ends at its last use, not at the closing
  brace.
- `fn longest<'a>(x: &'a str, y: &'a str) -> &'a str` compiles; so does a
  signature that ties the output only to the first input.
- `RefCell` (borrow-mut/push) and `Rc` (strong count / deref) examples compile
  and run with expected output.

## Known issues to acknowledge, not hide

- On newer toolchains the exact suggestion text in `E0106` may read
  "consider using the `'static` lifetime, but this is uncommon unless…" — that
  hint is *not* the fix for this unit's purposes; the real lesson is the
  relationship the annotation must describe.
- `NLL` makes many snippets compile that older Rust (pre-1.31) rejected. Keep
  the pinned `--edition 2021` when the class tests by hand, or reintroduce the
  confusion.
- The four interior-mutability wrappers are behaviorally overlapping class
  material. Strict classroom discipline: name the *enforcement point* for each
  one (compile time vs runtime check vs lock vs reference count) before
  choosing it.

## Grading posture

Reproduce > diagnose > build. Sessions 1–3 *reproduce* the video's reframes;
the discrimination is in Session 5's fix-menu (annotation only when truly
needed) and Session 6's wrapper choice (which rule moves where).

## Running the unit

```bash
# verify any example in the unit (edition pinned to match the root course)
rustc --edition 2021 file.rs -o file.bin && ./file.bin

# the root Rust course's lessons provide the prior-knowledge anchors:
ls ../lessons/lesson-08-ownership ../lessons/lesson-09-borrowing
```

## Extension ideas

- Take the `longest` signature exercise and show why the return type *cannot*
  be tied to a literal or a temporary (the compiler rejecting `let x; x = &5;`
  is a lovely follow-on probe).
- After Session 6, grep a small real project for `RefCell`/`Mutex`/`Arc`:
  name the enforcement point each site is using.
- Convert a Session 5 fix into a `#[cfg(test)]` unit test that proves the fixed
  code compiles and behaves — knitting this unit into the root course's
  test-first habits.