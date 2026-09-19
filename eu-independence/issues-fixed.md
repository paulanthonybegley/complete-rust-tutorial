# Issues Fixed — European Independence Lab

> Log of every bug found and fixed while building the interactive course app,
> with the reproduction, root cause, resolution and how each fix was verified.
> Written so "future me" (and any reader) can reuse the pattern, not just the
> patch.

## Table of contents

1. [Policy card "Open" button does nothing](#1-policy-card-open-button-does-nothing)
2. [Policy cards and quiz questions render twice](#2-policy-cards-and-quiz-questions-render-twice)
3. [Template parse errors: page loads throw Thymeleaf exceptions](#3-template-parse-errors-page-loads-throw-thymeleaf-exceptions)
4. [Property-style vs method-style enum access](#4-property-style-vs-method-style-enum-access)
5. [EnumMap lookup fails in templates](#5-enummap-lookup-fails-in-templates)

---

## 1. Policy card "Open" button does nothing

**Reported by:** user during review — clicking **Open →** on any card in the
policy explorer produces no visible result.

### Symptom

The htmx request fires (server returns `200` with the detail HTML) but the
drawer stays closed; nothing is displayed. The network call works, the
presentation layer fails.

### Root cause

A version/API mismatch on the exact thing that was supposed to open the drawer.
The button handed the "open the drawer" job to an htmx event listener:

```html
<button class="btn open"
        hx-get="/policies/detail?id=…"
        hx-target="#policy-detail"
        hx-swap="innerHTML"
        hx-on:htmx:after-swap="…classList.add('open')">
```

But the vendored `htmx.min.js` (a **2.0.7** build, confirmed via
`version:"2.0.7"` in the file) dispatches its events in **camelCase**:

```js
// from the vendored htmx.min.js
ae(e, "htmx:afterSwap", g.eventInfo)
```

The handler listened for the **kebab-case** name `htmx:after-swap`. The event
name never matched, so `after-swap` never fired, so the `open` class was never
added — and the CSS keeps the drawer at `max-height: 0; overflow: hidden`.

### Resolution

Removed the dependency on event-name wiring entirely. The button now swaps the
whole drawer element with `outerHTML`, and the detail response _carries its own
open state_:

```html
<!-- button -->
<button class="btn open"
        hx-get="/policies/detail?id=…"
        hx-target="#policy-detail"
        hx-swap="outerHTML">Open →</button>
```

```html
<!-- /policies/detail response -->
<div id="policy-detail" class="drawer open"> … </div>
```

There is no `hx-on` anywhere; the drawer appears because the response is
already open. The **✕ close** button keeps working (plain inline JS that strips
the `open` class and clears the content).

### Verification

- New test `policyDetailOpensTheDrawerForHtmx` asserts the response contains
  `class="drawer open"` → **22/22 tests pass**.
- Live smoke test over HTTP shows the button markup
  (`hx-swap="outerHTML"`, no `hx-on`) and the open-drawer response
  `id="policy-detail" class="drawer open"`.

### Lesson

Never wire UI state onto a specific htmx event string without checking the
vendored build's actual event names — and prefer **state-in-the-response**
over **state-by-event**. If the HTTP response already encodes "open", the
client cannot get it wrong.

---

## 2. Policy cards and quiz questions render twice

### Symptom

Pages showed every card exactly twice: 42 flip toggles when 21 were expected,
24 quiz cards when 12 were expected, twice the expected policy cards.

### Root cause

Thymeleaf **renders the bodies of `th:fragment` definitions when a template is
rendered as a whole page**. A template that both *defined* its fragments and
*inserted* them (looping over the same data) emitted every fragment twice —
once at the `th:insert` point and once at the fragment definition.

This error was invisible to the string-based tests (a duped card still contains
the same strings) — only DOM node counting exposed it.

### Resolution

Moved **every** fragment into a single dedicated `_fragments.html` file that is
never served as a page, and referenced them as `~{_fragments :: name}`. All
controllers' fragment return strings updated to match. Unique fragment names
remove the `grid`/`card`/`detail` name collisions.

### Verification

Live HTML node counts after the change:

| Page | Before | After |
|---|---|---|
| `/flashcards` flip toggles | 42 | 21 (20 deck + 1 spotlight) |
| `/quiz` question cards | 24 | 12 |
| `/policies` cards | 64 (`sort -u` hid it) | 32 (catalog size) |

### Lesson

Hunt for **duplicate output**, not just errors. Assert element counts, not just
substring presence.

---

## 3. Template parse errors: page loads throw Thymeleaf exceptions

### Symptom

Full-page loads failed with
`TemplateProcessingException: Exception evaluating SpringEL expression … on
null context object` for expressions like `'q-' + q.id()` and
`result['question']`.

### Root cause

Because fragment bodies are processed during a normal page render
(see issue 2), a fragment whose body referenced a context variable that is
absent on that page evaluated that variable against `null` and threw. The
`question`/`answered` quiz fragments only have their data when the *fragment*
endpoint is hit, not on the `/quiz` page render.

### Resolution

Guarded optional-data fragments with `th:if`:

```html
<th:block th:fragment="quizQuestion">
    <th:block th:if="${q != null}"> … </th:block>
</th:block>
```

### Verification

All per-page tests (dashboard, explorer, quiz, flashcards, lab, partners,
timeline, sources) stopped throwing and returned `200`.

### Lesson

`th:fragment` does not protect a fragment body from being evaluated during a
full-page render. Fragments that need optional context data must be explicit
about it (guard it), or live in `_fragments.html` with the guard.

---

## 4. Property-style vs method-style enum access

### Symptom

`Exception evaluating SpringEL expression: materials.grade().label()` — render
failure in the materials simulator.

### Root cause

The `ExposureGrade` enum defines `getLabel()`/`getReading()` (property style),
but the template called `label()`/`reading()` as methods. Spring EL method
invocation fails when no such method exists; for a JavaBean accessor the
correct template form is the **property** form `grade.label`.

### Resolution

Changed the template to `materials.grade().label` / `materials.grade().reading`
(record accessors like `grade()` keep their parens; the enum getter is a
property access).

### Verification

`/lab/materials?share=90` renders `CRITICAL` (test
`materialsSimGradesNinetyAsCritical`).

### Lesson

In Thymeleaf/Spring EL, records are called with parens (`figure.value()`), but
JavaBean getters are accessed as properties (`grade.label`). Check the actual
accessor style in the model before templating.

---

## 5. EnumMap lookup fails in templates

### Symptom

`Exception evaluating SpringEL expression: counts[c]` on the dashboard theme
tiles.

### Root cause

The dashboard model exposes an `EnumMap<Category, Integer>`; Spring EL's
compiled/index syntax `counts[c]` resolved `c` but the `MapAccessor` did not
index the enum map as expected in this configuration.

### Resolution

Called the map's own accessor instead: `counts.get(c)`.

### Verification

Dashboard renders all nine theme counts (test `dashboardShowsThesisAndMeter`).

### Lesson

When in doubt, prefer an explicit method call (`map.get(key)`) over the
Spring EL `map[key]` shorthand for enum-keyed maps in Thymeleaf.

---

## Scope

| File | Role |
|---|---|
| `app/src/main/resources/templates/_fragments.html` | All fragments moved here; Open button + drawer reworked (issues 1–3) |
| `app/src/main/resources/templates/{policies,timeline,lab,quiz,flashcards,partners}.html` | Relocated fragment references; insert `~{_fragments :: …}` |
| `app/src/main/java/com/example/euind/web/*Controller.java` | Fragment return strings → `_fragments :: …` |
| `app/src/test/java/com/example/euind/EuIndependenceApplicationTests.java` | 22 tests, incl. drawer-open and count-based coverage |
| `education.md`, `README.md` | Existing docs; this file is the resolution log |