# Lesson 12: Option, Result & Error Handling — Walkthrough

## Learning Goal
Handle values that may be absent (`Option`) and operations that may fail (`Result`) without exceptions.

---

## 1. The problem: no exceptions, no null

Rust has **no `null`** and **no exceptions**. Instead:
- **`Option<T>`** — a value that may or may not exist (replaces nullable types / `null`)
- **`Result<T, E>`** — an operation that may succeed (`Ok`) or fail (`Err`)

These are **enums**, so the compiler *forces* you to handle both cases.

---

## 2. `Option<T>` — absence of value

```rust
enum Option<T> {
    Some(T),
    None,
}
```

```rust
let present = Some(5);            // i32 present
let absent: Option<i32> = None;   // no value
```

You **cannot** use an `Option<i32>` as if it were an `i32`:
```rust
let x: i8 = 5;
let y: Option<i8> = Some(5);
let sum = x + y;   // ERROR: cannot add Option<i8> to i8
```
This forces you to unwrap/check the `Option` before using it — eliminating "null pointer" bugs.

---

## 3. Common `Option` methods

| Method | What it does |
|--------|--------------|
| `.unwrap()` | Panic if `None`, else return the value |
| `.expect("msg")` | Like unwrap but with a custom panic message |
| `.unwrap_or(default)` | Return value, or `default` if `None` |
| `.is_some()` / `.is_none()` | Check presence |
| `.map(fn)` | Apply fn to `Some` value, keep `None` as `None` |

---

## 4. `Result<T, E>` — fallible operations

```rust
enum Result<T, E> {
    Ok(T),    // success — holds a value of type T
    Err(E),   // failure — holds an error of type E
}
```

```rust
use std::fs::File;

fn main() {
    let f = File::open("hello.txt");
    let f = match f {
        Ok(file) => file,
        Err(error) => panic!("Problem opening file: {error:?}"),
    };
}
```

---

## 5. `unwrap` / `expect` — be careful

```rust
let s = File::open("x.txt").unwrap();   // panics if Err
let s = File::open("x.txt").expect("x.txt should exist"); // clearer panic msg
```
These are convenient but **panic on failure**. Use them only when you're sure the call can't fail, or when panic is the desired behavior. For real error handling, use `match`, `?`, or `unwrap_or`.

---

## 6. The `?` operator — error propagation

`?` is sugar for "if this is `Err`, **return** the error early; otherwise give me the `Ok` value."
```rust
use std::fs::File;
use std::io::{self, Read};

fn read_username_from_file() -> Result<String, io::Error> {
    let mut file = File::open("hello.txt")?;  // ? propagates Err
    let mut s = String::new();
    file.read_to_string(&mut s)?;             // ? propagates Err
    Ok(s)
}
```
- `?` works inside functions that return `Result` or `Option`
- It **returns** the error to the caller — no panics, clean propagation
- `main` can return `Result`: `fn main() -> Result<(), Box<dyn Error>>`

---

## 7. Custom error types with `From`

For large programs, define your own error type and implement `From` to convert errors automatically (so `?` can convert between error types):
```rust
use std::fmt;

#[derive(Debug)]
struct MyError(String);

impl fmt::Display for MyError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.0)
    }
}
impl std::error::Error for MyError {}
```
In practice most projects `#[derive(thiserror::Error)]` (from the `thiserror` crate).

---

## 8. Activity

Write a program that:
1. Opens a file by name (use `Result`)
2. **Parses** its contents as a number (`parse::<i32>()` returns `Result`/`Option`)
3. Handles a **missing** value with `Option`
4. Uses `match`, `?`, `unwrap_or`, and `expect` appropriately

Example: read `numbers.txt`, parse each line, sum the valid numbers, report errors.

---

## 9. Assessment

Refactor this **panic-prone** code to use proper `Result` handling:
```rust
let file = File::open("data.txt").unwrap();   // panics if missing
let n: i32 = "abc".parse().unwrap();           // panics if not a number
```
Explain **each** change and why `unwrap` is unsafe here.

**Accessibility:** Provide an error-handling "cheat sheet" (table above) for reference during exercises.

---

## How to run this lesson's examples

```bash
rustc main.rs && ./main
```
