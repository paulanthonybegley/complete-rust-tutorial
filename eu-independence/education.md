# Education Log — European Independence Course

> Documentation of every step taken to build this course, written for "future
> me" (and anyone else) to learn from and reuse.

## 1. Project Overview

**Goal:** Turn the YouTube video *"Von der Leyen's New Plan for European
Independence Explained"* (TLDR News EU,
<https://www.youtube.com/watch?v=ec_4Un6JrAY>) into structured, runnable
educational resources — lesson plans, exercises, solutions **and an
interactive Spring Boot 4 learning app built with Thymeleaf + HTMX** — using
the "lesson plan as a system" framework from `OLD.jpg`.

This is the **second iteration** of the pipeline first used for
[`../eighth-api-rules`](../eighth-api-rules) (a REST-API course). The major
difference: the previous app was a JSON REST API; this one is a
**server-rendered, htmx-driven interactive web application** because the topic
(EU policy) benefits from browsing, simulating and quizzing rather than curl.

**Tools used:**
- `tesseract` OCR (already installed) — the framework image → `OLD.txt`
- `youtube-transcript.ai` — full ~4,558-word transcript in one fetch
- `websearch` — corroborating press coverage (Reuters, Euronews, Politico)
- **JDK 24 + Maven 3.9** — build and test
- **Spring Boot 4.1.1** + **Thymeleaf** + **HTMX 2.0.7** (vendored locally)

**Deliverables:**

| File / Folder | Description |
|---|---|
| `OLD.txt` | OCR extraction of `OLD.jpg` (9-input lesson-planning framework) |
| `README.md` | Course overview, feature list, run instructions |
| `lesson_plans.md` | 9 lessons + integration project on the 9-input framework |
| `exercises.md` | Per-lesson exercises + scorecard rubric |
| `solutions.md` | Verified sample answers |
| `education.md` | This document |
| `linkedin-post.md` | Shareable social post |
| `app/` | Spring Boot 4 + Thymeleaf + HTMX interactive lab |

## 2. The Source Material

Two inputs were combined:

1. **`OLD.jpg`** — the "lesson planning as a system" framework (model cannot
   read images; OCR again). The 9 design inputs: Learning Goal, Lesson
   Sequence, Assessment Evidence, Learner Profile, Prior Knowledge, Learning
   Activities, Output Requirements, Accessibility & Supports, Teacher
   Decisions. Tagline: *"A lesson plan is a system, not a prompt."*

2. **YouTube video** (8:42) — analysis of the 2026 State of the European
   Union speech. The **features raised**, extracted mostly from the transcript
   and cross-checked with Reuters/Euronews/Politico coverage:
   1. SOTEU framing — "strongest it has ever been / precarious"; decoupling
      from Russia, China and the US.
   2. Economy — speed agenda, permitting, single market by 2027, 16%
      cross-border lending, omnibus simplification (€17bn/yr), gold-plating,
      Savings & Investments Union, 28th regime.
   3. Critical raw materials — ~90% China dependency; joint procurement +
      stockpiles via a European body.
   4. Defence — €1.5bn programme, €90bn joint debt, Article 4 equivalent
      (Emergency Security Protocol), counter-hybrid playbook, European
      Security Council, Strategic Enablers, +80% defence spending.
   5. Energy — double electricity share by 2040, €260bn/yr import cut, grid
      connections, 6× renewables waiting for grid.
   6. Climate — 2× faster warming, insurance alliance, heatwave/drought plans,
      firefighting fleet, water losses.
   7. AI — more compute, scale-up finance, model-safety cooperation, 5
      industrial sectors.
   8. Partnerships — Canada associate member, India/Mercosur deals, Middle
      Corridor (€12bn), €1bn/day China deficit.
   9. Media — 157 sources, left/right framing (from the sponsor segment).

## 3. Step 0 — OCR the Image

Identical to the sister course:

```bash
tesseract OLD.jpg OLD.txt        # -> writes OLD.txt.txt, then rename
```

The text `OLD.txt` at the repo root is the canonical extraction; this course
copies it locally so each course folder is self-contained.

## 4. Step 1 — Analyse the Video

Repeated the exact playbook that worked before:

1. `webfetch` on the YouTube URL → returns almost nothing (JS-rendered page).
2. `webfetch https://youtube-transcript.ai/transcript/ec_4Un6JrAY.txt` → the
   full 4,558-word transcript in one shot. This is the backbone for the
   feature list.
3. `websearch` for the video + title → finding TLDR News EU's own page plus
   Reuters, Euronews, Politico, intellinews and watcherpost coverage of the
   16 Sept 2026 speech. These corroborate and extend the transcript's numbers
   (€12bn Middle Corridor, 100 vulnerable territories, 25% insured losses,
   SAFE €150bn, Freyja missiles, 80GW/6× grid figures).

> Lessons: the transcript-first approach scales to any news-analysis video;
> press coverage is essential for the numbers the video's narrator states
> second-hand. Always cross-check; never invent stats.

## 5. Step 2 — Spring Boot 4 + Thymeleaf + HTMX Research

| Question | Finding |
|---|---|
| Existing stable Boot version for this repo? | **4.1.1** (matches `eighth-api-rules`) — keeps one platform across the suite |
| Thymeleaf starter available for Boot 4.1.1? | Yes — `spring-boot-starter-thymeleaf` 4.1.1 on Maven Central (confirmed via repo listing) |
| htmx delivery? | Vendored to `static/js/htmx.min.js` (v2.0.7, 51 KB) so the lab runs offline/behind a school network — no CDN dependency |
| Test annotations in Boot 4 | Same as sister course: `@AutoConfigureMockMvc` now lives in `org.springframework.boot.webmvc.test.autoconfigure` via `spring-boot-starter-webmvc-test` |
| htmx partial strategy | Services/data live server-side; each page renders a `th:fragment`; the same fragment is returned alone for `HX-Request` calls, so one template serves both full page and partial |

## 6. Step 3 — Design the Application

**Concept:** an **"Independence Lab"** — a dashboard, a policy explorer,
three simulators, a timeline, a quiz, flashcards, a partner explorer and a
sources page. Data is in-memory (records populated by services) so there is no
database and the teaching stays focused.

**Mapping features → screens:**

| Feature | Screen |
|---|---|
| Speech thesis + one stat per theme | `/` dashboard + independence meter |
| All 9 theme groups as cards, filterable, drill-down | `/policies` + `/policies/{id}` htmx partial |
| Energy target / import bill | `/lab` energy simulator (slider → htmx GET recompute) |
| China dependency / stockpile | `/lab` materials simulator |
| Security Council membership | `/lab` council builder (checkbox → score) |
| Year-on-year progress | `/timeline` with theme filter |
| Self-assessment | `/quiz` (htmx answer reveal) + `/flashcards` |
| Partnerships & trade | `/partners` with groups |
| Media literacy | `/sources` (left/right frame cards) |

**Architecture:** pure server-side MVC. Controllers return full views; htmx
endpoints return fragments (checked via `HX-Request` header or dedicated
sub-URLs). A tiny CSS file in `static/css/style.css` provides the EU-blue/gold
theme; no frontend build step.

Sim models are deliberately small and transparent:
- *Energy:* saving = `260 × progress` (€bn/yr) where progress is how far the
  dragged 2040 share has moved from today's 22% baseline towards the 44%
  target — maxing at the video's quoted €260bn/yr.
- *Materials:* exposure grade (MANAGED → CRITICAL) + "stockpile days" derive
  from a single-supplier concentration slider so learners feel the leverage
  mechanic.
- *Council:* score = population coverage of 447M EU + point bonuses for East
  (+10), North (+5), small states (+5 each, cap 10) and external partners
  (+5 each, cap 10), so toggling members visibly changes the result.

## 7. Step 4 — Verify the Code

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 24) mvn -B -f eu-independence/app/pom.xml test
```

Result: **21 tests, 0 failures**: context loads; every full page returns 200
and contains its headline string; each htmx partial returns the fragment; the
quiz scoring endpoint marks correct/incorrect and explains wrong answers; the
sim endpoints compute expected values for known inputs (energy 44% → €260bn,
materials 90% → CRITICAL / 80 days, council ro+se → +15 bonus); the flashcard
deck and spotlight render; static htmx/JS and CSS are served.

**Live smoke test:** packaged the jar, booted on port 8091 and curled every
page plus one htmx partial over HTTP. Verified the quiz grid renders all 12
cards, the flashcard deck renders 20 + 1 spotlight, the `/policies` explorer
renders all 32 catalog cards, and a filtered `/flashcards/grid?cat=MEDIA`
partial returns the "157 *outlets*" card.

**Bug fought (worth remembering):** Thymeleaf renders the *bodies of every
`th:fragment` definition* when a template is rendered as a whole page — so a
template that both defined and inserted its own fragments output them twice,
and a fragment whose body referenced context variables blew up during a normal
render when those variables were absent. Fix: keep all fragments in one
separate `_fragments.html` (never served directly), reference them as
`~{_fragments :: name}`, and guard any fragment that must work with optional
model data. Cost: ~2 hours of test-driven debugging; the tests caught the
duplication (card counts) and the null-safety (template parse errors).

## 8. Step 5 — Author the Teaching Material

`lesson_plans.md` applies the 9 inputs from `OLD.txt` to 9 lessons covering
every feature raised in the video plus an integration project (Independence
Scorecard). `exercises.md` requires driving the app (simulators, quiz,
partners); `solutions.md` anchors every answer to transcript/press facts.

## 9. Quick Reference — Commands

```bash
# OCR (once, at repo root — this course copies the result)
tesseract OLD.jpg OLD.txt && mv OLD.txt.txt OLD.txt 2>/dev/null

# Build & test
JAVA_HOME=$(/usr/libexec/java_home -v 24) mvn -B -f eu-independence/app/pom.xml test

# Run
cd eu-independence/app && mvn spring-boot:run          # http://localhost:8080
# or on a custom port:
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8089

# From the repo root via the Makefile (idempotent, port 8091):
make start-euind   # boot the lab, logs in .run/eu-independence.log
make stop-euind
```

## 10. Lessons Learned

1. **The pipeline is now a formula.** transcript → feature list → facts →
   app → lesson docs. This course replayed it end-to-end with zero surprises.
2. **Two app styles, one pattern.** The REST course taught via curl; this one
   teaches via browsing + simulation. Same repo layout, same docs, different
   front-end — the framework (a lesson plan is a system) survives both.
3. **htmx is a teaching superpower.** In-page recompute (simulators, quiz
   feedback, flashcards) makes content *feel* alive without any frontend
   toolchain — perfect for classrooms with flaky networks (everything is
   vendored).
4. **Stats first, prose second.** Nearly every lesson anchors on a number
   (90%, 16%, €260bn, 80GW, 2×, 25%, 1-in-4, €1bn/day, 157). Concrete numbers
   are what make policy teaching stick.
5. **Bundle media literacy.** The video's sponsor segment is itself a lesson
   (left vs right framing of the same announcement) — including it turned the
   most "advert-like" part into one of the best classroom discussions.
6. **Hunt for duplicate output, not just errors.** The passing tests hid that
   fragment bodies rendered twice on full pages until a raw card count exposed
   it. String searches won't catch dupes; count DOM nodes in tests.
7. **Fragments belong in their own file.** An htmx partial served solo, and
   the same partial inserted server-side on first load, is a great pattern —
   but only if the fragment definitions live somewhere that never renders as a
   page. `_fragments.html` is now the house style.

## 11. Where to Go Next

- [ ] Add a "speech search" — full transcript loaded into the app with
  client-side filtering per theme.
- [ ] Turn the Independence Scorecard (integration project) into a real
  in-app form with a saved comparisons page.
- [ ] Add a 10th lesson on EU institutions (why the Commission proposes, the
  Parliament/ Council decide) using the "who decides" row of the scorecard.
- [ ] Version the app content: expose the same data as JSON (REST) so the
  sister course's 8 laws can be taught against this dataset.
- [ ] Add per-lesson printable handouts generated server-side (Pdf via
  Templates?).