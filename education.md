# Education Log — Rust Course Build

> Comprehensive documentation of every step taken in this project, written for "future me" (and anyone else) to learn from, reuse, or pick up where I left off.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [The Source Material](#2-the-source-material)
3. [Step 0 — Reading an Image Without Image Support (OCR)](#3-step-0--reading-an-image-without-image-support-ocr)
4. [Step 1 — Analysing the Video](#4-step-1--analysing-the-video)
5. [Step 2 — Creating Structured Lesson Plans](#5-step-2--creating-structured-lesson-plans)
6. [Step 3 — Building the Lessons Folder](#6-step-3--building-the-lessons-folder)
7. [Step 4 — Verifying the Code](#7-step-4--verifying-the-code)
8. [Step 5 — Git & Publishing](#8-step-5--git--publishing)
9. [Final Structure](#9-final-structure)
10. [Lessons Learned](#10-lessons-learned)
11. [Where to Go Next](#11-where-to-go-next)
12. [Quick Reference — Commands](#12-quick-reference--commands)

---

## 1. Project Overview

**Goal:** Turn a YouTube video about Rust into a complete, structured, runnable educational resource.

**Tools & technologies used:**
- **Tesseract OCR** (via Homebrew) — extract text from an image when the AI model can't read images
- **Web search / fetch** — analyse a YouTube video's content
- **Rust toolchain** (`rustc` 1.91.1, `cargo`) — test that every code example compiles and runs
- **VS Code / markdown** — authoring deliverables

**Deliverables produced:**
| File / Folder | Description |
|---------------|-------------|
| `lesson_plans.md` | High-level structured lesson plans for all 15 lessons |
| `lessons/` | Per-lesson walkthrough READMEs + runnable code + exercises |
| `linkedin-poat.md` | A social-media post announcing the achievement |
| `education.md` | This document |

---

## 2. The Source Material

**Primary inputs:**
1. **`OLD.jpg`** — an image containing a "lesson planning framework" (9 design inputs). Named `OLD` (vs `NEW`) because it contrasted two approaches to AI-generated worksheets.
2. **YouTube video:** `https://www.youtube.com/watch?v=tS1PFlTmpuU` — *"Rust Course 2026 – Complete Rust Programming for Beginners"* by **Codynn** (~1h17m, 14 chapters).

**The video's 14 chapters:**
1. Introduction
2. Setup & Environment
3. Cargo & Hello World
4. Variables & Mutability
5. Data Types
6. Functions
7. Control Flow
8. Ownership
9. Borrowing & References
10. Structs
11. Enums & Pattern Matching
12. Option, Result & Error Handling
13. Collections (Vec, String, HashMap)
14. Traits & Generics

---

## 3. Step 0 — Reading an Image Without Image Support (OCR)

**The problem:** The task said "analyse this video and create structured lesson plans for all concepts illustrated using file `OLD.jpg`." But the active AI model **does not support image input** — it returned an error: `ERROR: Cannot read image (this model does not support image input).`

**Options considered (in order of user preference):**
1. Ask the user to describe the image
2. User types the content manually
3. **Use OCR** ← *chosen by user*

**What I did (and the exact commands):**

1. Installed Tesseract OCR:
```bash
# Check if already installed
which tesseract

# Install via Homebrew if missing
brew install tesseract
```
> Note: Homebrew's default `tesseract` contains only `eng`, `osd`, and `snum` language data. Install `tesseract-lang` if you need other languages.

2. Ran OCR on the image:
```bash
tesseract OLD.jpg /var/folders/.../old_ocr_output
```
This produces a `.txt` file (`old_ocr_output.txt`). I then read it.

**Result:** The OCR extracted the 9-part lesson-planning framework:
1. **Learning Goal** — clarifies what success looks like
2. **Lesson Sequence** — sets flow and pacing
3. **Assessment Evidence** — defines how learning is shown
4. **Learner Profile** — shares who the learners are
5. **Prior Knowledge** — reveals what learners already know
6. **Learning Activities** — names the tasks learners will do
7. **Output Requirements** — specifies formats/constraints
8. **Accessibility & Supports** — plans inclusive options
9. **Teacher Decisions** — shares preferences/priorities

> **Core tagline from the image:** *"A lesson plan is a system, not a prompt."*

---

## 4. Step 1 — Analysing the Video

**The problem:** I needed the video's chapter list and content outline. Fetching the raw YouTube page returns only a tiny amount of visible text (page loads content via JS).

**What I did:**
- `webfetch` on the YouTube URL (returned title + description snippet only)
- `websearch` for the video title → found the full **14-chapter breakdown** in a search summary

**Key finding:** The search result gave me the complete chapter list shown in Section 2.

> **Lesson:** For YouTube content analysis, a targeted web search often reveals the chapter/topic list faster than scraping a single page. Use `websearch "topic full title"` before falling back to `webfetch` on the raw HTML.

---

## 5. Step 2 — Creating Structured Lesson Plans

**Output file:** `lesson_plans.md`

**Approach:** Combined the two inputs:
- The **9-domain framework** from `OLD.jpg` (used for administrative structure: objective, teaching points, assessment, learner profile, accessibility)
- The **14 Rust topics** from the video (the actual content)

**For each Rust topic**, I wrote:
- **Objective** — what success looks like
- **Key Teaching Points** — the essential concepts
- **Activity** — an assigned hands-on task
- **Assessment / Assessment Evidence** — how learning is demonstrated
- **Learner Profile / Prior Knowledge / Accessibility / Teacher Decisions** — where applicable

**[lesson_plans.md]** followed a template structure:
```
## Lesson N: <Title>
**Objective:** ...
**Key Teaching Points:** <bulleted list>
**Activity:** <task>
**Assessment:** ...
<optional extra fields>
---
```

**Deliverable:** 14 lessons + a final "Integration Project".

---

## 6. Step 3 — Building the Lessons Folder

**Goal:** Create runnable teaching artifacts — not just prose.

**Directory created:**
```bash
mkdir -p lessons/lesson-01-introduction ... lessons/lesson-15-integration-project
```

**Per-lesson artifact pattern** — each topic folder contains (as applicable):

| File | Purpose |
|------|---------|
| `README.md` | The walkthrough guide (teaching points, code explanations, activity, assessment, "how to run") |
| `main.rs` / `examples.rs` / `verify.rs` | Full, **runnable** demonstration of the topic |
| `exercise.rs` | Practice file with `TODO` comments for the learner to complete |
| `errors.rs` | (Lessons 4 & 8) deliberately-broken code to train reading compiler errors |

**Key authoring decisions:**
- Used a **consistent per-file template** so future me can extend easily.
- `main.rs` files are **fully working** (verified independently in Section 7) so learners have a reference.
- `exercise.rs` files use a **"comment out the broken/TODO, uncomment to complete"** pattern — the file compiles once TODOs are filled.
- `errors.rs` files put broken code inside `/* ... */` block comments so the file itself still compiles; learners uncomment one function at a time to see errors.
- Lessons 1–2 are more conceptual (no full program), so they use `examples.rs`/`verify.rs` instead.
- The integration project (`lesson-15`) is the capstone combining everything.

**Instructor note embedded in READMEs:** Each README begins with `## Learning Goal` (mapping to the framework) and ends with a `## How to run this lesson's examples` section.

---

## 7. Step 4 — Verifying the Code

**Critical best practice:** never ship code you haven't compiled. "If it's not tested, it doesn't work."

**1. Check the toolchain exists:**
```bash
which cargo rustc rustup && cargo --version   # cargo 1.91.1
```

**2. Compile every `main.rs`:**
```bash
cd lessons
for d in lesson-*/; do
  if [ -f "$d/main.rs" ]; then
    rustc --edition 2021 "$d/main.rs" -o "$d/main.bin" && echo "OK $d" || echo "FAIL $d"
  fi
done
```
**Result:** All 14 `main.rs` files compiled. (Warnings were only harmless `dead_code` notes for unused demo fields/variants.)

**3. Run every binary and eyeball the output:**
```bash
for d in lesson-*; do ./"$d/main.bin"; done
```
**Result:** All outputs matched expected behavior (e.g., shadowing chain gives `12`, FizzBuzz correct, ownership moves correctly, word counts correct).

**4. Test the integration project with Cargo:**
```bash
cargo new wc_test
cp lessons/lesson-15-integration-project/main.rs wc_test/src/main.rs
cargo test --manifest-path wc_test/Cargo.toml
```
**Result:** `6 passed; 0 failed` — all six unit tests green.

**5. Clean up build artifacts:**
```bash
find . -name "*.bin" -delete
```

> **Lesson learned:** Creating a temporary Cargo project in a temp directory lets you run `cargo test` without polluting the real `lessons/` tree. Use `/var/folders/.../opencode` (the designated temp dir) or `/tmp`.

---

## 8. Step 5 — Git & Publishing

**Problem:** The directory wasn't a git repo yet.

```bash
# Initialize
git init

# Stage everything
git add -A

# Inspect
git status

# Commit
git commit -m "Add Rust course lesson plans and source image"
```
**Result:** Root commit `c9998e6` — 2 files: `OLD.jpg`, `lesson_plans.md`.

> Guide daily practice: *Only commit when the user asks.* Check `git status`/`git diff` before committing, stage only intended files, never commit secrets.

---

## 9. Final Structure

```
complete-rust-tutorial/
├── OLD.jpg               # source image (OCR'd)
├── lesson_plans.md       # high-level lesson plans
├── linkedin-poat.md      # social media post
├── education.md          # this document
└── lessons/
    ├── lesson-01-introduction/       README.md, examples.rs
    ├── lesson-02-setup/              README.md, verify.rs
    ├── lesson-03-cargo-helloworld/   README.md, main.rs
    ├── lesson-04-variables/          README.md, main.rs, errors.rs
    ├── lesson-05-data-types/         README.md, main.rs, exercise.rs
    ├── lesson-06-functions/          README.md, main.rs, exercise.rs
    ├── lesson-07-control-flow/       README.md, main.rs, exercise.rs
    ├── lesson-08-ownership/          README.md, main.rs, errors.rs
    ├── lesson-09-borrowing/          README.md, main.rs, exercise.rs
    ├── lesson-10-structs/            README.md, main.rs, exercise.rs
    ├── lesson-11-enums/              README.md, main.rs, exercise.rs
    ├── lesson-12-option-result/      README.md, main.rs, exercise.rs
    ├── lesson-13-collections/        README.md, main.rs, exercise.rs
    ├── lesson-14-traits-generics/    README.md, main.rs, exercise.rs
    └── lesson-15-integration-project/ README.md, main.rs
```

---

## 10. Lessons Learned

1. **Model limitations are workable.** "I can't read that image" is not a dead end — OCR (`tesseract`) (or asking the user) sidesteps it cleanly.
2. **Verify everything.** Compiling and running every example caught real issues and gave confidence that the course is usable as-is.
3. **Structure reduces ambiguity.** The lesson-planning framework from `OLD.jpg` gave a reusable template for every lesson — same shape, different content.
4. **Consistent file conventions scale.** `README` + `main.rs` + `exercise.rs` (+ `errors.rs`) pattern made 15 lessons uniform and easy to generate/extend.
5. **Search beats scraping for YouTube.** A targeted web search surfaced the full chapter list quickly.
6. **Temporary projects keep things clean.** Testing the capstone in a temp Cargo dir avoids polluting the deliverable tree.
7. **Best practices matter even solo:** only commit when asked; always check `git status`/`git diff` first.

---

## 11. Where to Go Next

Future extensions this project could take:
- [ ] **Add a completion checklist** or quiz per lesson.
- [ ] **Add a `solutions/` file** for the `exercise.rs` TODOs.
- [ ] **Add further test coverage** to more lessons (not just the capstone).
- [ ] **Package the course** — a root `index.md` linking all lessons in order.
- [ ] **Add an async Rust / lifetimes / smart-pointers** advanced section.
- [ ] **Video-to-lesson automation** — generalize the pipeline (OCR image → topic list → lesson artifacts) into a reusable script.

---

## 12. Quick Reference — Commands

```bash
# --- OCR an image (macOS) ---
brew install tesseract                # install (if not present)
tesseract input.jpg /path/to/output   # -> produces output.txt

# --- Verify Rust toolchain ---
which cargo rustc rustup
cargo --version

# --- Compile & run a single Rust file ---
rustc --edition 2021 file.rs -o file.bin && ./file.bin

# --- Compile every main.rs in a tree ---
for d in lessons/lesson-*/; do
  [ -f "$d/main.rs" ] && rustc --edition 2021 "$d/main.rs" -o "$d/main.bin" && echo "OK $d"
done

# --- Run the capstone's tests via Cargo ---
cargo new wc_test -q
cp lessons/lesson-15-integration-project/main.rs wc_test/src/main.rs
cargo test --manifest-path wc_test/Cargo.toml

# --- Clean up build artifacts ---
find lessons -name "*.bin" -delete

# --- Git ---
git init
git add -A
git status
git commit -m "message"
```

---

*Written for future me — February/summer 2026, on the "complete-rust-tutorial" project.*
