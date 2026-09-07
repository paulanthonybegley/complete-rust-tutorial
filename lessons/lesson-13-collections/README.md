# Lesson 13: Collections (Vec, String, HashMap) — Walkthrough

## Learning Goal
Use Rust's standard collections — growable arrays, strings, and key-value maps.

---

## 1. `Vec<T>` — growable array (heap-allocated)

A `Vec` is a dynamically-sized sequence of elements of the **same type**, stored on the heap.

```rust
let mut v: Vec<i32> = Vec::new();
v.push(1);       // add to the end
v.push(2);
v.push(3);

// Or with the vec! macro:
let v2 = vec![1, 2, 3];
let v3 = vec![0; 10];   // ten zeros

v.pop();                // remove & return last (Option<T>)
let x = v[1];           // index access — PANICS if out of range
```

Common methods:
| Method | What it does |
|--------|--------------|
| `v.push(x)` | Add to end |
| `v.pop()` | Remove & return last (`Option`) |
| `v.len()` | Number of elements |
| `v.get(i)` | Optional access (returns `Option<&T>`) — safe indexing |
| `v.contains(&x)` | Membership check |
| `v.remove(i)` | Remove at index (shifts elements) |
| `v.sort()` | Sort in place |

**Safe vs panic indexing:**
```rust
let v = vec![10, 20, 30];
let &a = &v[1];           // a = 20 (panics if out of bounds)
let b = v.get(99);        // b = None (safe!)
```

---

## 2. `String` vs `&str`

This is a frequent point of confusion. Both hold UTF-8 text but differ in ownership.

| | `String` | `&str` |
|--|----------|--------|
| **Owned?** | Yes — owns the data on the heap | No — a borrow/reference (string slice) |
| **Mutable/growable?** | Yes | No |
| **View/copy?** | Data lives in the `String` | A *view* into some `String` (or a literal) |
| **Example** | `let s = String::from("hi")` | `let s: &str = "hi"` |

```rust
let mut s = String::from("hello");   // owned, growable
s.push('!');                          // add char
s.push_str(" world");                 // add a &str
println!("{}", s);                    // "hello! world"

// &str is immutable
let literal: &str = "fixed";          // string literal is a &str
```

Create/convert:
```rust
String::from("hi");       // from &str
"hi".to_string();         // from &str
"hi".to_owned();          // from &str

// &str can't be indexed by char position safely (UTF-8) — use .bytes() or .chars()
for c in "héllo".chars() { println!("{c}"); }
```

---

## 3. `HashMap<K, V>` — key-value pairs

A map from keys of type `K` to values of type `V`. Keys must be unique.

```rust
use std::collections::HashMap;

let mut scores = HashMap::new();
scores.insert(String::from("Blue"), 10);
scores.insert(String::from("Yellow"), 50);

// Access
let score = scores.get("Blue");          // Option<&i32>
match scores.get("Blue") {
    Some(&v) => println!("Blue = {v}"),
    None => println!("No Blue"),
}

// Update
scores.insert(String::from("Blue"), 25);   // overwrite
scores.entry(String::from("Green")).or_insert(99); // insert if absent

// Iterate (unordered)
for (key, value) in &scores {
    println!("{key} = {value}");
}
```

**Updating based on existing value** — the classic word-count pattern:
```rust
let text = "hello world hello";
let mut map = HashMap::new();
for word in text.split_whitespace() {
    let count = map.entry(word).or_insert(0);
    *count += 1;
}
```

> **Ownership:** inserting a `String` into a HashMap **moves** it — the map now owns it.

---

## 4. Iterating over collections

```rust
for item in &v { }          // borrow each element
for item in &mut v { }      // mutable borrow
for item in v { }           // consume the Vec (takes ownership)

for (k, val) in &map { }    // iteration over maps
```

---

## 5. Activity: Build a word counter

1. Take a paragraph of text
2. Split into words
3. Count occurrences of each word in a `HashMap`
4. Print the words sorted by count

Bonus: a contact list (name → phone number), a shopping cart (`Vec`), or a library catalog.

---

## 6. Assessment

Choose the **right collection** for each scenario (and justify):
| Scenario | Collection |
|----------|------------|
| Preserve order of user clicks | `Vec` |
| Fast lookup of user by ID | `HashMap` |
| Growable list of scores | `Vec` |
| Unique set of words | `HashSet` (related) |
| Count occurrences | `HashMap` counting idiom |

---

## How to run this lesson's examples

```bash
rustc main.rs && ./main
```
