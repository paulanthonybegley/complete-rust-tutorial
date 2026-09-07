# Lesson 4: Variables & Mutability — Walkthrough

## Learning Goal
Declare variables, understand Rust's default immutability, and use constants and shadowing.

---

## 1. Key concepts

### Immutability by default
```rust
let x = 5;      // x is IMMUTABLE (cannot be changed)
x = 6;          // ERROR: cannot assign twice to immutable variable
```

This is Rust's **default** — and it's intentional. Immutable-by-default makes code easier to reason about and prevents accidental modification bugs.

### Making variables mutable
```rust
let mut y = 5;   // y is MUTABLE (can be changed)
y = 6;           // OK
```

Use `mut` only when you actually need to change a value.

### Type annotation
```rust
let z: i32 = 5;              // Explicit type annotation
let w = 5;                   // Type inferred by compiler (i32)
```

### Constants
```rust
const MAX_POINTS: u32 = 100_000;
```
- **Always immutable** (cannot be `mut`)
- **Type must always be annotated**
- Value is inlined at compile time
- Convention: `SCREAMING_SNAKE_CASE`
- Can be declared at the top level (outside functions)

### Shadowing
```rust
let x = 5;        // x = 5
let x = x + 1;    // x = 6 (new binding, old one is gone)
let x = x * 2;    // x = 12
```
Shadowing **re-declares** a variable with `let`, creating a new binding. Unlike `mut`, it doesn't change the value — it creates a new one. Shadowing can also change the **type**:

```rust
let spaces = "   ";       // &str
let spaces = spaces.len(); // usize  (type changed!)
```

---

## 2. Mutability vs shadowing — the key difference

| | `mut` | Shadowing |
|--|-------|-----------|
| Changes value | Yes | Yes |
| Re-uses variable name | No (same binding) | Yes (new binding) |
| Can change type | **No** | **Yes** |
| Syntax | `let mut x` | `let x = ...` again |

```rust
let mut guess = "5";       // &str
guess = 5;                 // ERROR: expected &str, found integer
// ^ with `mut` you CANNOT change the type

let guess = "5";           // &str
let guess = 5;             // OK — shadowing lets you change type
```

---

## 3. Reading compiler errors

Rust's compiler gives **excellent** error messages. Example:
```
error[E0384]: cannot assign twice to immutable variable `x`
 --> src/main.rs:3:5
  |
2 |     let x = 5;
  |         -
  |         |
  |         first assignment to `x`
  |         help: consider making this binding mutable: `mut x`
3 |     x = 6;
  |     ^^^^^ cannot assign twice to immutable variable
```

The compiler even **suggests a fix** ("consider making this binding mutable"). Always read the `help:` lines — they often solve the problem directly.

---

## 4. Activity: Explore variables

Create a program that:
1. Declares immutable and mutable variables
2. **Attempts** to mutate an immutable one (see the error)
3. Demonstrates shadowing (including a type change)
4. Defines a constant

Run it and observe the compiler messages.

---

## 5. Assessment

1. **Predict** whether each snippet compiles or errors (then verify by running):
   ```rust
   let a = 1; a = 2;            // ??
   let mut b = 1; b = 2;        // ??
   const C: i32 = 3; C = 4;     // ??
   let d = 1; let d = d + 1;    // ??
   ```
2. Write **3 code examples** demonstrating immutability (one each: compile error, `mut` fix, shadowing).

---

## How to run this lesson's examples

```bash
rustc main.rs
./main
```
(Or run inside a `cargo new` project using `cargo run`.)
