# Education Document · Code Smells

### A build record: what was made, why it was made that way, and how it was made real

**Project:** `code-smells` — an educational Spring Boot 4 application accompanying
*The Serious CTO*'s fifteen-part video series *Code Smells: Identifying and Refactoring
Troubled Code*.

**Audience for this document:** the learner who will use the app, the instructor who
will teach from it, the maintainer who will extend it, and — because a build record is
also a teaching artifact — anyone who wants to see how a video series gets turned into a
runnable curriculum.

**How to read this document.** Part 1 states the brief and inventories the deliverable.
Part 2 explains the design decisions and the reasoning behind them. Part 3 walks the
machinery, file by file, so the app is comprehensible to someone who has never seen the
code. Part 4 is the heart of the document: fifteen chapters, one per lab, each explaining
the smell, the tell, the refactoring, what the metrics actually do, and how to teach it.
Part 5 and Part 6 are the honest parts — the verification pass, the four defects that
verification caught, and the limits of what was built. Part 7 maps the work back onto the
nine-input lesson-design framework it was commissioned against. Part 8 closes with
engineering decisions, limitations, and where a maintainer should take it next.

A note on length and honesty. This is a long document because the work was long, and
because a course deserves documentation proportional to its teaching surface. Where a
number appears, it is a number that was measured, not estimated. Where a claim was made
and then found to be false by a test, both the claim and the correction are recorded,
because a course that hides its own corrections is teaching the wrong lesson.

---

## Part 1 · The Brief and the Deliverable

### 1.1 What was asked

The commission had four parts, and they are worth separating because each constrains the
others in a different way.

1. **Analyse a specific video series.** Fifteen videos, one per code smell, by a
   practitioner whose framing is deliberately opinionated.
2. **Build an application that illustrates each feature presented.** Not a blog post, not
   a slide deck: a running Spring Boot 4 application with Thymeleaf and HTMX.
3. **Containerise dependent services with Docker.** So the thing runs identically on a
   laptop and on a machine that has never met the author.
4. **Produce complete lesson plans, exercises, and solutions** following a nine-input
   framework supplied as `OLD.txt`, in which a lesson plan is treated as *a system, not a
   prompt*.

Part 2 of that list is where the real design work lives. "Illustrate each feature" is
easy to satisfy badly. The obvious bad version is a page per video containing a paragraph
of prose and a code block, which is a worse reading experience than the video itself and
teaches nothing a learner could not get by skimming captions. The good version is an
application in which the learner *acts* — reads code, forms a hypothesis, commits to an
answer, and gets evidence. That distinction drove essentially every subsequent decision
in this build.

### 1.2 The series, mapped to fifteen labs

The playlist is a chronological tour of a well-known catalogue. The catalogue itself is
part of the shared professional vocabulary of software development: the refactoring
vocabulary — *Extract Method*, *Extract Class*, *Move Method*, *Inline Class*, *Replace
Conditional with Polymorphism*, *YAGNI* — is standard material taught in most professional
courses and reproduced in reference works, not the intellectual property of any one
teacher. What the series contributes is a particular voice: it argues these smells with
conviction, uses vivid framing (a god class as a convenience object that grew until it
owned the company; a hierarchy as a museum of ritual levels), and pairs each smell with a
concrete cure.

The application preserves the author's framing and ordering rationale while
reorganising the *teaching* order, which is a separate concern (see §2.3). The mapping:

| Lab | Slug | Series video | One-line subject |
|---|---|---|---|
| 1 | `long-method` | `xXUBmlFwz_s` | One method five screens long; the master smell |
| 2 | `god-class` | `jFt5uMkTQ6U` | One class that knows everything and does everything |
| 3 | `data-class` | `4381wH2PEdQ` | A data holder with its behaviour parked elsewhere |
| 4 | `primitive-obsession` | `u5EL-QPOxUU` | Price as `double`, email as `String` |
| 5 | `temporary-field` | `tKodfHQ0bYI` | An object whose fields only exist sometimes |
| 6 | `feature-envy` | `dG2y0FHguqw` | A method living in the wrong house |
| 7 | `inappropriate-intimacy` | `3FUXLpjuYwU` | Two classes too familiar with each other's internals |
| 8 | `divergent-change` | `iwOMCqJNfFA` | One class, changed for many unrelated reasons |
| 9 | `switch-statements` | `EpsbKaFHM8w` | The same switch, repeated everywhere the type appears |
| 10 | `lazy-class` | `RLYeybC8ZfU` | A class that works a two-hour week |
| 11 | `class-obsession` | `LZz_PxGWAKk` | Classes because we love classes |
| 12 | `middle-man` | `zpthKKRO19s` | A class whose whole job is forwarding calls |
| 13 | `speculative-generality` | `YofkusanIRc` | Abstractions built for futures that never arrive |
| 14 | `comments` | `E83a4dBANoI` | Comments that apologise instead of explain |
| 15 | `hidden-bugs` | `3qimblc5nvw` | Latent defects that sat in plain sight |

Each row corresponds to a directory under
`src/main/resources/snippets/<slug>/` containing `Before.java` and `After.java`. That
thirty-file corpus is the teaching material of the entire application, and it is worth
pausing on why it is the most important artifact here. Everything else in the app —
controllers, templates, metrics, the quiz — is scaffolding around those thirty files. If
the snippets are weak, the course is weak, and no amount of interface polish will
compensate.

### 1.3 Inventory of the deliverable

```
code-smells/
├── pom.xml                              Spring Boot 4.1.1 parent, Java 21
├── docker-compose.yml                   app on 8084, db on 5435
├── README.md                            orientation and run instructions
├── education.md                         this document
├── db/init.sql                          smell_attempts table, indexes, app role
├── docker/
│   ├── db.Dockerfile                    Postgres 16 + baked-in schema
│   └── app.Dockerfile                   multi-stage maven build → JRE runtime
├── lessons/
│   ├── lesson-plan.md                   the nine-input framework, filled in
│   ├── exercises.md                     one hands-on exercise per lab
│   └── solutions.md                     the answer key, with the key metric per lab
└── src/
    ├── main/java/com/example/codesmells/
    │   ├── CodeSmellsApplication.java
    │   ├── domain/
    │   │   ├── Smell.java               the lab record
    │   │   ├── SmellRegistry.java       the syllabus: all fifteen labs
    │   │   ├── SmellSnippets.java       loads Before/After from the classpath
    │   │   ├── SniffMetrics.java        deterministic line heuristics
    │   │   ├── Diff.java                LCS line diff
    │   │   └── AttemptStore.java        Postgres persistence
    │   └── web/
    │       ├── HomeController.java      syllabus + scoreboard
    │       ├── LabController.java       one generic handler for all fifteen labs
    │       └── QuizController.java      the HTMX quiz endpoint
    ├── main/resources/
    │   ├── application.yml
    │   ├── snippets/<slug>/{Before,After}.java     thirty teaching files
    │   ├── static/css/styles.css
    │   └── templates/
    │       ├── layout.html              head, nav, footer fragments
    │       ├── home.html                the syllabus page
    │       ├── lab.html                 the lab page (works for every smell)
    │       └── partials/
    │           ├── lab-nav.html         previous/next rail
    │           ├── metrics.html         the metrics table fragment
    │           ├── diff.html            the diff fragment
    │           ├── check.html           the quiz verdict fragment
    │           └── scoreboard.html      leaderboard + per-lab statistics
    └── test/java/com/example/codesmells/domain/
        └── SniffMetricsTest.java        thirteen tests pinning metric behaviour
```

### 1.4 The pedagogical reordering

The playlist's order is chronological — a tour, ordered by the author's whim and by
publication date. The lab order is pedagogical. They differ, and the difference is the
single most important instructional decision in the build:

```
Playlist order              Lab order
God Class            →  1.  Long Method
Class Obsession      →  2.  God Class
Lazy Class           →  3.  Data Class
Temporary Field      →  4.  Primitive Obsession
Data Class           →  5.  Temporary Field
Comments             →  6.  Feature Envy
Middle Man           →  7.  Inappropriate Intimacy
Speculative          →  8.  Divergent Change
Hidden Bugs          →  9.  Switch Statements
Feature Envy         → 10.  Lazy Class
Long Method          → 11.  Class Obsession
Inappropriate        → 12.  Middle Man
Primitive Obsession  → 13.  Speculative Generality
Divergent Change     → 14.  Comments
Switch Statements    → 15.  Hidden Bugs
```

Three principles produced that order.

**Start with the master skill.** *Extract Method* is a prerequisite for almost every
other refactoring in the catalogue. If a learner cannot confidently split a long method,
they cannot sensibly split a god class, cannot move a method that envies its data, and
cannot collapse a hierarchy. Lab 1 is therefore the one the playlist places eleventh.

**Then build outward from the object.** Labs 2 through 5 all concern the *shape* of a
single class: too many responsibilities (God Class), no responsibilities (Data Class),
wrong representation (Primitive Obsession), invalid state (Temporary Field). By the end
of lab 5 the learner has a working model of "a class is a bundle of data plus the
invariants that protect it", which is the model the remaining twelve labs manipulate.

**Then relationships between objects.** Labs 6 through 9 are about coupling and cohesion
*between* classes: envy, inappropriate intimacy, divergent change, repeated conditionals.
These are the classical measurements (the series explicitly invokes coupling and
cohesion), and they only make sense once the learner has a stable intuition about one
class.

**Then quantity and proportion.** Labs 10 through 13 are all about *how many* abstractions
exist and whether they earn their keep: Lazy Class, Class Obsession, Middle Man,
Speculative Generality. This is where the series' most contrarian content lives — the
argument that the cure for a bad abstraction is usually deletion, not a better
abstraction — and it lands well only after the learner has seen abstractions earn their
place in labs 2 through 9.

**Finish with humans and consequences.** Lab 14 (Comments) is about communication, and
lab 15 (Hidden Bugs) is about the cost of carelessness. Ending on "here is how it hides in
real systems and here is what it costs" gives the course its moral weight, which a
curriculum about code aesthetics otherwise lacks.

### 1.5 What the learner actually does

Per lab, in order:

1. Reads a two-to-four sentence summary of the smell, its tell, its cure, and *why* the
   cure works.
2. Optionally follows a link to the corresponding video. The app treats the video as
   optional; it is a supplement, not a prerequisite, and the lab is designed to be
   complete on its own.
3. Reads the `Before.java` snippet with line numbers, and compares the smell metrics
   against the refactored twin in a side-by-side layout.
4. Reads the `After.java` snippet.
5. Reads an LCS line diff showing exactly which lines changed — added, removed, retained.
6. Answers two questions: *which smell is this?* and *which refactoring resolves it?* Both
   must be correct for a double-right.
7. Sees their verdict with, when wrong, the tell restated or the expected cure named.
8. Sees the leaderboard update, including per-lab accuracy percentages.

The pedagogical bet is in step 3. Most code-smell material is *descriptive*: it tells you
the smell is bad. Very little is *evidential* within a single screen: it shows you the
counter-metric that moved. Step 3 is where a learner's prior belief ("extracting a method
just moves code around, the function is the same") gets confronted with data from the
snippet in front of them.

---

## Part 2 · Design Decisions and Why They Were Made

Every significant choice in this build was a choice between available alternatives. This
part records the alternatives considered, the option taken, and — where the decision is
genuinely contestable — the argument for the other side.

### 2.1 One generic controller, not fifteen bespoke pages

**The decision.** All fifteen labs are served by a single `LabController` handling
`GET /lab/{slug}`. Adding a sixteenth smell would require one new entry in
`SmellRegistry` and two new snippet files. No new controller, no new template, no new
CSS.

**Why.** A course has a long tail. The obvious first implementation — a controller class
and template per smell — is faster to write for lab one and becomes a liability by lab
six, because the fifteenth smell's page will have drifted in spacing, in section order,
in the placement of the quiz, and in the phrasing of the symptom block. Drift is
invisible until someone notices that lab 12's quiz sits above its code while lab 13's
sits below.

The generic handler inverts the cost curve. All presentation decisions live in one
template, so they cannot drift. All content decisions live in one registry, so adding a
smell is a content change, which is what a maintainer actually wants to make. The
registry is also the single place where the *pedagogical contract* of the course lives:
each entry carries the summary, the tell, the cure, the rationale, and the two quiz
option lists with their correct indices. One record, one lab, no partial states.

**The cost.** The registry is a long Java file. It is a long Java file because each entry
is genuinely long prose with a consistent shape, and the alternative — externalising to
YAML or Markdown — would trade a readable Java file for a data format that either needs a
schema or needs escaping rules, and would make the quiz option indices an opaque
configuration detail. The `Smell` record has nine components and twenty-four lines; that
is a fair price.

### 2.2 Snippets as classpath resources, not database rows

**The decision.** The thirty Java files live in `src/main/resources/snippets/<slug>/` and
are loaded by `SmellSnippets` from the classpath.

**Why.** Because the snippets are *code*, and code belongs in files that a compiler, an
IDE, and a human can all work with. Two consequences follow. First, the snippets get
syntax highlighting in the repository, in review diffs, and in any editor the maintainer
uses. Second, and more importantly, `SniffMetricsTest` can load them from the same
classpath the application uses, which means the tests measure exactly what the page shows
— not a copy, not a stale fixture. A database-stored snippet would have required either
duplication or a fixture-drift risk that is precisely the kind of quiet rot this project
was trying to avoid.

There is also a small pedagogical benefit. A learner who clones the repository has the
snippets on disk. A curious learner can open `Before.java` in an IDE, hit compile, and
break it. That is a legitimate form of engagement, and the design costs nothing to permit.

### 2.3 Metrics that are heuristics, and labelled as such

**The decision.** `SniffMetrics` computes seven numbers by counting lines, comments,
method declarations, field declarations, type mentions, switch/case lines, and the length
of the longest method body found by walking brace depth. No parser, no AST, no external
analysis library.

**Why.** A real static-analysis tool would be more accurate and would also be wrong for
this purpose in a specific way. The goal is not to certify code quality; the goal is to
give a learner a *counter-metric that visibly moves* when the refactoring is applied, and
to do it in under a millisecond so it can be recomputed per request on thirty-line
snippets. A dependency-free brace walk achieves that. It also has a virtue that accuracy
would destroy: **the learner can predict it.** They can count the `case` lines in the
switch snippet themselves and check the app's number. A tool they cannot reproduce
becomes an oracle; a tool they can reproduce becomes a lesson.

**The honesty requirement.** These numbers are labelled "metrics", never "quality
scores", and the lesson plan states plainly under Teacher Decisions: *"Metrics are
heuristics (brace-counting, not full parser)"*. A learner who learns that longest-method
is a brace-walk and not a complexity measure has been taught accurately; a learner who
learns that the app certifies code quality has been taught something false.

**Where the heuristic had to be fixed.** The first version of `SniffMetrics` measured
"longest method" as the gap between consecutive method-*looking* lines, which produced a
number dominated by method *call* lines. On the Long Method snippet it reported 11 → 9
where the truth is 33 → 8. A master lab whose headline metric barely moved is a broken
lab, so the analyzer was rewritten to walk brace depth from each declaration
(`SniffMetrics.java:137`). It is the clearest single example in this build of a metric
that is only worth having if it moves in the direction the lesson claims.

### 2.4 The diff is computed, not stored

**The decision.** `Diff` implements a standard longest-common-subsequence line diff
(O(n·m), trivially fast at these sizes) and the template renders added, removed, and
retained lines.

**Why.** A diff is the most information-dense way to show the *shape* of a refactoring,
and it is information the two code panels do not carry. The two panels tell you what the
before code was and what the after code is. The diff additionally tells you the edit was
*local* — that the refactoring did not rewrite the world, that it left unrelated lines
alone. That locality is itself a teaching point: a good refactoring is a small,
reviewable, behaviour-preserving edit, and a diff that shows forty changed lines out of
eighty makes the reviewer nervous in a way that prose cannot.

**Why LCS specifically.** Because LCS is the algorithm that produces a *minimal* edit
script. A naive diff that reports every line as changed would technically be correct and
practically useless — the learner would see red and green everywhere and learn nothing
about the size of the edit. Minimality is a property worth having even at n < 100.

### 2.5 Assessment is a persisted double-right, not a self-assessment checkbox

**The decision.** Each lab's quiz asks two multiple-choice questions: identify the smell,
then name the cure. Both correct = a double-right, which is the only thing the leaderboard
rewards. Every attempt, right or wrong, is inserted into Postgres.

**Why two questions.** Because identifying a smell and fixing it are different skills at
different altitudes, and conflating them hides which one a learner actually lacks.
Identifying is recognition — pattern-matching against a catalogue, which some learners
are good at for reasons that have nothing to do with judgement. Naming the cure requires
the learner to connect the smell to a structural change, which is the skill the course
exists to build. A learner who names "God Class" but reaches for "add getters" has learned
nothing transferable, and a single-question quiz would have scored that learner as
correct.

**Why multiple choice.** Because the alternative — free-text "describe the refactoring" —
measures the ability to produce vocabulary a learner may not possess yet, and scores it as
a knowledge gap when the actual gap is recall under time pressure. Multiple choice with
plausible distractors (all three options in every quiz are plausible-sounding engineering
moves, not jokes) isolates the judgement from the phrasing. The distractors were chosen
with care: "Convert to a Singleton", "Inline Method", "Write more comments everywhere",
"Extract two more interfaces", "Add getters and setters" are all things people genuinely
do. A distractor that is obviously wrong teaches nothing.

**Why persist wrong attempts.** Two reasons. Pedagogically: a learner who is wrong three
times in a row and then right has learned something, and the scoreboard should reflect
that learning happened rather than hide the struggle. Practically: the per-lab accuracy
table ("Long Method: 6 attempts, 67% identify correct, 33% refactor correct") is an
instructor's diagnostic. A class where Long Method refactor accuracy sits at 30% and
Feature Envy identify sits at 95% is a class that needs another fifteen minutes on
extraction and no more time on envy.

### 2.6 The database is a course feature, not a logging feature

**The decision.** Postgres 16, one table, `smell_attempts`, with indexes on `lab` and
`player`, plus a least-privilege application role created idempotently in the init
script. Every write path degrades gracefully: if the database is unreachable, the labs
still render, the quiz still returns a verdict, and only the scoreboard goes quiet.

**Why graceful degradation matters here specifically.** This app is used by learners in
rooms with unreliable networks and by maintainers who cloned the repo and ran
`mvn spring-boot:run` without starting Docker. An app that returns HTTP 500 on every lab
page because a scoring database is down is an app whose *teaching* is hostage to its
*analytics*. The `AttemptStore` catches `DataAccessException` and returns empty lists, so
the course continues and the scoreboard shows an empty state that explains itself
("No attempts yet — the database starts quiet"). Teaching availability outranks
measurement completeness.

**Why a real database rather than in-memory state.** Because the leaderboard is only
interesting if it is shared. The point of a scoreboard is that a cohort can see itself. An
in-memory list would make the feature a curiosity rather than an instrument, and it would
silently lose history on every redeploy — the exact moment a learner returns to check
their standing.

### 2.7 HTMX for one interaction only

**The decision.** HTMX 2.0.6 from a CDN is the only JavaScript dependency. It is used for
exactly one thing: submitting the quiz form and swapping the returned verdict fragment
(and the refreshed scoreboard) into the page.

**Why so little.** Because the interaction that benefits most from partial-page updates is
precisely this one: the learner has read a lot of code, formed a hypothesis, and wants to
commit to it. A full page reload at that moment destroys the reading context — they have
to scroll back, re-find the After panel, and re-establish what they were thinking. An
in-place swap of the verdict keeps the whole lab — before code, after code, diff — on
screen while the answer appears beneath the form. That is a real cognitive continuity
win for about eight lines of markup and zero lines of custom JavaScript.

**Why not HTMX the code panels too.** Considered and rejected. Pre-rendering both panels
server-side means the page is complete and readable on first paint, works without
JavaScript, works when the CDN is blocked (which happens on locked-down corporate
networks, and is precisely the environment this app's audience often occupies), and
carries zero risk of a learner staring at an empty div. The pre-rendered approach is
strictly more robust; the only thing it costs is a slightly larger initial payload, which
for two ~40-line snippets is irrelevant.

**The name-memory micro-feature.** The layout carries a small inline script that
persists the learner's chosen name to `localStorage` and pre-fills the quiz's name field
on subsequent labs. This began as a server-side `playerName` model attribute driven from
a cookie; it was changed to client-side storage during the build because a purely cosmetic
convenience should not require a model attribute, a cookie read, or a value threaded
through two controllers. It is the only custom JavaScript in the application, and it is
twelve lines.

### 2.8 Ports, and the discipline of not colliding

**The decision.** App on host port **8084**, database on host port **5435**, both mapping
to container-standard ports 8080 and 5432.

**Why.** A developer machine is a crowded place. In the workspace where this project was
built, ports 5432, 5433 and 5434 already carried three other Postgres containers, and
8080, 8081 and 8083 already carried three other Spring applications. Claiming a familiar
port would have made `docker compose up` fail with a bind error, or — worse — succeed and
route requests into the wrong container. The numbers were chosen by inspection of what was
already in use, which is unglamorous and completely reliable.

**Why the *internal* ports stay standard.** Only the host side is remapped. Inside the
compose network the app still talks to `jdbc:postgresql://db:5432/postgres`, and the
container still exposes 8080. Remapping internal ports buys nothing and would break every
default health check and every `EXPOSE` convention.

### 2.9 Infrastructure baked into images, not bind-mounted

**The decision.** `docker/db.Dockerfile` is two lines: a `postgres:16` base and a `COPY`
of `db/init.sql` into `/docker-entrypoint-initdb.d/`. No volume mount for the schema file.

**Why.** Because bind mounts from a macOS host into a Linux container are subject to the
host's file-access permissions, and in the environment where this was built that
permission prompt reliably denied access. The symptom is a container that starts
"successfully" with no schema, and then a scoreboard that quietly catches exceptions and
shows nothing — precisely the graceful-degradation path from §2.6 masking a build
infrastructure problem. Baking the schema into the image makes the failure impossible
rather than merely unlikely. The named volume for database *data* is kept, because data
persistence is a different concern from schema provisioning and the two should not share
a mechanism.

### 2.10 Two-stage application image

**The decision.** A build stage on `maven:3.9-eclipse-temurin-21`, a runtime stage on
`eclipse-temurin:21-jre`, with only the built jar copied across.

**Why.** Image size and attack surface. The build stage carries Maven, a local repository
and a compiler; none of that belongs in a running service. The runtime stage carries a
JRE and a jar. Dependencies are resolved in a separate earlier layer (`dependency:go-offline`
after copying only `pom.xml`) so that editing a Java file rebuilds in seconds instead of
re-resolving the world. These are unglamorous choices with unglamorous payoffs, and they
are the difference between a fifteen-second rebuild and a two-minute one, which is the
difference between an instructor demoing the app and an instructor waiting for the app.

---

## Part 3 · How It Works, File by File

This part is the mechanical companion to Part 2: what each file does, in the order data
flows through them. A maintainer extending the course should be able to read this section
and then add a seventeenth smell without reading the rest of the code.

### 3.1 The lab record

`domain/Smell.java` is a Java record with nine components: `slug`, `episode`, `title`,
`subtitle`, `videoId`, `summary`, `symptom`, `fix`, `why`, the two snippet paths, the two
option lists, and the two correct indices. Two convenience methods, `identifyAnswer()` and
`refactorAnswer()`, resolve the correct option from the index, so grading logic in
`QuizController` is a single string comparison and cannot drift from the presentation
logic that shows the learner the options.

The design point: **grading derives from the same data that renders the question.** A
separate answer key would be a second source of truth, and the day it disagrees with the
options the app is lying to learners in a way nobody notices until the scoreboard looks
wrong.

### 3.2 The registry

`domain/SmellRegistry.java` is a single immutable `List<Smell>` built in one expression,
each entry produced by a private static helper `s(...)` that fills in the snippet paths
from the slug (`/snippets/<slug>/Before.java` and `/snippets/<slug>/After.java`) so that
no entry can point at the wrong file. The registry is a Spring `@Component` exposing
`all()` and `bySlug(slug)`; `bySlug` throws for an unknown slug, which produces a 500 for
a mistyped URL — acceptable, because URLs in this app come from the registry itself and
are therefore always valid.

The lab pages' previous/next links are computed by index against `all()`, which is why
the list order *is* the curriculum order. There is no separate navigation configuration
to forget to update.

### 3.3 Loading the snippets

`SmellSnippets` is nine lines. It builds a classpath resource path and reads it as UTF-8,
wrapping `IOException` in `IllegalStateException` with the path in the message. That
exception message matters more than it looks: a missing snippet is a build-packing error
that would otherwise surface as a bare 500, and with the path in the message the failure
names its own cause in the log.

### 3.4 Computing the metrics

`SniffMetrics.of(source)` walks the lines once, maintaining a small state machine for
block comments (`/* ... */` spanning lines), and for each non-blank, non-comment line:

- increments `codeLines`;
- tests `isField(cleaned)` — requires a trailing `;`, a non-control first word, a
  `type name` shape, and that the line is not itself a method declaration;
- tests `isMethodDeclaration(cleaned)` — requires a matching pair of parentheses, an
  opening brace, a first word that is not a control keyword, and the presence of a
  modifier or `void` on the line;
- counts `switch`/`case`/`default` lines into `branchCount`;
- harvests capitalised identifiers into a set, excluding a stop-list of built-ins
  (`String`, `List`, `BigDecimal`, `LocalDate` and friends) so that `typeMentions` counts
  *domain* types rather than the twenty types everyone uses.

A second pass, `longestMethodOf`, walks brace depth from every declaration to its matching
close and keeps the largest span. Comments are stripped before the walk, so a long method
that is *mostly* comment does not get credit for the comment — the honest measure of body
size. Blank and comment lines *inside* the body do count, because that padding is part of
what makes a method long to read.

The stop-list deserves a note. It contains a few entries that are not built-ins at all
(`Record`, `Field`), added because those capitalised words appear in snippets for reasons
unrelated to domain types. This is the heuristic's honest edge, and the test suite exists
partly to catch the day an edge becomes a lie.

### 3.5 Computing the diff

`Diff.lcs(before, after)` builds an `(n+1)×(m+1)` table of longest-common-subsequence
lengths by dynamic programming, then walks it forward emitting `SAME`, `REMOVED`, and
`ADDED` lines carrying their 1-based line numbers in the respective file. `Diff.changedLines`
counts the non-`SAME` entries, which the lab page shows as "(88 lines)" next to the diff
heading. The implementation is the textbook one; at thirty to eighty lines per side the
quadratic cost is irrelevant and the code is short enough to read in one sitting — which
matters more here than performance, because a maintainer should be able to audit the
educational tooling.

### 3.6 Rendering a lab page

`LabController.lab(slug, model)` does the whole job in one method: resolve the smell, load
both snippets, compute the diff, compute both metrics objects, split both snippets into
line arrays, work out previous/next from the registry index, and fetch this lab's row from
the per-lab statistics for display. It adds thirteen model attributes and returns
`"lab"`.

`lab.html` then assembles the page. The code panels iterate the line arrays twice, once
per column, emitting a `<span class="ln">` gutter and a `<span class="txt">` body per
line. Because Thymeleaf escapes `th:text` output by default, the snippets' angle brackets
(generics like `List<OrderLine>`) render as text rather than being interpreted as markup —
a detail that would have been a serious defect had the templates used `th:utext`.

The metrics table is a fragment, `partials/metrics.html`, declared as
`th:fragment="metrics(m, label)"` and included twice with different arguments. Fragments
were chosen over duplicating a thirteen-line table because duplication is exactly how two
labs end up disagreeing about which metrics are shown.

### 3.7 The quiz round trip

The form is a standard HTML form carrying two HTMX attributes:

```html
<form th:hx-post="@{/lab/{slug}/check(slug=${smell.slug})}"
      hx-target="#quiz-result" hx-swap="innerHTML">
```

`QuizController.check(...)` takes the player name, the identify choice and the refactor
choice, compares both against `smell.identifyAnswer()` and `smell.refactorAnswer()`,
inserts the attempt, re-reads the leaderboard and per-lab statistics, and returns the
`partials/check` view. That view is a verdict block — with the learner's chosen option,
the correct answer when they were wrong, and the tell restated — followed by a fresh
scoreboard partial appended beneath it.

The consequence worth noting: **the scoreboard the learner sees immediately after an
answer is the scoreboard including their own answer.** There is no stale-state window and
no "refresh to see your result" step, which for a self-assessed course is a small but
real difference in whether the loop feels closed.

### 3.8 Persistence

`AttemptStore` is four methods over a `JdbcTemplate`: `insert`, `recent`, `leaderboard`,
`labStats`. The leaderboard query groups by player and uses `COUNT(*) FILTER (WHERE ...)`
twice — once for smell-correct, once for the double-right — ordered by doubles descending.
The per-lab query uses the same filter idiom to compute round-numbered percentages, which
is why the table shows integers: these are counts of a cohort's judgements, not
measurements of a physical quantity, and false precision would be a small lie.

Each of the four methods wraps its call in `try`/`catch (DataAccessException)` returning
an empty list. Four small catches, one behaviour: *the course does not depend on the
scoreboard working*.

### 3.9 The tests

`SniffMetricsTest` contains thirteen tests. They do not test the metrics against
hand-computed constants — a brittle choice, since the snippets are meant to be edited.
They test **direction**: that each lab's headline metric moves in the direction its lesson
claims. Long Method's longest method must shrink. God Class's field count must drop.
Data Class's method count must rise and type mentions fall. Switch Statements' branch
count must fall. Comments' comment lines must drop. Speculative Generality's code lines
must more than halve. Lazy Class and Middle Man must lose code. Feature Envy must lose
code without growing a method. Inappropriate Intimacy's longest method must shrink.
Temporary Field must lose a field. Hidden Bugs must not shrink — it trades lines for
explicit validation, and a test that demanded shrinkage would have pushed the author
toward deleting the very checks the lab teaches.

This is a small but important design stance: **the tests encode pedagogy, not just
correctness.** If a future edit to a snippet makes its refactor look worse, the test
fails, and the maintainer is forced to confront the fact that they have made the lesson
weaker. That is a far more valuable failure than a typo in a counter.

---

## Part 4 · The Fifteen Labs

Each chapter below covers one lab: what the smell is, where the tell is in the snippet,
what the refactoring does, what the metrics actually measured, and how to teach it. The
metric numbers are from the running application, captured after the analyzer was
corrected in §5.3. Read them as evidence of what the learner will see on their screen,
not as theoretical claims.

---

### Lab 1 · Long Method (`xXUBmlFwz_s`)

**The smell.** A method that has grown past the point where a person can hold it in their
head as a single unit of meaning. The series frames this as the master smell, and the
framing is correct: nearly every other refactoring in the catalogue is easier once you
can split a method cleanly. A long method is not automatically wrong — a linear algorithm
with a genuinely irreducible sequence of steps can be long and fine — but a long method
with independent concerns bundled together is a defect, and it is *recoverable*, which is
what makes it the right place to start a course.

**The tell in the snippet.** `Before.java` shows an `OrderProcessor.processOrder` that
validates three things, computes money, applies a discount, mutates inventory, flips
order status, and sends two emails — then a `shippingFor` helper tacked on beneath it
whose body is a single ternary. Four readable code comments act as paragraph breaks, the
classic symptom of a method that has outgrown its name. A reader who wants to know what
happens to stock has to hold the email logic in their head to find out.

**The refactoring.** The After panel keeps `processOrder` as a six-line reading list —
`validate`, `price`, `reserveStock`, two setters, `notify` — and moves each concern into
its own named method, including a family of small assertion helpers. Two of the extractions
are the canonical ones worth naming aloud: *Extract Method* for the price arithmetic, and
*Extract Query* for the subtotal loop, which is the one extraction that also makes the
method testable without mocking the order.

**The metrics.** Code lines rise 37 → 68, method declarations 2 → 12, and longest method
falls **33 → 8**. Comment lines drop 6 → 0 because the section comments are no longer
needed once the section is a method. Those two movements — more lines, shorter methods —
are the point of the whole lab, and they must be taught as *the same behaviour in more
lines*. Learners routinely read a code-line increase as a regression. It is not: it is the
cost of naming, and the naming is what makes the next change safe. Field count (1 → 1)
and type mentions (9 → 9) do not move, which is also worth saying — extraction is a
within-class transformation and it should leave the class's shape alone.

**Teaching notes.** Ask the learner to predict the direction of every metric *before*
revealing the After panel; the surprise of "code lines go up" is memorable in a way that
"extraction adds lines" is not. Then ask the reverse question: name one thing the
refactoring did *not* fix. (It did not create a seam for testing `notify`, and it did not
change the class's responsibility — it still does order processing. Both are honest
answers, and both set up Labs 2 and 6.)

**The trap.** Learners frequently extract a method per *line* of the original rather than
per *concept*, producing `computeTaxOnSubtotalOfItemsPlusShipping` and a new class of
unreadable method. The fix in the room is to ask them to say the extracted method's
purpose as a verb phrase. If the phrase needs an "and" in it, the extraction is wrong.

---

### Lab 2 · God Class (`jFt5uMkTQ6U`)

**The smell.** A class that accumulates responsibilities until every change to any
concern lands in the same file. It almost never arrives as a monster; it arrives as the
convenient class that everyone adds to because it is already imported. The series calls
out the specific failure mode: this class becomes the reason merges conflict, the reason
the test suite is a gauntlet, and the reason nobody wants to touch the file.

**The tell in the snippet.** `ExpenseTracker` holds expenses *and* a budget *and* an
accountant's name and email *and* a tax rate *and* a reminder cutoff *and* a list of
birthday contacts, then implements budget reporting, tax summarising, and birthday
reminders. Seven fields, seven responsibilities, one private `email` helper doing the work
all three features need. Every method is a plausible method; the problem is that no two of
them would ever be changed together.

**The refactoring.** Three extractions — `Budget`, `ReminderService`, `ReceiptEmailer` —
leave `ExpenseTracker` as a thin coordinator that holds the three collaborators and
forwards. The facade is deliberate and should be discussed rather than quietly kept: it
exists so the callers do not all change in the same commit, which is exactly the property
that makes a refactoring safe to land in a busy codebase. The class also stops pretending
to be a data store and starts holding initialised collaborators.

**The metrics.** Code lines rise 51 → 78 and method declarations 7 → 14, because the
god class's concerns now have to be *written down* in separate classes. Fields fall
**7 → 3** — the headline — but the interesting fact is *why*: the fields do not vanish,
they **disperse**, each extracted class carrying only the state its own concern needs.
Type mentions rise 5 → 8 as new named types appear. A learner who expects "extracting
classes makes the code smaller" is wrong here, and the page is where they find out.

**Teaching notes.** The question that unlocks this lab: *"name the last bug in this file.
Who else would have had to change it?"* When a learner cannot name a second, the class is
not a god class — it is a class with two responsibilities, and the honest advice is to
extract one. Related trap: extracting classes but keeping every original method as a
forwarder is God Class with better formatting. If the extracted class's only public method
is `doTheOldThing()`, the extraction has not happened.

---

### Lab 3 · Data Class (`4381wH2PEdQ`)

**The smell.** A class that holds data and no behaviour, with the behaviour living in
services that interrogate it. The series frames this as data and behaviour being separated
at birth, and the practical consequence is that every consumer re-implements the same
rules, slightly differently. The rules do not live anywhere; they are copy-pasted with
drift.

**The tell in the snippet.** `OrderDraft` is a bag of fields behind eight getter/setter
pairs. `OrderService` then does the actual thinking: it walks the items, adds shipping by
country, decides whether the order can ship, and formats a receipt — while reaching into
the draft for `getItems()`, `getShippingCountry()`, `getPaid()`, `isShipped()` and
writing `setShipped(true)` back. Every rule is in the wrong place, and the draft itself
cannot enforce a single one.

**The refactoring.** The service disappears. `Order` gets a constructor, `total()`,
`shipping()`, `markShipped()`, `confirmPaid()` and `receipt()`. Two of those methods are
now *enforcing* rather than *computing*: `markShipped()` refuses an unpaid order and
refuses a second shipment; `confirmPaid()` rejects underpayment. Those guards used to be
callers' responsibility; now they are properties of the thing that has the state. The
course should point at this explicitly, because it is the entire argument for the
refactoring: **invariants belong with the state they constrain**, and a data class is a
class that has been denied the chance to hold any.

**The metrics.** Code lines are effectively flat, 41 → 42, and this is a genuine teaching
moment: moving behaviour does not reduce the amount of behaviour. Method declarations rise
**3 → 6** as the bag of accessors becomes a class of rules, and type mentions fall **6 → 5**
as the service type disappears. Field count is unchanged at 6, which is correct — the data
did not move, only the logic.

**Teaching notes.** Ask the learner to find the invariant in the Before panel ("an order
cannot ship unpaid") and then ask where that rule could be violated from. The answer is
"any caller that forgets to check", which is the whole lesson. A strong follow-up: ask
them to write the one-line test that the refactor now makes unnecessary. Trap to name
explicitly: the Data Class and the "anemic domain model" debate. A deliberate data class
is sometimes correct — DTOs, records at a boundary, transport models. The smell is
behaviour in the *domain* living far from the domain data, not the existence of plain
data.

---

### Lab 4 · Primitive Obsession (`u5EL-QPOxUU`)

**The smell.** Domain ideas modelled with the language's basic types. Money is a `double`,
an email is a `String`, a SKU is a text field, and every operation on them re-imposes the
meaning that the type system was supposed to carry. The series' contrast between a
"dystopia" of naked primitives and a "utopia" of value objects is the right frame:
primitives are promiscuous — a `String` parameter will accept anything at all — and a
value object is opinionated.

**The tell in the snippet.** `Payment` holds `double price`, `String currency`,
`String customerEmail`, `String sku`, `int quantity`, and offers two static validators,
`looksLikeEmail` and `looksLikeSku`. `Checkout.pay` then re-checks email, price and SKU
before doing arithmetic. The tell is visible in the arithmetic: `price * quantity * 1.21`
— a `double` multiplied by a VAT rate, where a rounding rule is hiding somewhere and
nobody has written it down. And the tax rate is applied inside `total()` unconditionally,
because the method has no way to know the currency.

**The refactoring.** Three value objects — `Money`, `Sku`, `EmailAddress` — each
validating in its constructor, and `Money` owning `times`, `withRate` and
`ensureSameCurrencyAs`. The validation *helpers disappear entirely*, because there is no
longer a moment at which an invalid value can exist. The most valuable line to point at is
`ensureSameCurrencyAs`: currency mistakes were previously a runtime surprise somewhere
downstream, and are now a named operation that throws at the point of the mistake.

**The metrics.** Code lines rise 44 → 70 and type mentions rise **6 → 11** — the only lab
where a *count going up* is unambiguously the good outcome, because each new type is a
piece of domain vocabulary that used to be implicit. Longest method falls 14 → 10. Field
count is 6 → 7, essentially flat, since the state is the same; what changed is that the
fields are now typed with opinions.

**Teaching notes.** This is the lab where "more code is better" is most counter-intuitive,
so lead with the arithmetic. Ask: *what does `0.1 + 0.2` mean in a price field?* Then ask
the same question after the refactor, where the answer is "it cannot happen". Practical
framing for working developers: this is the class of bug that reaches production as a
one-cent discrepancy, and the fix is a type, not a code review comment.

**The trap.** Value-object sprawl. Three or four well-chosen value objects beat thirty
one-method wrappers. The test to teach is whether the type *prevents* something — a
`Money` prevents currency mixing, an `EmailAddress` prevents bad addresses. A
`CustomerId` wrapper that only holds a `long` and prevents nothing is ceremony.

---

### Lab 5 · Temporary Field (`tKodfHQ0bYI`)

**The smell.** An object that carries fields which are only meaningful on some code paths.
The field looks like state; it is actually a parameter in disguise, and because it is
sometimes null, every reader needs a guard. The series treats this as the quietest of the
smells and therefore the most dangerous, because nothing looks wrong and the bugs arrive
as production incidents.

**The tell in the snippet.** `Order` has three honest fields (`id`, `amount`,
`placedOn`) and three conditional ones (`promotionalDiscount`, `voucherCode`,
`expiresOn`), set together by a single `applyVoucher` method. Then `total()` guards all
three, `receiptText()` guards two, and `isDiscounted()` guards one. An `Order` constructed
without calling `applyVoucher` is in a *valid-looking* state that is not really valid, and
no type in the system says so.

**The refactoring.** The conditional trio becomes `Voucher` — its own small type with a
`validOn(day)` method. `Order` returns to being an order, `total()` delegates to
`totalAfter(voucher)`, and every null guard is gone. The lesson is precise and worth
stating: a field that exists on some instances only is a *conditional concept* the object
is pretending to have. Either the concept is always present (make it a parameter, or make
the type enforce it) or it is a different concept wearing this object's clothes.

**The metrics.** Code lines 38 → 45, method declarations 5 → 8, fields fall **7 → 6**.
The one-field drop is modest, and honesty requires saying so: the real win is not the
count, it is the *absence of four guard expressions* that no counter captures. Field
count is the weakest available proxy for this lab, which is a good moment to tell learners
that a metric panel is evidence, not a verdict.

**Teaching notes.** The killer question: *"construct this `Order` and call `total()`. Is
the answer right? How would anyone reading `total()` know that?"* The follow-up — *which
fields would you make the compiler check?* — generalises to the whole catalogue. Trap:
over-correcting into "make everything a separate type". The cure is proportion; a
temporary field that always has a sensible default is not the same problem as one that
branches the object's meaning.

---

### Lab 6 · Feature Envy (`dG2y0FHguqw`)

**The smell.** A method that spends more of its body reaching into another object than
operating on its own. The series' rule of thumb is domestic: the method and the data it
genuinely needs should be roommates. A method that is mostly `other.getSomething()` is
behaving like a method of `other` that is living in the wrong class.

**The tell in the snippet.** `DiscountService.discountFor(Customer, BigDecimal)` opens
with `customer.getTier()`, then `customer.getPastEmails().size()`, then
`customer.getJoined()`, then `customer.getLoyaltyPoints()` — and reaches into
`customer.getTier()` *again* in the fourth condition. The prefix `customer.` appears
eleven times. `Customer` is a passive data holder with four getters, and
`DiscountService` knows the structure of its loyalty programme.

**The refactoring.** `discountFor` moves onto `Customer` and the service disappears
entirely. The method body is unchanged in logic — which is the point worth emphasising:
*Feature Envy's refactoring is a move, not a rewrite* — but the eleven `customer.`
prefixes collapse to zero, because from inside the class the data has no name at all.

**The metrics.** Code lines fall 40 → 34, method declarations stay at 2, longest method
is unchanged at 19, fields 5 → 5, type mentions 3 → 3. Every structural counter is
flat, and the code got shorter. This lab is the strongest argument in the course that
**a metric panel cannot be the whole argument** — the win here is *where the knowledge
lives*, and no line count captures that. Say so in the room. It is a humbling and
useful lesson about the limits of the tool the learner is looking at.

**Teaching notes.** Have the learner count the `customer.` prefixes themselves before
revealing the After panel; the count is a proxy for envy, and asking them to derive the
measurement is what makes it stick. Then the harder question: *whose* discount rules are
these? If the answer is "the customer's", the envy is confirmed. If the answer is "the
loyalty programme's", the correct refactor is neither move — it is to give the loyalty
programme its own type, and the learner has just designed a better solution than the lab
provides.

**The trap.** Moving a method that legitimately needs another's data. Two collaborating
objects are not envious of each other; envy is directional and asymmetric. A method that
reads one field from a collaborator and does its own work is fine where it is.

---

### Lab 7 · Inappropriate Intimacy (`3FUXLpjuYwU`)

**The smell.** Two classes so familiar with each other's internals that they are hard to
change independently. The series' image is the fondue: two forks sharing one pot, each
reaching past the other's boundary. The distinction from Feature Envy matters — envy is
one method leaning on another object; intimacy is a *relationship* between classes, and
it is usually bidirectional.

**The tell in the snippet.** `Employee` and `Department` each have full public getters and
setters for everything, including `grade` and `level` — two representations of the same
concept, free to disagree. `Department.promoteEach` then reaches into each `Employee`,
reads `getGrade()`, writes `setGrade(grade + 1)`, writes `setLevel("LEVEL_" + (grade+1))`,
and reads back `getGrade()` to decide on a salary raise. `setLevel` derives from `grade`
inside a *different* class, so the invariant "level matches grade" can be violated by
anything that calls `setLevel` directly. Meanwhile `isOverBudget` walks the same staff
list and does payroll arithmetic — knowledge of salary internals that belongs to the
employee.

**The refactoring.** Fields become `final` and the association becomes unidirectional in
practice: the department manages a roster and delegates. `Employee` gains real behaviour —
`monthlyPay()`, `promoted()` returning a *new* employee, and `bonus()` — so the promotion
rules live where the grade lives, and `Department` can no longer write the employee's
internals even by accident. `promotePaidEnumerable` composes with a method reference.

**The metrics.** Code lines fall 47 → 44, longest method **11 → 6**, method declarations
rise 3 → 6, fields 8 → 9, type mentions 3 → 4. Field count goes *up*, and this is the
lab's most instructive number: making a class immutable costs declarations, because
`promoted()` has to hand back a fresh object. The win is not fewer fields; it is that
half of them can no longer be written by strangers.

**Teaching notes.** Frame the lab around the *bug*, not the style. Ask the learner to
write a two-line method elsewhere in the system that breaks the "level matches grade"
invariant, and let them feel how easy it is. The immutable-return refactor is the modern
answer, and it is worth naming that it also makes the object safe to share across threads
— a benefit the lab does not need in order to be worthwhile.

**The trap.** Reading intimacy as "too much code in common" and answering with an
interface. An interface does not reduce coupling if both classes still call each other's
internals behind it. The question is *how* they are connected, not how much text they
share.

---

### Lab 8 · Divergent Change (`iwOMCqJNfFA`)

**The smell.** One class that must be edited whenever any of several unrelated things
happens. The series presents this as the still frame of a paired problem: *Shotgun
Surgery* is the motion (many files, one change), *Divergent Change* is the cause (one file,
many changes). If a class shows up in unrelated commit messages, this is why.

**The tell in the snippet.** `ReportService` has three jobs, and the file carries a
comment block that admits it: it changes when the DB schema changes (the row mapping in
`load`), when the report layout changes (`renderHtml`, `summarizeCsv`), and when the email
content changes (`emailWeekly`, `emailMonthEnd`). A fourth responsibility hides in `runQuery`.
The `Record` at the bottom of the file — `ReportRow` — is a hint that the data shape
belongs to a different layer than the email does.

**The refactoring.** Three classes, one reason each: `ReportRepository` owns the query and
the row mapping, `ReportRenderer` owns layout, `ReportMailer` owns email content and
depends on the renderer. A schema migration now touches one file. This lab is the most
direct illustration of the single-responsibility principle, and the most satisfying to
watch, because the diff shows a class being *cut* along a seam that was already there.

**The metrics.** Code lines rise 40 → 46, type mentions 6 → 8, field count 0 → 1. Nothing
shrinks. That is the honest outcome and it must be taught: **Divergent Change is fixed by
adding structure, not by removing code.** The refactor is a relocation plus a set of
public contracts; the total is slightly larger and the *churn* is much smaller. The metric
panel cannot show churn — which is precisely why the lab is worth building. The right
teaching artefact is the diff: the learner can see exactly which lines moved where, and
that the `SELECT` and the `<table>` tag stopped sharing a neighbourhood.

**Teaching notes.** A neat exercise: give learners a git log from a real project and ask
them to find files that appear in commits of unrelated types. The ones that surface
immediately are the divergent-change candidates, and the evidence is empirical rather
than aesthetic. Trap: splitting a class into three that are always changed together. The
test is the same one the file's own comment block failed: would a single logical change
ever require editing two of them? If yes, they were one class.

---

### Lab 9 · Switch Statements (`EpsbKaFHM8w`)

**The smell.** Not the switch syntax — the *repetition*. The series' argument is that a
conditional on a type code is fine once and a liability three times, because each new
kind must be added to every one of them and forgetting one is how "we only changed one
place" becomes an incident.

**The tell in the snippet.** `OrderHandler` contains three switches on the same `typeCode`
parameter: `priceFor`, `labelFor`, `needsShipping`. Each lists `BOOK`, `DIGITAL`,
`TICKET`. The case lists are near-identical, and the methods have nothing to do with each
other except that they all branch on the same string. Add a fourth kind and there are
three edits, two of which nothing will remind you about. Note also that the strings are
bare literals repeated in three places — the *data* is duplicated along with the logic.

**The refactoring.** An `OrderKind` interface with `price`, `label`, `needsShipping`, three
implementations, and a `OrderKindFactory` that performs the *single* remaining switch.
Each kind now answers all three questions about itself, so adding a kind is one new class
plus one factory line. The factory's `default:` now throws rather than silently returning
`"Misc"` — the second lesson hiding in this lab, which is that a silent default is how a
missing case becomes a bug that ships.

**The metrics.** Switch/case lines fall **15 → 5** — the lab's headline, and the number
that finally makes the repetition visible — and type mentions rise 1 → 6 as behaviour
moves onto types. Code lines are roughly flat (27 → 31), method declarations fall 3 → 1
in the handler because the branching collapsed into a polymorphic call. A learner looking
at "31 versus 27 code lines" might conclude nothing happened; the branch count is what
they should be looking at, and it moved by two thirds.

**Teaching notes.** Teach this lab with a physical prop if you have one: write the four
questions a "product type" must answer on the board, then delete the three switches and
hand out one interface with three methods. The physical gesture of *deleting* branching
lines lands harder than a diagram. Trap: the enum-with-behaviour variant, which is
excellent in Java and which learners often reach for immediately — it is a valid answer,
but the polymorphic version scales better when variants carry state, and both are
defensible.

---

### Lab 10 · Lazy Class (`RLYeybC8ZfU`)

**The smell.** A class with too little to justify its existence — one trivial method, or a
subclass that adds nothing. The series' framing is nicely inverted: it is not that lazy
classes are useless, it is that they are *expensive*. Every class is a place a bug can
hide, a name a reader must resolve, and a thing a test must mention. The felt symptom is
"the code feels lazy because it is full of classes that do too little".

**The tell in the snippet.** `CurrentUser` exists to concatenate a greeting. `SessionWrapper`
has `today()` returning `LocalDate.now()` and a method literally called `nothingElse()`
with a comment admitting "even the name does no work". Two files, two classes, one
behaviour, and a `SessionWrapper` that has wrapped nothing.

**The refactoring.** Both are inlined: `UserSession` keeps the greeting and nothing else.
The lesson is deletion, and deletion is a skill that has to be taught deliberately,
because most developers' instincts run the other way — a class feels like structure, and
structure feels like progress. The teachable question is the one from the series: *what
does this class's existence buy anyone?* If the answer is "it makes the code look
organised", that is a decoration, and decoration is a cost.

**The metrics.** Code lines fall 17 → 10, method declarations 4 → 2, type mentions 3 → 1,
comment lines 2 → 1. Everything moves the same direction for once — this lab is the rare
case where the panel is uniformly green, and that contrast with labs 1 through 4 (where
counts rise) is pedagogically useful. Learners who internalise "refactoring can mean
deleting" leave this lab better equipped than learners who only saw extraction.

**Teaching notes.** Pair this with Speculative Generality (Lab 13) as a pair: both are
deletion smells, one for classes that are too small and one for abstractions built for a
future that never arrives. Run them close together if you can. Trap: deleting a class
that a *test* names is harder than deleting a class the code names, and the honest
refactoring sequence is to inline the class, delete it, and fix the test's subject in the
same commit — a small, real lesson about what "small commit" means.

---

### Lab 11 · Class Obsession (`LZz_PxGWAKk`)

**The smell.** The mirror image of Lazy Class: abstraction where a looser structure would
do. The series lists the tell-tales — rigid hierarchies, needless layers, tight coupling
in the name of flexibility — and its image is a museum: a four-level inheritance ladder
for three behaviours, each level a room you must walk through to see the one real thing.

**The tell in the snippet.** Two museums. First, `AuditableEntity` → `ProtocolAuditableEntity`
→ `IntervalAuditableEntity` → `Report`, where each level adds exactly one field and no
behaviour, and the leaf's `render()` has to concatenate three inherited fields it does not
own. Second, `AbstractReport` → `TimestampedReport` → `OrderReport`/`InvoiceReport`, where
the middle level exists only to add a timestamp, and both leaves render `"name@0"`.
Neither hierarchy is paying rent.

**The refactoring.** Both collapse. The report hierarchy keeps `Report` abstract with a
`render()` contract and a shared `meta()` helper, so `OrderReport` and `InvoiceReport`
extend one level and nothing else — the subclasses genuinely differ, which is what a
subclass is for. The audit fields move into a real type, `AuditRecord`, that owns them and
can describe itself. The lesson is *proportion*, and it should be stated carefully: the
cure is not "inheritance is bad", it is "each level should carry a concept a reader would
miss if it were gone".

**The metrics.** Type mentions fall **8 → 4** — the museum's floor count, halved — while
code lines rise slightly (26 → 31) and method declarations rise 1 → 4. The rise in methods
is worth explaining rather than defending: the flattened version has real behaviour
(`meta()`, `describe()`) where the museum had concatenation scattered across leaves.

**Teaching notes.** The best exercise in the course: take the Before panel and, for each
class, ask "if I deleted this, what would a reader lose?" Every `yes` justifies the level;
every `no` condemns it. Run it on a real codebase's inheritance tree and learners are
surprised by how fast it goes. Trap: the reflex to replace inheritance with composition
without asking whether the composition is *worse* — four classes each holding one field is
the same museum, now with more files.

---

### Lab 12 · Middle Man (`zpthKKRO19s`)

**The smell.** A class whose every method forwards to another object. The series names
this the fatal mistake: delegation with no purpose. The object is a toll booth on a road
that goes directly past it, and it charges for every crossing.

**The tell in the snippet.** `OrderManager` holds an `Order` and forwards five calls:
`getStatus`, `getTotal`, `getItems`, `fulfill`, `add`. Every body is a single
`return order.x()` or `order.x()`. There is no state of its own, no decision of its own,
and no reason for a caller to prefer it over the `Order` it wraps. As the class grows, the
manager's method list must track the wrapped class's method list, and the day someone adds
a method to `Order` and forgets the manager, callers using the manager are stuck.

**The refactoring.** The manager is deleted. Callers use `Order` directly. `Order` in the
After panel is also tidier — `final` items, initialised `status` and `total` — because
writing the direct version is when its real requirements (is the list immutable? is
`total` a constant until changed?) become obvious.

**The metrics.** Code lines fall 23 → 15, fields 4 → 3, method declarations 1 → 1, type
mentions 2 → 2. The headline is a *removal*, which makes this a good palate cleanser after
nine labs where numbers mostly rose.

**Teaching notes.** The teachable nuance is that middle men are sometimes legitimate, and
the learner should be able to say when. A proxy that hides a remote system, a facade that
defines a stable interface over a volatile implementation, a null-object that answers for
absent collaborators — all are forwarding, and all earn their place. The test the series
supplies is the right one: *if you delete the middle man, does anything besides the
callers' source files need to change?* If the answer is no, it is dead weight.

**The trap.** A middle man that forwards *and* adds one small policy — "and also
normalises the name" — is a class with a job, however thin. Learners reach for deletion
too eagerly here; the discipline is to check for that one line of value first.

---

### Lab 13 · Speculative Generality (`YofkusanIRc`)

**The smell.** Code written for a future that has not been chosen: an abstract base with
one implementation, a strategy interface with one strategy, a factory whose factory has
one consumer. The series' framing is that this *reads* as flexibility and *costs* as
effort — attention from readers, maintenance from tests — for a requirement no one has.

**The tell in the snippet.** A complete plaza of speculation. `DiscountPolicy` (abstract)
has one subclass, `FlatDiscount`. `DiscountPort` (interface) is implemented only by that
same class. `DiscountGateway` wraps a `DiscountPort` and forwards one call. `Checkout.total`
takes a `discountKind` string that, as its own comment admits, only ever receives
`"FLAT"`, and then branches on it to construct the one implementation that exists. Five
types, one behaviour, and a parameter that exists only to select between nothing and
something.

**The refactoring.** Everything speculative is deleted. `Checkout.total(base, discount)`
subtracts the discount. Four types, one signature, and the same result. The After panel is
six lines against twenty-nine, and the *behaviour is identical* — which is the point: the
speculation was never protecting anything, because there was never a second case to
dispatch to.

**The metrics.** Code lines fall **29 → 6** — by far the largest proportional reduction in
the course — type mentions 5 → 1, fields 2 → 0, methods 3 → 1, longest method 7 → 3. Every
counter moves the right way at once. The test suite pins this one with a strict
inequality (after must be less than *half* of before), because a lab whose whole argument
is deletion should be the most rigorously measured.

**Teaching notes.** This lab needs the most careful framing, because "we might need it
later" is a real concern and dismissing it produces defensive developers. The honest
position, and the one the series takes: the cost of speculation is paid *now*, by every
reader, while the benefit is speculative and may never arrive. And the escape hatch is
not abstinence — it is that refactoring into an abstraction later is a *small* change,
while refactoring *out* of a bad abstraction is an expensive one. A learner who
understands that asymmetry has learned the whole lesson.

**The trap.** Deleting the wrong thing. An interface with two implementations is not
speculative, and neither is a parameter with two callers. Ask for the *count of
implementations* and the *count of call sites* before cutting anything. The lab's distractor
option, "add the second implementation now", exists precisely because inventing a second
implementation to justify the abstraction is the most common real-world form of this
mistake.

---

### Lab 14 · Comments (`E83a4dBANoI`)

**The smell.** Not comments. Three specific failures, which the series names and which the
snippet demonstrates one at a time: comments that restate the code, comments that have
drifted from the code they describe, and commented-out code left behind as a graveyard.

**The tell in the snippet.** `Pricing.computeTotalPrice` opens with
`// computes the total price` above a method named `computeTotalPrice`, then
`// TOTAL = ZERO` above `BigDecimal total = BigDecimal.ZERO;`, then
`// add the price to total` above the line that adds the price. Then the drift: a
trailing comment on that line says *"this is wrong: prices are already the final prices"*
— the comment contradicts the code and nobody fixed either. Then a nine-line commented-out
`if` block ending in `System.out.println("old bulk logic")` and a "bulk gnarni" typo.
Finally the worst line: `return totalPrice.equals(BigDecimal.ZERO) ? total : totalPrice;`
with the comment `// return total`. This is a method that has been edited into
incoherence, and the comments are what let it stay incoherent, because they *appear* to
document it.

**The refactoring.** Comments are deleted and the code is renamed to carry the intent:
`computeTotalPrice` becomes `total`, and the 5% discount becomes a named constant
`CLIENT_DISCOUNT` — whose name preserves the one piece of information the comments
actually contained (that the discount is at a client's insistence, not a business rule).
The genuinely dead commented-out block is deleted outright, because commented-out code is
a version-control feature being used in a place where it does not work.

**The metrics.** Comment lines fall **10 → 0**, code lines 13 → 9, longest method 23 → 4.
The longest-method collapse is the striking one and it is not really about method length:
the Before method is long *because* the comment scaffolding makes it long, and the
dead `totalPrice` variable existed only to give the misleading comment something to
attach to. Deleting the lies made the method short.

**Teaching notes.** Teach the distinction the series insists on: **comments should say
*why*, code should say *what*.** The surviving `CLIENT_DISCOUNT` constant is the worked
example — the "why" (client insisted) is information the code cannot recover, so it is
kept; everything else was "what". A great in-room exercise: hand out a printout of the
Before panel and ask learners to mark every comment as *redundant*, *misleading*, or
*keep*. The correct answer is seven redundant, one misleading, one keep-as-constant, one
delete-with-code.

**The trap.** Comment deletion as dogma. The `/* ... */` block that explains *why* a
hack exists is the most valuable comment in any codebase, and deleting it because it is
"just a comment" destroys institutional memory. This lab must be taught with its
counterpart, which is Lab 13: delete speculation, keep truth.

---

### Lab 15 · Hidden Bugs (`3qimblc5nvw`)

**The smell.** Not a classic refactoring smell, but the reason the other fourteen matter.
The series closes with a tour of the disasters that sat in careful-looking code for
years: silent truncation, a unit mix-up, an off-by-one, and implicit trust. The framing
that makes this a *refactoring* lesson: each of those is a symptom of code that never
repaid its meaning, its types, or its assumptions.

**The tell in the snippet.** `FlightCalculator` contains four, one per class of failure,
and the comments confess them. **Silent truncation:** `private int speedMph` with
`totalSeconds` as an `int` that "will silently overflow". **Wrong units:** the field is
miles per hour, the constant is 200, and the comment says "200mph vs 200kph is a different
plane" — the comparison is meaningless because the threshold has no unit. **Division
blow-up:** `flightMinutes` divides by `speedMph` with no zero check, and its comment notes
the off-by-one. **Off-by-one:** `reach(max, climbRate)` returns `climbRate < max`, so
equality is silently excluded. **Implicit trust:** `altitudeWithin` trusts that callers
pass a non-negative `climbRate`, and nothing enforces it.

**The refactoring.** Four changes, each turning silent failure into loud failure. Types
widen to `long`. The threshold is converted explicitly: `speedMph > 200L * MPH_PER_KPH`
— the unit is now visible at the point of comparison. `flightMinutes` rejects a negative
distance and handles the remainder with a `+ 1` instead of truncating. `altitudeWithin`
rejects a negative rate and uses `<=` so equality is included. Each guard is one or two
lines; the point of the After panel is that **every one of those lines exists only because
the corresponding assumption was written down**.

**The metrics.** Code lines rise 23 → 26, fields 2 → 3, methods fall 6 → 4. The app's test
suite asserts that this lab's code does *not* shrink, and the assertion carries a comment
explaining why: the refactor trades lines for explicit validation, and a test demanding
shrinkage would push an author toward deleting the very checks the lab teaches. This is
the course's most counter-intuitive metric page, and it is worth an instructor's
attention: the number going up is the whole lesson.

**Teaching notes.** Close the course here deliberately. The learner's takeaway should not
be "smells are ugly" but "smells are how defects survive review". Suggested closing
discussion: for each of the four bugs, ask which *earlier lab* would have caught it if the
code had been refactored. Silent truncation is Lab 4 (Primitive Obsession). The unit
mix-up is Lab 4 again. The off-by-one is Lab 1 (Long Method — the condition was buried).
The unvalidated trust is Lab 7 (Inappropriate Intimacy). The course ends by showing that
its own fifteen lessons are one argument.

**The trap.** Treating guards as noise. The instinct to remove an `if (speedMph <= 0)`
check because "the constructor already checks it" is exactly the reasoning that produced
the original bug. Bounds checks at the boundary of a public method are not redundancy;
they are the contract.

---

## Part 5 · Verification and the Defects It Caught

This part records what was actually checked, and — more usefully — what failed and why.
Four defects were found during verification. Three were of different species, and the
fourth is the most interesting because it was a *pedagogical* defect rather than a
technical one.

### 5.1 The verification pass

Verification was not a single smoke test. It was, in order:

1. `mvn -q -B compile` — the Java builds.
2. `mvn -q -B test` — the metric assertions hold.
3. `docker compose build` and `up -d` — both images build, both containers reach healthy.
4. A loop over sixteen URLs (`/` plus all fifteen `/lab/{slug}`) asserting HTTP 200. The
   first run of this loop reported **fifteen 500s and one 200**.
5. A quiz round trip: correct answers, wrong answers, and half-right answers, checking for
   the right verdict *and* the right hint text.
6. A direct database query to confirm the rows landed.
7. A metrics sweep across all fifteen labs, printing before/after for every counter, to
   check that the numbers on screen tell the story the lesson claims.

Steps 4, 5 and 7 each found something. That ratio is the argument for having all three.

### 5.2 Defect one: malformed fragment expressions (all fifteen labs, HTTP 500)

**Symptom.** Every lab page returned 500; only the home page rendered. The logs named
`lab.html`, line 41, column 34, with a `ParseException` for the expression
`~{partials/metrics :: metrics(table(${beforeMetrics}), label('Before'))}`.

**Cause.** The metrics table is included twice, with different arguments, via a
`th:fragment="metrics(m, label)"`. In Thymeleaf, the arguments *after* the fragment
name are expressions to evaluate and pass; they are not a nested function-call syntax.
`table(...)` and `label(...)` are not Thymeleaf constructs at all — they were a habit
from other template languages leaking in.

**Fix.** Replace with the correct fragment-invocation syntax: arguments are
`${beforeMetrics}, 'Before'` — first an expression, then a string literal. Both call
sites changed. After the fix, all sixteen pages returned 200.

**The lesson.** Thymeleaf's fragment syntax is a small language, and a small language
has a small number of ways to be wrong. The failure was loud and immediate — 500s on
fifteen pages with a line and column in the log — which is the good case. Worth noting
for the course's own maintainers: the home page rendered fine *because it does not use
fragments*, so a smoke test that only checks `/` would have shipped this.

### 5.3 Defect two: the metrics measured the wrong thing

**Symptom.** Not an error — a *lie*. On the Long Method lab, the panel reported
longest-method **11 → 9** across a refactor that takes a 33-line method and turns it into
a 6-line method calling named helpers. A master lab whose headline metric barely moves is
a broken lab, and the number on screen was not a measurement of method length at all.

**Cause.** The original `longestMethodOf` recorded the *index* of every line that looked
method-ish and took the maximum gap between consecutive indices. Method-*like* included
method *calls*, so a 33-line method containing calls was chopped into a dozen "methods" of
two or three lines each, and the reported maximum was the largest fragment. The metric was
measuring call sites, not methods.

**Fix.** Rewrote the analyzer to do what a person does when asked "how long is the longest
method": find each declaration, then walk forward counting brace depth until it returns to
zero. `isMethodDeclaration` now requires a matching parenthesis pair, a trailing opening
brace, a first word that is not a control keyword, and a modifier or `void` on the line —
so `if (...) {`, `for (...) {`, and `switch (...) {` are excluded and
`private BigDecimal shippingFor(...) {` is not. The corrected report for Long Method is
**33 → 8**, which is the number the lab needed all along.

**A second, quieter bug in the same class.** The set of built-in type names used to filter
`typeMentions` contained two duplicate entries (`List` and `Optional` each appeared
twice). `Set.of` rejects duplicates at class-initialisation, so this surfaced as
`NoClassDefFoundError` on ten of ten tests — a confusing symptom for what is
one duplicated word. Fixed by removing the duplicates.

**The lesson.** A metric is a claim about code, and a claim needs a test. The thirteen
tests in `SniffMetricsTest` were written partly to prevent a recurrence, and they encode
*direction* rather than exact values, so that a future edit to a snippet cannot pass by
accident. This is also the moment to record that the copy was checked against the numbers
(§5.5).

### 5.4 Defect three: a fragment whose attribute value contained its own quote character

**Symptom.** The quiz POST returned HTTP 500 with an empty body, for correct answers,
wrong answers, and everything in between. Logs: `check.html`, line 10, column 71,
`Malformed markup: Attribute "+" appears more than once in element`.

**Cause.** The verdict fragment rendered the learner's chosen option as
`th:text="'you said " + ${identifyGiven} + '"'`. The intent was to build a string with
double quotes around the answer. The problem is the *attribute* delimiter: the element
uses double quotes, and the expression was written so that the literal's double quote
terminated the attribute early. The parser then read the following `+` as a second
attribute of the same element and rejected the tag.

**Fix.** Rephrase the sentence so the expression needs no embedded quote characters:
`th:text="'you said: ' + ${identifyGiven}"`. The rendering is arguably better anyway.

**The lesson.** Two related traps in one: template attribute delimiters, and *inventing
synthetic data for display*. The learner's answer does not need quotation marks to be
legible. When a template expression grows awkward, the usual cause is that the view is
trying to do presentation work — and the fix is to change what it renders, not to escape
harder. Worth adding to a maintainer's checklist: if an expression needs concatenation and
quoting, ask whether the sentence could be simpler.

### 5.5 Defect four: lesson copy that contradicted the app's own numbers

**Symptom.** Nothing failed. Every page rendered, every test passed, and four labs
*taught something false*.

**The detail.** After the metrics analyzer was corrected, the numbers were dumped for all
fifteen labs and compared against the "why" prose in `SmellRegistry`. Four claims did not
survive:

- **God Class** claimed "watch field count and longest method drop". Fields drop
  (7 → 3), but longest method does not (7 → 7) and code lines *rise* (51 → 78).
- **Data Class** claimed "method count on the data class rising while type mentions in
  services fall" — which is right, but the surrounding text implied a shrinking file, and
  code lines are flat (41 → 42).
- **Class Obsession** claimed "depth (longest method), file count, and type mentions all
  fall". Type mentions fall (8 → 4), but longest method *rises* (3 → 5) because the
  flattened version has real behaviour where the museum had concatenation in leaves.
- **Inappropriate Intimacy** claimed "field count on both sides and type mentions drop
  together". Fields *rise* (8 → 9), because immutability costs declarations.

**Fix.** All four were rewritten to state what actually happens, and each rewrite turned
out better than the original because it became *specific*: "the seven fields do not
shrink, they disperse" is more instructive than "field count drops", because it names the
mechanism. A fifth case was handled differently: the Switch Statements lab claimed "the
dashboard counts switch branches before and after" — a promise the app had never made.
Rather than soften the sentence, the missing metric was implemented: `branchCount` now
counts `switch`/`case`/`default` lines, and the lab reports **15 → 5**.

**The lesson, and the reason this defect is in the document at length.** Technical
defects announce themselves. A prose defect is invisible to every automated check in the
build; it ships, it teaches, and it erodes the learner's trust in everything else the
application says. The procedure that caught these was mundane: *make the app print its
numbers, then read the numbers and the sentences side by side.* Any educational artifact
that makes quantitative claims needs that pass, and it needs to be re-run whenever the
snippets or the metrics change. It is now encoded structurally as well: the tests pin
metric *direction*, so at least the relationship between lesson and number is guarded
going forward.

### 5.6 Smaller things, recorded for completeness

Three minor defects that did not survive to a build, listed because the record is more
useful than a clean sheet:

- **Garbled string literals in the registry.** The syllabus was written in one long
  expression, and four of its prose strings were malformed while being typed — an
  unterminated literal, a stray `" ("` in the middle of a sentence, a sentence fragment
  that had swallowed a word, and a typo in a verb. All four were found by the compiler and
  fixed. The lesson is mundane: a 300-line literal block in one expression is hard to
  proofread, and the compiler catches only the *syntactic* damage.
- **An invented helper method.** The Feature Envy "after" snippet initially called a
  non-existent `BigDecimal.when(boolean)`, which made the refactored code — the part of
  the lab learners are meant to copy — not compile. Rewritten with plain conditionals.
  Snippet quality is not a lesser concern: it is the only code a learner is likely to
  retype.
- **A model attribute that was never wired.** The layout's head fragment referenced a
  `playerName` attribute for pre-filling the quiz's name field, but no controller supplied
  it. Rather than thread a value through two controllers, the pre-fill was moved to the
  twelve-line `localStorage` script described in §2.7. The lesson: a cosmetic convenience
  should not cost a model attribute and a cookie read.

---

## Part 6 · Assessment Design, in Full

The quiz is the spine of the course's measurement, so it deserves a complete account of
what it measures, what it deliberately does not, and how to read the results.

### 6.1 What a double-right means

Both questions correct. This is the unit of achievement because it certifies two
distinct things: that the learner **recognised** the smell in unfamiliar code, and that
they **connected** it to a structural change. A learner who scores the identify and fails
the refactor has learned that the pattern is familiar but not actionable — a common and
specific state, and one worth seeing. A learner who fails the identify has a vocabulary
gap, which is the cheapest gap in the course to close and the one most likely to be closed
by simply doing Labs 1–5.

### 6.2 Reading the per-lab table

Each lab shows attempts, identify-correct percentage, and refactor-correct percentage. The
instructor's use of this data is the interesting part, and four patterns are worth
naming in advance:

- **Low identify, high refactor.** The learner can *do* the refactor but does not trust
  their own diagnosis. Almost always fixed by having them articulate the tell before
  looking at the After panel.
- **High identify, low refactor.** The catalogue is memorised, the structure is not
  understood. This learner needs labs that ask "why does this cure fix it", which is what
  the "why" block on every page is for.
- **Low on both, in one lab only.** A specific snippet is confusing, or the smell is
  genuinely ambiguous. Check the snippet before blaming the learner.
- **Rising double-rights across the course.** The signal that the ordering worked: the
  early labs (extraction) are mastered first, and the later, subtler ones (intimacy,
  speculation) are mastered last. If the early labs are *not* mastered, the ordering has
  been undermined and the instructor should say so rather than pushing on.

### 6.3 What the assessment deliberately does not do

- **It does not time the learner.** No timers, no speed bonus. A course about reading
  code carefully should not reward reading it quickly.
- **It does not penalise wrong attempts.** They are recorded, not punished. The leaderboard
  rewards doubles; it never ranks failure. This is both a humane choice and a diagnostic
  one, since a hidden failure rate teaches the instructor nothing.
- **It does not score prose.** The "explain the refactor" activity lives in
  `lessons/exercises.md` and in the room, not in the app, because scoring prose reliably
  would require a rubric and a marker, and a rubric-free automated score would be theatre.
- **It does not compare learners on raw attempt counts.** Counts rise with cleverness
  (clicking through every lab twice) and the leaderboard's ordering is by doubles, which
  requires reading the code to inflate.

---

## Part 7 · The Nine-Input Framework, As Applied

The commission supplied `OLD.txt`: a lesson plan is a system, not a prompt, and a system is
defined by its inputs. The nine inputs and how each was answered:

| Input | How it was answered | Where |
|---|---|---|
| **Learning goal** | Identify 15 smells from real code, choose the correct refactoring for each, explain *why* it improves the structure, and demonstrate it with metrics and a diff. | `lessons/lesson-plan.md` §1 |
| **Lesson sequence** | Fifteen labs in pedagogical order (master skill → object shape → relationships → proportion → consequences), with pacing and rationale for the reordering. | `lessons/lesson-plan.md` §2; this document §1.4 |
| **Assessment evidence** | Two-question quiz per lab with persisted double-rights; leaderboard and per-lab accuracy as instructor diagnostics; metrics and diff as analytical evidence. | §6; `partials/scoreboard.html` |
| **Learner profile** | Intermediate Java developers (1–3 years) who prefer tight loops: small example, visible evidence, immediate test of a hypothesis. | `lessons/lesson-plan.md` §4 |
| **Prior knowledge** | Java syntax; the basic refactoring vocabulary (Extract Method, Extract Class, Move Method); cohesion and coupling intuition; reading diffs. | `lessons/lesson-plan.md` §5 |
| **Learning activities** | Preview → read Before → compare metrics → read After → read diff → quiz → scoreboard check, per lab, in that order. | `lessons/lesson-plan.md` §6; this document §1.5 |
| **Output requirements** | Runnable locally and in Docker; snippets in a known layout; attempts persisted with indexes; app on 8084, db on 5435. | `lessons/lesson-plan.md` §7; `README.md` |
| **Accessibility & supports** | Line numbers on every snippet; side-by-side comparison; highlighted diff; multiple choice to reduce load; short declarative text; labs replayable in any order. | `lessons/lesson-plan.md` §8 |
| **Teacher decisions** | Pedagogical order over playlist order; "one common cure" rather than "the cure"; metrics declared as heuristics; every attempt saved; snippets kept tiny so the diff is obvious. | `lessons/lesson-plan.md` §9 |

The two inputs that changed the build most were **Lesson sequence** and **Assessment
evidence**. Sequence is why the labs are ordered the way they are rather than by video
date, and assessment is why the app has a database at all: an assessment that persists
produces a diagnostic instrument, and an instrument changes what an instructor can do in
a room.

---

## Part 8 · Running It, Extending It, and What It Is Not

### 8.1 Running the application

```bash
cd code-smells
docker compose up --build

# application   http://localhost:8084
# database      localhost:5435
# health        http://localhost:8084/actuator/health
```

The build prints its own progress and the app is healthy once the log shows
`Started CodeSmellsApplication`. The database container waits for the schema to be
applied before the app starts, so there is no race to lose.

To run without Docker, start any Postgres reachable at the URL in
`application.yml` (default `jdbc:postgresql://localhost:5435/postgres`, user
`smells_app`), apply `db/init.sql`, and:

```bash
mvn spring-boot:run
```

If the database is not running, the labs and the quiz still work and the scoreboard shows
its empty state. That is intentional (§2.6).

To rebuild after changing a snippet: `docker compose up -d --build app`. The image build
resolves dependencies in an earlier layer, so a snippet-only change rebuilds in roughly
fifteen seconds.

### 8.2 Adding a sixteenth smell

1. Create `src/main/resources/snippets/<new-slug>/Before.java` and `After.java`. Keep
   them tiny — twenty to eighty lines — so the diff stays legible.
2. Add one `s(...)` entry to `SmellRegistry` with a summary, a tell, a cure, a rationale,
   three identify options, and three refactor options. Choose the episode number where it
   belongs in the teaching order, not where it appeared in the playlist.
3. Add a test to `SniffMetricsTest` asserting that the metric the lesson claims will move,
   actually moves, in that direction.
4. Rebuild and check the lab page. Then read the page's prose next to its numbers — the
   check from §5.5.

No controller, template, or CSS change is needed. That is the payoff of §2.1.

### 8.3 What this application is not

Stating the boundaries is part of the teaching, so:

- **It is not a static analyser.** `SniffMetrics` counts lines. It does not parse Java, does
  not compute cyclomatic complexity, and will miscount on constructs it has not seen
  (lambdas with block bodies, text blocks, annotations with arguments, records with
  compact constructors). Its purpose is to give a learner a number they can reproduce by
  hand, not to certify a codebase.
- **It is not a replacement for the videos.** It is a companion. The summaries here are
  compressed; the videos carry the argument and the conviction, and a learner who wants
  the reasoning will get more from ten minutes of the video than from any summary.
- **It is not a Java compiler.** The snippets are illustrative. They are written to be read
  and copied, and while most are close to compilable in isolation, they reference
  collaborators (a `Mailer`, an `Inventory`, an `OrderLine`) that exist only in the
  reader's imagination. That is a deliberate simplification: a runnable sample would need
  six files of support types per lab, and the support types would be noise.
- **It is not a grading system.** It cannot assess whether a learner *understood*; it can
  only record what they chose. The exercises in `lessons/exercises.md` are where
  understanding is assessed, and they are assessed by a person.
- **It is not finished.** See below.

### 8.4 Honest limitations

Four limitations are worth stating plainly.

**The metrics are the weakest link.** They are heuristics, they are labelled as
heuristics, and §5.3 documents the time one of them was quietly wrong. A maintainer who
wants real analysis should replace them with a parser and treat the transition as a
version bump, because the lesson copy references specific movements.

**The snippets are small, and smallness is a limitation as well as a virtue.** They are
sized so the diff is obvious, which means they are not the sprawling, entangled
codebases where these smells actually live. A learner who has only met Long Method in a
thirty-line snippet may not recognise it in a two-hundred-line method. The remedy is the
exercises, which ask for variants — but a maintainer with access to a real legacy codebase
could add a lab per smell with production-shaped code, and that would be a significant
upgrade.

**The diff is line-based, not semantic.** It shows which lines changed, not which
behaviour was preserved. That is exactly enough for a refactoring lesson, and it is not
enough to teach that a refactoring is behaviour-preserving — which is a *behavioural*
claim, provable only by tests the app does not run. A future version could add the
assertion that the snippets compile and that the Before and After produce identical
results on a fixed set of inputs. That would be a genuinely valuable addition, and it is
the single highest-value improvement available to this project.

**The assessment has one item per skill.** One identify question and one refactor question
per lab is a thin measurement of a fifteen-lab course. Real assessment would use several
items per lab, ideally including some where the same code exhibits two smells at once —
which is the actual difficulty of the real world and which this course, by construction,
avoids.

### 8.5 Closing reflection

The most useful thing this build produced was not the application. It was the discovery
that **an educational artifact makes claims, and claims need the same discipline as code**.
Three of the four defects found in verification were technical and announced themselves.
The fourth — lesson copy that contradicted the app's own numbers — announced nothing at
all, passed every test, and would have taught four falsehoods to every learner who reached
those labs. Finding it required one act: making the application print its numbers, and
then reading the numbers and the sentences side by side.

The same discipline applies to the course's central claim. Code smells are not
aesthetic preferences. A god class is a class that appears in unrelated commits; a
temporary field is an object whose valid state depends on its call path; a hidden bug is
a latent defect that nobody was paid to look for. Each of the fifteen labs exists to
convert an aesthetic intuition into a structural observation, and the metrics, the diff,
and the quiz exist to make the observation *checkable* by the person learning it.

That is the whole design. Fifteen snippets, thirty code panels, one honest diff, a few
honest numbers, two questions per lab, and a scoreboard that keeps the receipts.

---

*Companion documents: `README.md` (orientation and run instructions),
`lessons/lesson-plan.md` (the nine-input plan), `lessons/exercises.md` (one exercise per
lab), `lessons/solutions.md` (the answer key with the key metric per lab).*

