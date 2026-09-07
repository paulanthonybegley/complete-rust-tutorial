# Lesson 8: Ownership — Walkthrough

## Learning Goal
Understand Rust's ownership system — the core mechanism that guarantees memory safety without a garbage collector.

---

## 1. The three ownership rules

1. **Each value has exactly one owner** at a time
2. **When the owner goes out of scope, the value is dropped** (freed from memory)
3. Ownership can be **moved** from one variable to another

```rust
{
    let s = String::from("hello"); // s owns the string
    // ... use s ...
}   // <-- scope ends, s is dropped, memory freed automatically
```

> Rust frees memory automatically when the owner goes out of scope — **no garbage collector, no manual free()**. This is why there are no memory leaks or use-after-free bugs.

---

## 2. Stack vs Heap

| Memory | Characteristics |
|--------|-----------------|
| **Stack** | Fast, fixed size, LIFO. Stores primitives (i32, f64, bool, char) and fixed-size types |
| **Heap** | Slower, dynamic size. Stores growable data: `String`, `Vec`, etc. |

```rust
let x = 5;                    // stack (fixed size)
let s = String::from("hi");   // pointer+len+cap on stack, STRING DATA on heap
```
The stack holds a **pointer** to heap data. Ownership governs who owns the heap data.

---

## 3. Move semantics

Copying a `String` doesn't deep-copy it — it **moves ownership**:

```rust
let s1 = String::from("hello");
let s2 = s1;      // s1 is MOVED into s2

println!("{}", s1); // ERROR: value borrowed/moved — s1 is no longer valid
println!("{}", s2); // OK
```

Why? If both pointed to the same heap data, freeing both would be a **double-free** bug. Rust instead *invalidates* `s1`, so only `s2` frees the heap.

For **primitive types** (Copy types like i32), assignment *copies* — no move:
```rust
let x = 5;
let y = x;   // x is Copy, so both are valid
println!("{} {}", x, y); // OK!
```

**Copy types:** integers, floats, booleans, chars, tuples of Copy types. Types that own heap data (String, Vec) are NOT Copy.

---

## 4. Clone — explicit deep copy

If you want a *true* copy of heap data:
```rust
let s1 = String::from("hello");
let s2 = s1.clone();   // deep copies the heap data
println!("{} {}", s1, s2); // both valid
```
`clone()` is explicit — you pay for the copy on purpose.

---

## 5. Functions take ownership

Passing a value to a function **moves** it:

```rust
fn takes_ownership(s: String) {
    println!("{}", s);
} // s dropped here

fn main() {
    let s = String::from("hi");
    takes_ownership(s);   // s moved into the function
    // println!("{}", s); // ERROR: no longer valid
}
```

Returning a value **moves ownership back**:
```rust
fn gives_back(s: String) -> String {
    s // ownership returned
}
```

This "moving in and out" is the reason we use **references** (Lesson 9) to avoid moving everything constantly.

---

## 6. Activity: Ownership explorer

1. Write code that **moves** a `String` and try to use it afterward (observe the error).
2. Use `.clone()` to fix it.
3. Create a function that takes a `String`, returns it, and observe ownership transfer.
4. Verify that `i32` is Copy (can reuse after assignment).

---

## 7. Assessment

For this snippet, **diagram** the ownership flow and explain what happens at each step:
```rust
fn main() {
    let s = String::from("hello");
    let t = s;          // step 1
    let u = t.clone();  // step 2
    print_string(u);    // step 3
    println!("{}", t);  // step 4
    // println!("{}", s); // step 5 — does this compile?
}
fn print_string(x: String) {
    println!("{}", x);
}
```

**Accessibility:** Draw a diagram showing each variable pointing to (or owning) the heap data, and mark when each is freed.

---

## How to run this lesson's examples

```bash
rustc main.rs && ./main
# run errors.rs to see compiler messages (uncomment functions one at a time)
```
