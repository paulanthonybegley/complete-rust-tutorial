# Chapter 1: Variable — The Equal Sign is a Verb, Not a Noun
## From "Programming Thinking" by Visual Kernel — Walkthrough

### Learning Goal
Reshape your mental model of the `=` sign: in programming it is an **action (verb)**, not a mathematical statement (noun).

---

## 1. Why we need to unlearn math

When you first saw `1 + 1 = 2` in grade school, the `=` sign described a **static math expression**. This interpretation is buried deep in our subconscious.

In programming, `=` means something **completely different**:

```python
x = 5
```

- It's a **verb** — an **action you perform** as a programmer.
- It doesn't *describe* a relationship; it **binds a name** (`x`) to a value (`5`).
- The value is **stored in memory**, and `x` is how you refer to it later.

> **Mental model shift:** `=` is not "equals". It is "let x refer to 5", or "assign 5 to x".

---

## 2. Assignment is NOT equality

In math: `x = x + 1` is nonsense.
In programming: `x = x + 1` is a valid, meaningful action.

```python
x = 0          # x refers to 0
x = x + 1      # read current x (0), add 1, bind x to 1
x = x + 1      # now x = 2
```
Each line **reads** the current value, computes, then **rebinds** the name. The `=` performs an assignment *at that moment in time*.

---

## 3. Variables are "flexible containers"

Python variables are **dynamic** — a variable is not typed; it can hold **any data type**, and what it holds can **change** during execution:

```python
x = 5          # x holds an integer
x = "hello"    # now x holds a string  (fine in Python!)
x = [1, 2, 3]  # now x holds a list     (fine in Python!)
```
The same name `x` can refer to a number, then a string, then a list. The variable is just a *name for a memory location*.

---

## 4. The "diagnosis/doctor" analogy

The video uses a character/doctor example: a variable's current **value** is like a patient's current reading at a moment in time, and each **assignment** updates that reading. The *name* stays the same; the *value* changes.

Think of a whiteboard slot labeled `x`. Writing `x = "hello"` erases whatever was there and writes `"hello"` — and the type doesn't need to be declared.

---

## 5. Simple vs complex data

A key preview (expanded in Chapter 6 — Value vs Reference):

```python
number = 7         # simple value — the actual data is "stored in" the variable
my_list = [1, 2]   # list — the variable only stores a REFERENCE/arrow to the list
```

Right now: just **remember the mental image** — simple data (number, string, boolean) lives directly in the variable; complex data (list, dict) lives elsewhere and the variable holds a pointer to it (the "arrow"). We'll deeply explore *why* in Chapter 6.

---

## 6. Activity

Write a Python script that:
1. Bind `temperature = 20`
2. Print it
3. Reassign `temperature = 25` and print again — observe the same name, new value
4. Assign the *same* variable a **string**, then a **list**, and print each — confirm Python variables are type-flexible

---

## 7. Assessment

1. **Explain** the difference between the math `=` and the programming `=`.
2. **Predict** the output of this:
   ```python
   n = 1
   n = n + 1
   n = n + 1
   print(n)   # ???
   ```
3. True/False: A Python variable can hold different data types during one program's execution.

---

## How to run this chapter's examples

```bash
python3 main.py    # working demonstration
python3 exercise.py  # complete the TODOs
```
