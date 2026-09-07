# Lesson 6: Functions — Walkthrough

## Learning Goal
Define and call functions with parameters, return values, and understand statements vs expressions.

---

## 1. Syntax

```rust
fn function_name(param: Type, other: Type) -> ReturnType {
    // body
    return_value   // last expression (no semicolon) = return value
}
```

### Example
```rust
fn add(a: i32, b: i32) -> i32 {
    a + b   // last expression, no semicolon — this is the return value
}
```

- Parameters **must** have type annotations
- Return type after `->` (optional if nothing is returned)
- Naming convention: **snake_case**

---

## 2. Statements vs Expressions

This is fundamental to Rust.

| | Returns a value? |
|--|------------------|
| **Statement** | No (does an action) |
| **Expression** | Yes (evaluates to a value) |

```rust
let y = {
    let x = 3;
    x + 1      // this expression (no semicolon) is the value of the block
};
// y = 4
```

Key differences:
- **Semicolon** makes something a statement: `x + 1;` becomes a statement returning `()` (unit)
- `if` blocks are expressions, `match` is an expression, blocks `{ }` are expressions
- Function bodies are expressions — the last expression is returned

```rust
fn five() -> i32 {
    5            // NO semicolon -> returns 5
}

fn five_wrong() -> i32 {
    5;           // WITH semicolon -> returns () -> ERROR (expected i32, found ())
}
```

---

## 3. Explicit return

Use `return` to exit early:
```rust
fn is_even(n: i32) -> bool {
    if n % 2 == 0 {
        return true;   // early exit
    }
    false               // implicit return
}
```
In idiomatic Rust, prefer implicit last-expression returns over `return` except for early exits.

---

## 4. Multiple return values with tuples

```rust
fn min_max(nums: &[i32]) -> (i32, i32) {
    let min = *nums.iter().min().unwrap();
    let max = *nums.iter().max().unwrap();
    (min, max)   // return a tuple
}
```

---

## 5. Activity

Write three functions:
1. `fn area(width: u32, height: u32) -> u32` — returns rectangle area
2. `fn fahrenheit_to_celsius(f: f64) -> f64` — `(f - 32) * 5 / 9`
3. `fn stats(nums: &[i32]) -> (i32, i32)` — returns `(min, max)`

---

## 6. Assessment: Debug broken functions

Fix these (each has a specific error) and explain why each fails:

```rust
// Broken 1
fn broken1() -> i32 {
    5;   // has a semicolon -> returns (), type mismatch
}

// Broken 2
fn broken2(x: i32) -> i32 {
    x = x + 1;  // missing mut + statement issue
}

// Broken 3
fn broken3(x: i32, y: i32) -> i32 {
    x + y  // actually fine! what's the "mistake"? (none - it compiles)
}
```

---

## How to run this lesson's examples

```bash
rustc main.rs && ./main
```
