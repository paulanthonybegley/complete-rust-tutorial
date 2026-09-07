# Chapter 3: Python List — Your First Reference Type
## From "Programming Thinking" by Visual Kernel — Walkthrough

### Learning Goal
Understand the Python list as a collection, and start building the crucial **mental model** that lists are stored by *reference*, not copied.

---

## 1. What is a list?

A list is an **ordered collection** of items (called **elements**). It's one of Python's most flexible data structures.

```python
grocery_list = ["milk", "eggs", "bread"]
scores = [98, 85, 72, 90]
mixed = [1, "hello", 3.5, True]    # lists can hold different types
empty = []
```

- Lists are **mutable** — you can change their contents
- Lists are **ordered** — order is preserved (unlike sets/dicts)
- Lists can be **nested** — a list inside a list
- A single item in a list is called an **element** or **item**

---

## 2. Indexing & slicing

```python
scores = [98, 85, 72, 90]

scores[0]    # 98   (first item)
scores[1]    # 85
scores[-1]   # 90   (last item)
scores[1:3]  # [85, 72]  (slice: indices 1 up to but not including 3)
```

> **Note:** Indexing uses square brackets `[]`. Indexing starts at **0**.

---

## 3. Creating a list with initial data

```python
# Empty then append
nums = []
nums.append(10)
nums.append(20)

# Or initialize directly
nums = [10, 20]
```
When you put initial data, **commas separate the elements**. The elements can be any objects.

---

## 4. THE MENTAL MODEL SHIFT ⚠️

This is a key moment in the video:

> *"The variable `my_list` does **not** store the entire list, which is what everyone expected. Rather, it stores an **arrow** which references the actual list. Why? Strange, indeed. In the later chapter called value versus reference, we'll have a much more in-depth conversation about this."*

**Simple values (number, string, boolean):** the variable stores the **actual data**.
**List (and dict) variables:** the variable stores a **reference** (arrow) to the data, which lives elsewhere in memory.

For now, just **hold this mental image**:
```
number = 7          number ──> 7            (data stored directly)

my_list = [1, 2]    my_list ──> [1, 2]      (data stored elsewhere; variable holds arrow)
```

We fully explore *why* in **Chapter 6 — Value vs Reference**.

---

## 5. Common list operations

| Operation | Code | Description |
|-----------|------|-------------|
| Length | `len(lst)` | Number of elements |
| Append | `lst.append(x)` | Add to end |
| Insert | `lst.insert(i, x)` | Insert at index |
| Remove by value | `lst.remove(x)` | Remove first matching |
| Remove by index | `lst.pop(i)` | Remove & return |
| Clear | `lst.clear()` | Empty the list |
| Sort | `lst.sort()` | Sort in place |
| Reverse | `lst.reverse()` | Reverse in place |
| Index of | `lst.index(x)` | Position of x |
| Count | `lst.count(x)` | Occurrences of x |

```python
menu = ["coffee", "tea", "pastry"]
menu.append("cake")           # ["coffee","tea","pastry","cake"]
menu.insert(1, "juice")       # insert at position 1
menu.remove("tea")            # remove by value
last = menu.pop()             # "cake"; menu loses last item
```

---

## 6. Mutable by nature

Because lists are mutable and stored by reference, **changes persist** even when you pass a list to a function — a preview of reference semantics in Chapter 6.

---

## 7. Activity

1. Create a shopping list with at least 5 items; print its length.
2. Use `append`, `insert`, `remove`, and `pop`, printing the list after each.
3. Access the first, last, and a middle element using both positive and negative indexing.
4. Slice the list and print the result.

---

## 8. Assessment

1. **Predict** the output:
   ```python
   nums = [1, 2, 3]
   nums.append(4)
   print(len(nums))        # ???
   print(nums[-1])         # ???
   ```
2. What does `nums[1:3]` return if `nums = [10, 20, 30, 40]`?
3. **In your own words**, explain the "arrow" mental model: does a list variable store the list itself, or a reference to it?

---

## How to run this chapter's examples

```bash
python3 main.py       # working demonstration
python3 exercise.py   # complete the TODOs
```
