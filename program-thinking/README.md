# Programming Thinking — A Tutorial Course
## Applied from "Programming Thinking" by Visual Kernel

> **Core thesis of the course:**
> *"People say you don't need to write code anymore — you have AI. I agree with that. But if somebody says you don't need programming thinking because of AI, I couldn't disagree more."*

Programming thinking is the **mental model** that lets you harness the full potential of AI. Pure "vibe coders" plateau; programming thinkers build impactful projects. Even with AI writing code, you need to **think** — to ask better questions, spot wrong output, and choose the right approach.

This course teaches programming thinking using **Python**, one concept at a time.

---

## How to use this course

Each chapter folder contains:
- **`README.md`** — the walkthrough (teaching points, examples, activity, assessment)
- **`main.py`** — a working, runnable demonstration
- **`exercise.py`** — practice TODOs to complete

Run any example with:
```bash
python3 main.py
```

---

## Course map

| # | Folder | Topic | The key idea |
|---|--------|-------|--------------|
| 0 | `chapter-00-why-programming-thinking` | Why programming thinking matters (with AI) | The mental model beats vibe coding |
| 1 | `chapter-01-variables` | Variable | `=` is a verb, not a noun |
| 2 | `chapter-02-control-flow` | If, Else & Elif | Decide between branches |
| 3 | `chapter-03-lists` | Python List | Your first *reference* type |
| 4 | `chapter-04-for-loops` | For Loop | Iteration superpower |
| 5 | `chapter-05-functions` | Function & Return | Reusable, modular logic |
| 6 | `chapter-06-value-vs-reference` | Value vs Reference | The memory model that unlocks everything |
| 7 | `chapter-07-dictionaries` | Dictionary | Key–value power + real data analysis |
| 8 | `chapter-08-recursion` | Recursion | Elegant self-reference |

---

## Suggested order & rough pacing

1. **Chapter 0 & 1** (mental setup + variables) — ~30 min
2. **Chapters 2, 3, 4** (control flow, lists, loops) — build the fundamentals — ~1–2 hrs
3. **Chapters 5, 6** (functions + value vs reference) — the conceptual core — ~1–2 hrs
4. **Chapters 7, 8** (dictionary + recursion) — advanced power — ~1–2 hrs

---

## The connective thread — why each chapter builds on the last

- **Variables (1)** teach you that names *bind* to data — the foundation of everything.
- **Control flow (2)** lets your code *decide*.
- **Lists (3)** introduce *collections* and the crucial "arrow/reference" mental model.
- **For loops (4)** let you *repeat* over those collections — the superpower.
- **Functions (5)** wrap logic into reusable units.
- **Value vs Reference (6)** explains *why* lists/dicts behave differently from numbers — the key to avoiding bugs and reading AI output critically.
- **Dictionaries (7)** combine collections + loops + counting for real data analysis.
- **Recursion (8)** builds on functions + the call stack for self-similar problems.

---

## Verification

All `main.py` examples are verified to run correctly:
```bash
# From the program-thinking directory:
python3 chapter-01-variables/main.py
python3 chapter-02-control-flow/main.py
... etc for every chapter
```
