# Chapter 2: If, Else & Elif — Control Flow
## From "Programming Thinking" by Visual Kernel — Walkthrough

### Learning Goal
Make your programs **decide** between paths using `if`, `elif`, `else`, and understand how control flow is a core programming-thinking skill.

---

## 1. Why control flow

Variables are how we *store* data; control flow is how we *make decisions* based on it. The ability to branch execution is one of the most fundamental "thinking" tools in programming.

---

## 2. The `if` statement

```python
if condition:
    # code to run if condition is True
    ...
```
The `condition` is a **boolean** expression. Indentation (4 spaces) defines the block.

```python
age = 15
if age >= 18:
    print("You are an adult")
```
If `age` were 15, nothing prints — the block only runs when the condition is `True`.

---

## 3. `if` / `else` — two paths

```python
if condition:
    # path A
else:
    # path B
```

```python
age = 15
if age >= 18:
    print("adult")
else:
    print("minor")   # runs because 15 < 18
```

---

## 4. `elif` — multiple branches

`elif` = "else if". It checks the next condition only if the previous ones were False.

```python
score = 75
if score >= 90:
    grade = "A"
elif score >= 80:
    grade = "B"
elif score >= 70:
    grade = "C"        # 75 falls here
else:
    grade = "F"
print("grade:", grade)  # C

---

## 5. Comparison & logical operators

| Operator | Meaning |
|----------|---------|
| `==` | Equal |
| `!=` | Not equal |
| `<`, `>`, `<=`, `>=` | Comparisons |
| `and` | Both must be True |
| `or` | At least one True |
| `not` | Negate |

```python
if age >= 13 and age <= 19:
    print("teenager")
if not weekday:
    print("weekend")
```

---

## 6. The mental model

Think of control flow as a **flowchart** / decision tree:
```
        ┌─ [score >= 90] ──> A
score ──┼─ [score >= 80] ──> B
        ├─ [score >= 70] ──> C
        └─ [else] ─────────> F
```
Each branch is a "router". Programming thinking = anticipating all the branches your data can take.

---

## 7. Activity

1. Write a program that takes a number and prints whether it is **negative**, **zero**, or **positive** (use `if/elif/else`).
2. Write a **leap year** checker: divisible by 4, but not by 100 unless also divisible by 400.
3. Write a **grade** function using the table above, and test several scores.

---

## 8. Assessment

1. **Predict** the output of:
   ```python
   x = 5
   if x > 3:
       print("A")
   if x > 2:
       print("B")
   else:
       print("C")
   ```
   > *Hint: two separate `if` statements — both can run!*
2. Rewrite a nested `if` as an `elif` chain.
3. Explain **why order matters** in an `elif` chain.

---

## How to run this chapter's examples

```bash
python3 main.py       # working demonstration
python3 exercise.py   # complete the TODOs
```
