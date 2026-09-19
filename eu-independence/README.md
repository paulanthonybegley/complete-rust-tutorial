# European Independence — the SOTEU 2026 Explorer Course

An educational course built from the YouTube video **"Von der Leyen's New Plan
for European Independence Explained"** (TLDR News EU,
<https://www.youtube.com/watch?v=ec_4Un6JrAY>, ~9 min), which analyses
Ursula von der Leyen's 2026 State of the European Union speech.

Like its sister course [`../eighth-api-rules`](../eighth-api-rules), it turns a
video into **lesson resources plus a runnable Spring Boot application** — but
this time the app is an **interactive learning lab built with Spring Boot 4,
Thymeleaf and HTMX** (server-rendered pages, htmx partials, in-browser
simulators, self-grading quiz and flip cards).

> Tagline from the source framework (`OLD.jpg` → `OLD.txt`):
> **"A lesson plan is a system, not a prompt."**

## The features raised in the video

The video walks through the speech's recurring themes and concrete proposals.
Every one of them is a lesson in this course and a section of the app:

| # | Theme | What the video raises |
|---|-------|------------------------|
| 1 | The speech itself | 6th annual State of the Union; "strongest but precarious"; decoupling from Russia, China and the US |
| 2 | Economic resilience | Speed agenda: faster permitting, single market overhaul by 2027, banking fragmentation (only 16% of corporate lending crosses borders), omnibus simplification (€17bn/yr), "pact against gold-plating", Savings & Investments Union, the "28th regime" / EU Inc |
| 3 | Critical raw materials | China supplies ~90% of EU imports (rare earths); joint procurement + stockpile programme via a European body |
| 4 | Defence & security | €1.5bn Defence Industry Programme, €325m joint projects, €90bn joint debt to Ukraine support, NATO Article 4 equivalent (Emergency Security Protocol), counter-hybrid playbook, European Security Council, Instrument for Strategic Enablers, defence spending up ~80% in 5 years |
| 5 | Energy | Double electricity's share of energy use by 2040; cut fossil import bill by ~€260bn/yr; more grid connections; 80 GW renewables added, ~6× that waiting for a grid connection |
| 6 | Climate | Europe warming 2× faster than the world; climate risk insurance alliance, heatwave plans, drought strategies, firefighting fleet, water initiative (1 in 4 litres lost) |
| 7 | AI & tech sovereignty | More European compute, finance domestic scale-ups, frontier-model safety cooperation, industrial AI in 5 sectors (health, transport, agri-food, advanced manufacturing, defence & space) |
| 8 | Partnerships & trade | Canada as first "associate member"; trade deals India/Mercosur, NZ & Australia next; Middle Corridor via Global Gateway (€12bn); €1bn/day China trade deficit |
| 9 | Media & sources | 157 sources reporting on the speech; left/right framing differences; bias awareness in reporting |

## Repository layout

```
eu-independence/
├── OLD.txt          # OCR text extracted from OLD.jpg (the lesson-planning framework)
├── lesson_plans.md  # 9 lesson plans + integration project built on the OLD framework
├── exercises.md     # per-lesson exercises + integration rubric
├── solutions.md     # sample answers (verified against the running app)
├── education.md     # education log: how this course was built
├── linkedin-post.md # shareable social post
└── app/             # Spring Boot 4 + Thymeleaf + HTMX interactive lab
    └── src/main/
        ├── java/com/example/euind/
        │   ├── model/        # Policy, TimelineEvent, QuizQuestion, Flashcard, ...
        │   ├── service/       # PolicyService, TimelineService, QuizService, SimService, ...
        │   └── web/           # HomeController, PolicyController, SimController, ...
        └── resources/
            ├── templates/     # Thymeleaf pages + fragments
            └── static/        # style.css + vendored htmx
```

## Run the app

Requirements: JDK 17+ (tested on 24), Maven 3.9+.

```bash
cd app
mvn spring-boot:run        # starts on http://localhost:8080
mvn test                   # tests verify every page and partial renders
```

Open <http://localhost:8080> in a browser. No database is needed — all content
is in-memory so the focus stays on the teaching material.

> Port note: if something else already occupies 8080,
> `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8089`.

### What's inside the app

- **Dashboard** (`/`) — one-stat-per-theme scorecard with a live
  "independence meter".
- **Policy explorer** (`/policies`) — every feature from the video as cards,
  filterable by theme, opened with htmx partials (`/policies/{id}`).
- **Independence lab** (`/lab`) — three interactive simulators:
  - *Energy to 2040* — drag the electricity-share target, see the fossil
    import bill slash and grid catch-up needed.
  - *Critical raw materials* — adjust China dependency, watch the stockpile
    clock and exposure grade.
  - *Security Council builder* — check permanent + rotating members and see
    the legitimacy/coverage score react.
- **Timeline** (`/timeline`) — last year's progress → the speech → what's next,
  filterable by theme.
- **Quiz** (`/quiz`) — self-grading questions with instant htmx feedback.
- **Flashcards** (`/flashcards`) — flip cards, cycle by deck.
- **Partners** (`/partners`) — associate members, trade deals and observers.
- **Sources** (`/sources`) — the media-literacy feature: how different
  outlets frame the same speech.

## Learning order

1. Watch the video (<https://www.youtube.com/watch?v=ec_4Un6JrAY>).
2. Read `lesson_plans.md` — 9 lessons, each built with the 9 design inputs
   from `OLD.txt`.
3. Do the matching exercises in `exercises.md` while driving the app.
4. Check your work against `solutions.md`.
5. Finish with the integration project (rubric included in `exercises.md`).