# Chapter 8: Recursion — Elegant Self-Reference
## From "Programming Thinking" by Visual Kernel — Walkthrough

### Learning Goal
Understand recursion — a function that calls itself — and visualize the call stack that makes it work.

---

## 1. What is recursion?

Recursion is when a **function calls itself**, either directly or indirectly, to solve a problem by breaking it into smaller versions of the same problem.

```python
def countdown(n):
    if n == 0:                 # base case: stop!
        print("Blast off!")
        return
    print(n)                   # do something
    countdown(n - 1)           # recurse on a smaller problem
```

A recursive function needs two parts:
1. **Base case** — the condition where it *stops* (prevents infinite recursion)
2. **Recursive case** — the call to itself with a smaller/simpler input

---

## 2. The call stack (how recursion really works)

Every function call places a **frame** on the **stack** (memory). Each recursive call adds **another frame** with its OWN local variables.

```python
def countdown(n):
    if n == 0:
        return
    print(n)
    countdown(n - 1)   # pushes a new frame for n-1
```

Calling `countdown(3)`:
```
stack (bottom → top)        printed
countdown(3)                3
countdown(3) countdown(2)   2
countdown(3) countdown(2) countdown(1)   1
countdown(3) countdown(2) countdown(1) countdown(0)   (base case, unwinds)
...frames pop in reverse...
```

> **Key insight:** each call has its **own** local `n`. They don't interfere. The stack naturally gives you "last in, first out" — each recursive call frames smaller and smaller until the base case is hit, then they unwind.

**If there's no base case → infinite recursion → `RecursionError` (stack overflow):**
```python
def bad():
    bad()        # RecursionError: maximum recursion depth exceeded
```

---

## 3. A classic — factorial

```python
def factorial(n):
    if n == 1:              # base case
        return 1
    return n * factorial(n - 1)   # recursive case

print(factorial(5))   # 5*4*3*2*1 = 120
```

Visualizing `factorial(5)`:
```
factorial(5) = 5 * factorial(4)
factorial(4) = 4 * factorial(3)
factorial(3) = 3 * factorial(2)
factorial(2) = 2 * factorial(1)
factorial(1) = 1              <- base case
```
Then the multiplications **unwind** on the way back up: `1 → 2*1 → 3*2 → 4*6 → 5*24 = 120`.

---

## 4. Thinking recursively — the "trust the smaller call" mindset

Recursion *feels* like magic until you reframe it. The programming-thinking trick:

> **Assume the recursive call already gives you the correct answer for a smaller input.** Then just combine it with the current step.

Example — sum of a list:
```python
def sum_list(nums):
    if not nums:                  # base case: empty list sums to 0
        return 0
    return nums[0] + sum_list(nums[1:])   # first + recursively sum the rest
```

---

## 5. Recursion vs loops

| | Loop (`for`/`while`) | Recursion |
|--|-----------------------|-----------|
| Style | Iterative | Declarative/self-referential |
| Stack usage | Constant | Grows with each call |
| Best for | Many problems | Naturally recursive structures (trees, nested data) |

Not every recursive problem *should* be recursive — but recognizing when a problem is **self-similar** (a big version contains smaller versions of itself) is a powerful thinking tool. Trees, directories, and nested hierarchies are naturally recursive.

---

## 6. Real-world recursive thinking

- **File systems** — a directory contains files and sub-directories (recursively)
- **Nested data** — JSON/HTML are deeply nested structures best processed recursively
- **Trees** — hierarchies (org charts, DOM, decision trees)
- **Divide & conquer** algorithms (quick sort, merge sort)

When you meet a tree-like or nested problem, recursion often leads to far more elegant code than nested loops.

---

## 7. Activity

1. Write `countdown(n)` (above) and trace the stack by hand.
2. Write `factorial(n)` recursively.
3. Write `sum_list(nums)` recursively (base + recursive case).
4. Write `fibonacci(n)` recursively (and note why it's slow without memoization).
5. Predict what happens with no base case (`RecursionError`).

---

## 8. Assessment

1. **Trace** `factorial(4)` — write out each recursive frame and the final unwound result.
2. What would happen if a recursive function had **no base case**?
3. **Identify** the base case and recursive case in:
   ```python
   def count_down(n):
       if n == 0:
           return
       print(n)
       count_down(n - 1)
   ```
4. **Thinking question:** Is a directory tree (folders inside folders) naturally recursive? Why?

---

## How to run this chapter's examples

```bash
python3 main.py       # working demonstration
python3 exercise.py   # complete the TODOs
```
