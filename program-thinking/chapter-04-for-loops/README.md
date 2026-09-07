# Chapter 4: For Loop — Iteration Superpower
## From "Programming Thinking" by Visual Kernel — Walkthrough

### Learning Goal
Use `for` loops to repeat work efficiently, including looping over ranges and nested loops.

---

## 1. Why loops are a "superpower"

Recall from the dictionary chapter: *"The reason we love Python lists is because it's an iterable, which allows us to run a for loop. At this point, we know that loops are basically a superpower in programming."*

Loops let you **repeat a task** without writing it over and over — and they're how programs process data at scale (e.g., scanning an entire book).

---

## 2. The basic `for` loop

```python
for item in collection:
    print(item)
```
For each item in the collection, the loop **binds** `item` to that element, runs the body, then moves to the next.

```python
fruits = ["apple", "banana", "cherry"]
for fruit in fruits:
    print(fruit)
# apple
# banana
# cherry
```

> **Mindset:** a `for` loop is "for each element, do this." The loop variable (`fruit` here) is **rebound** each iteration — just like the Chapter 1 variable concept.

---

## 3. `for` over a range — `range()`

`range(n)` produces the numbers `0, 1, ..., n-1`:

```python
for i in range(5):
    print(i)        # 0 1 2 3 4
```

`range(start, stop, step)`:
```python
for i in range(1, 6):        # 1 2 3 4 5
    print(i)

for i in range(0, 10, 2):    # 0 2 4 6 8
    print(i)
```
> `range` is often combined with `range(len(lst))` to get both index and value:
> ```python
> for i in range(len(scores)):
>     print(i, scores[i])
> ```

---

## 4. Looping with index and value

When you need the index *and* the value, use `enumerate`:

```python
menu = ["coffee", "tea", "pastry"]
for index, item in enumerate(menu):
    print(index, item)
# 0 coffee
# 1 tea
# 2 pastry
```

---

## 5. Nested loops

A loop inside a loop. For each iteration of the **outer** loop, the **inner** loop runs **completely**.

```python
for x in range(2):           # outer
    for y in range(3):       # inner
        print(x, y)
# 0 0, 0 1, 0 2   <-- inner completes for x=0
# 1 0, 1 1, 1 2   <-- then for x=1
```

**Real-world example — the multiplication table:**
```python
for row in range(1, 4):
    for col in range(1, 4):
        print(f"{row*col:2}", end=" ")
    print()      # newline after each row
```
Produces:
```
 1  2  3
 2  4  6
 3  6  9
```

**Total iterations** = outer × inner. Programmers must count this to estimate work.

---

## 6. `break` and `continue`

```python
for i in range(10):
    if i == 3:
        continue        # skip 3
    if i == 7:
        break           # stop entirely at 7
    print(i)            # 0 1 2 4 5 6
```

---

## 7. Real-world thinking

The video's character-counting example loops over **every word in a book**:
```python
for word in all_words:
    # check if word is a character name, tally it
```
The `for` loop is what makes processing a huge dataset fast and concise. This is "programming thinking" — recognizing that *repetition you'd never hand-write* is exactly what loops are for.

---

## 8. Activity

1. Print numbers 1–10 with a `for` loop over `range`.
2. Print even numbers 0–20 using `range(0, 21, 2)`.
3. Loop over a list of salads with `enumerate` and print "index: item".
4. Build a **multiplication table** (1–5) using a nested loop.
5. Count how many of the characters in a name list appear in a paragraph (preview of dictionary counting).

---

## 9. Assessment

1. **Predict** the output of `for i in range(3, 0, -1)`.
2. How many total times does the body run here?
   ```python
   total = 0
   for a in range(3):
       for b in range(4):
           total += 1
   print(total)   # ???
   ```
3. Write a loop that prints only the **even-indexed** items of a list.

---

## How to run this chapter's examples

```bash
python3 main.py       # working demonstration
python3 exercise.py   # complete the TODOs
```
