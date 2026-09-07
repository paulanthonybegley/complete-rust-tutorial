# Chapter 5: Function & Return Statements
## From "Programming Thinking" by Visual Kernel — Walkthrough

### Learning Goal
Define reusable functions, understand parameters, return values, and the role of functions in modular programming thinking.

---

## 1. Why functions

Functions let you **wrap logic into named, reusable units**. This is a core *programming-thinking* skill — instead of pasting the same code everywhere, you define it once and call it by name.

Program thinking benefits:
- **Reusability** — use the logic many times
- **Abstraction** — hide the "how" behind a clear name
- **Testing** — isolate & verify one piece of logic
- **Readability** — readable main flow, details tucked into functions

---

## 2. Defining a function

```python
def function_name(param1, param2):
    # body
    return result
```

```python
def add(a, b):
    return a + b
```

- `def` starts the definition
- Parameters go in parentheses
- The body is indented
- `return` sends a value back to the caller

---

## 3. Parameters vs arguments

```python
def greet(name):        # `name` is a PARAMETER
    return f"Hello, {name}!"

greet("Alice")          # "Alice" is an ARGUMENT (the value passed in)
```

- **Parameters** are the names in the definition.
- **Arguments** are the actual values passed when calling.

---

## 4. The `return` statement (crucial)

`return` does two things:
1. **Stops** the function immediately.
2. **Sends a value** back to the caller.

```python
def classify(score):
    if score >= 90:
        return "A"        # early return: exits here
    elif score >= 80:
        return "B"
    else:
        return "F"
```
Multiple `return`s = **early exits** at different points.

> **Without `return`:** a function returns `None`. If you forget `return`, your function silently gives back `None`:
> ```python
> def f():
>     print("no return")   # returns None implicitly
> ```

---

## 5. Return values can be anything

```python
def stats(nums):          # returns a tuple
    return min(nums), max(nums)

low, high = stats([3, 1, 4, 1, 5])   # destructure the returned tuple
# low=1, high=5
```

---

## 6. Functions are like "mini machines"

The video's mental model: a function is a machine that takes **inputs** (parameters), does work, and produces an **output** (return value).

```
input(s) ──> [  function body  ] ──> return value
```

Thinking in these "input → process → output" units is what lets you decompose a big problem into manageable pieces.

---

## 7. Fitting with the other chapters

- Functions + **variables** (Chapter 1) — parameters are local variables
- Functions + **control flow** (Chapter 2) — `if`/`elif` inside the body
- Functions + **lists** (Chapter 3) — lists are commonly passed in/returned
- Functions + **references** (Chapter 6) — how lists behave when passed in

---

## 8. Activity

1. `def temperature_f_to_c(f)` that returns Celsius.
2. `def describe_grade(score)` with the grade table (A/B/C/F).
3. `def min_max(nums)` returning a tuple, and destructure it.
4. Write both a version *with* `return` and one *without* `return`; observe the `None` difference.

---

## 9. Assessment

1. **Explain** what `return` does (two effects).
2. **Predict** the output:
   ```python
   def mystery(x):
       if x > 0:
           return "pos"
       return "non-pos"
   print(mystery(5), mystery(-1))
   ```
3. What does a function return if it has **no** `return` statement?

---

## How to run this chapter's examples

```bash
python3 main.py       # working demonstration
python3 exercise.py   # complete the TODOs
```
