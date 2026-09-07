# Chapter 7: Python Dictionary — Key–Value Power
## From "Programming Thinking" by Visual Kernel — Walkthrough

### Learning Goal
Use the dictionary (`dict`) — one of Python's most useful data structures — to map keys to values, including nested dictionaries and real data analysis.

---

## 1. What is a dictionary?

A dictionary is a **collection of key–value pairs**. You look up a **value** by its **key** (like a real dictionary: word → definition, or a menu: item → price).

```python
menu = {
    "coffee": 3.5,
    "tea": 2.5,
    "pastry": 4.0,
}
```

Keys must be **unique**. Access values with square brackets:

```python
menu["coffee"]   # 3.5
```

---

## 2. Creating and accessing

```python
# Literal
scores = {"Harry": 90, "Ron": 85, "Hermione": 100}

# Access by key
print(scores["Harry"])     # 90

# Add / update
scores["Malfoy"] = 70      # add a new key-value pair
scores["Harry"] = 95       # update existing

# Safe access with .get()
score = scores.get("Luna")   # None if missing (no KeyError)
score = scores.get("Luna", 0)  # 0 if missing
```

> **`.get()`** is the safe way to access — it won't raise `KeyError` if the key is missing.

---

## 3. Adding, updating, removing

```python
menu = {"coffee": 3.5, "tea": 2.5}

menu["pastry"] = 4.0      # add
menu["coffee"] = 3.75     # update
menu.pop("tea")           # remove by key ("Shamrock removed from menu")
del menu["coffee"]        # also removes
```
`dict.pop(key)` removes a key–value pair and returns the value.

---

## 4. Dictionaries are versatile — nested values

The video emphasizes near-unconstrained freedom in **what can be a value**:

```python
# value is a number
{"a": 1}

# value is a string
{"name": "Alice"}

# value is a list
{"menu": ["coffee", "tea", "pastry"]}

# value is ANOTHER dict (nested)
{"Chick-fil-A": {"waffle fries": 2.49, "sandwich": 5.99}}
```

**Accessing nested dictionaries (chained lookups):**
```python
restaurants = {
    "Chick-fil-A": {"waffle fries": 2.49, "sandwich": 5.99},
    "Cafe":       {"coffee": 3.5},
}

price = restaurants["Chick-fil-A"]["waffle fries"]
# price = 2.49
```
The video's example:
> *"I do `restaurant` → `menu` with key Chick-fil-A and its value is the inner dictionary. Immediately after, I do another value access with key `waffle fries` to retrieve its price. Variable P binds to 2.49."*

---

## 5. THE KEY RESTRICTION: keys must be immutable

The video's important caveat:
> *"There's one restriction: the keys of a dictionary **cannot be a Python list or dictionary**."*

```python
# This raises an error:
# bad = { ["a"]: 1 }   # TypeError: unhashable type: 'list'
```

**Why?** 
> *"Dictionary is just a collection of key–value pairs. If my key is a Python list, and... the contents within a list can change easily, so our key can change. If the key changes, it no longer unlocks its corresponding value."*

If a key could change, it would break the key→value lookup. So keys must be **immutable/hashable** (strings, numbers, tuples), not lists or dicts.

---

## 6. Iterating over a dictionary + a real-world example

**Iteration** — `for key in dict`, and combine with a `for` loop (superpower from Chapter 4):
```python
for name, score in scores.items():
    print(name, score)
```

**The video's flagship example — counting characters in a book:**
```python
counts = {"Harry": 0, "Ron": 0, "Hermione": 0, "Malfoy": 0}

for word in every_word_in_book:          # a huge loop
    if word in counts:                   # is this word a character name?
        counts[word] += 1                # increment its tally
```
> *"I will iterate through every single word in the book. If the word matches any character's name, then I'll increment that name's value in the dictionary by one... Harry appeared 1,200 times... Notice how fast Python is — the for loop completed the entire book almost instantaneously."*

This is **real program thinking**: recognizing that "count occurrences efficiently" → use a dictionary keyed by the item being counted.

---

## 7. The `.get()`-based counting idiom (robust)

A more flexible counter that doesn't require pre-listing keys:
```python
counts = {}
for word in words:
    counts[word] = counts.get(word, 0) + 1
```

---

## 8. Activity

1. Build a simple `menu` dict (item → price); add, update, and `pop` items.
2. Build a **nested** dict (restaurant → menu → price) and access a deep value.
3. Try to make a list a key — observe the `TypeError`.
4. Build a word counter over a paragraph using a `dict` and a `for` loop.

---

## 9. Assessment

1. What is returned by `scores.get("MissingKey")`? And with a default?
2. **Why can't a list be a dict key?** Explain using the "key must unlock its value" reasoning.
3. Access the price of `waffle fries` in a nested dict.
4. **Predict:** after counting characters in a book with a dict, does `"Harry"` map to the number of times it appeared? Why is a dict ideal for this?

---

## How to run this chapter's examples

```bash
python3 main.py       # working demonstration
python3 exercise.py   # complete the TODOs
```
