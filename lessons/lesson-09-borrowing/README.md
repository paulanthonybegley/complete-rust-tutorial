# Lesson 9: Borrowing & References — Walkthrough

## Learning Goal
Use references (`&T`, `&mut T`) to access data without taking ownership.

---

## 1. The problem references solve

Without references, every function that uses a `String` must move it in and out:
```rust
fn main() {
    let s = String::from("hello");
    let s = calculate_length(s); // move in, move back out
}
fn calculate_length(s: String) -> (String, usize) {
    let len = s.len();
    (s, len) // tedious!
}
```

**References** let functions *borrow* data without taking ownership:
```rust
fn main() {
    let s = String::from("hello");
    let len = calculate_length(&s); // borrow, not move
    println!("The length of '{}' is {}.", s, len); // s still valid!
}
fn calculate_length(s: &String) -> usize {  // &String = reference
    s.len()
} // s is NOT dropped — we only borrowed it
```

The reference **does not own** the data, so nothing is freed when `calculate_length` ends.

---

## 2. Reference syntax

| Syntax | Meaning |
|--------|---------|
| `&T` | Reference to a value of type `T` (**immutable**) |
| `&mut T` | Mutable reference — can change the value |
| `*x` | **Dereference** — access the value the reference points to |

```rust
let s = String::from("hi");
let r = &s;        // immutable borrow

let mut s2 = String::from("hi");
let r2 = &mut s2;  // mutable borrow
```

---

## 3. The borrowing rules (THE facade of Rust)

1. **At any time, you can have either:**
   - Any number of **immutable** references (`&T`), **or**
   - **Exactly one** mutable reference (`&mut T`)
   - (Never both — never multiple `&mut`)

2. **References must always be valid** (no dangling references).

These rules are checked at **compile time**, so data races are impossible.

```rust
let mut s = String::from("hello");

let r1 = &s;          // immutable borrow OK
let r2 = &s;          // immutable borrow OK (many allowed)
// let r3 = &mut s;   // ERROR: cannot borrow as mutable because also immutable

let r4 = &mut s;      // OK if no immutable borrows are active
// let r5 = &mut s;   // ERROR: cannot borrow as mutable more than once
```

---

## 4. Mutating through a mutable reference

```rust
fn add_word(s: &mut String) {
    s.push_str(" world");  // mutate through the mut reference
}

let mut s = String::from("hello");
add_word(&mut s);
println!("{}", s); // "hello world"
```

> Borrowing a value **mutably** (via `&mut`) gives temporary ownership-like access — you can change it, but only one borrower at a time.

---

## 5. Dangling references are prevented

A **dangling reference** points to freed memory. Rust rejects them at compile time:
```rust
fn dangle() -> &String {
    let s = String::from("hello");
    &s   // ERROR: `s` is dropped here, reference would dangle
}
```
The fix: return the `String` itself (move ownership), not a reference to it.

---

## 6. Slices — a special kind of reference

A slice is a reference to a contiguous portion of data:
```rust
let s = String::from("hello world");
let hello = &s[0..5];   // "hello"
let world = &s[6..11];  // "world"
let first = &s[..5];    // 0..5
let last = &s[6..];     // 6..end
let whole = &s[..];     // entire string
```

Slices are the preferred way to pass string data (see Lesson 13: `&str` vs `String`).

---

## 7. Activity

1. Refactor the ownership version (Lesson 8) to use **references** instead of moving values.
2. Write a function that reads a `String` via `&String`.
3. Write a function that **modifies** a `String` via `&mut String`.
4. Try to create two mutable references to the same value — observe the error.

---

## 8. Assessment

Identify the **borrowing violation** in each snippet and explain why it's invalid:

```rust
// (a)
fn f(s: &mut String) {
    let r = &s;
}
```
```rust
// (b)
let mut v = vec![1, 2, 3];
let first = &v[0];
v.push(4);            // is this allowed while `first` is alive?
println!("{}", first);
```

**Assessment Evidence:** Peer code review — swap code with a partner, find each borrowing error, and explain the rule it violates.

---

## How to run this lesson's examples

```bash
rustc main.rs && ./main
```
