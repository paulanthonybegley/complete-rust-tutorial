# Lesson 5: Data Types — Walkthrough

## Learning Goal
Understand Rust's primitive (scalar and compound) data types.

---

## 1. Scalar types — a single value

### Integers (`i` = signed, `u` = unsigned; number = bits)
| Size | Signed | Unsigned |
|------|--------|----------|
| 8-bit | `i8` | `u8` |
| 16-bit | `i16` | `u16` |
| 32-bit | `i32` | `u32` |
| 64-bit | `i64` | `u64` |
| 128-bit | `i128` | `u128` |
| arch-dependent | `isize` | `usize` |

- **`i32` is the default** inferred type (good balance of speed/precision)
- `isize`/`usize` match your machine's pointer size (used for indexing)

### Integer literals can have suffixes & separators
```rust
let a = 57u8;        // unsigned 8-bit
let b = 1_000_000;   // underscores = readability (1000000)
let c = 0xff;        // hex
let d = 0o77;        // octal
let e = 0b1111_0000; // binary
```

### Integer overflow
```rust
let x: u8 = 255;
x + 1; // In DEBUG builds: PANICS ("attempt to add with overflow")
       // In RELEASE builds: wraps around to 0 (two's complement)
```
Debug builds panic on overflow (good for catching bugs); release builds wrap silently (for performance).

### Floats
```rust
let f = 2.0;   // f64 (default — double precision)
let g: f32 = 3.0; // f32 (single precision)
```

### Booleans
```rust
let t = true;
let f: bool = false;
```

### Characters
```rust
let c = 'z';
let z = 'ℤ';        // Unicode scalar values
let heart = '❤';
```
A `char` is **4 bytes** and represents a Unicode scalar value (not just ASCII).

---

## 2. Compound types — multiple values

### Tuples — fixed length, may be different types
```rust
let tup: (i32, f64, u8) = (500, 6.4, 1);
let (x, y, z) = tup;          // destructuring
println!("{} {} {}", x, y, z);
println!("{}", tup.0);        // index access
println!("{}", tup.1);
```

### Arrays — fixed length, same type, on the stack
```rust
let a = [1, 2, 3, 4, 5];       // [i32; 5]
let b: [i32; 5] = [1, 2, 3, 4, 5];
let c = [3; 5];                // [3, 3, 3, 3, 3]  (5 copies of 3)
println!("{}", a[0]);          // 1
// println!("{}", a[10]);       // PANICS: index out of bounds (checked!)
```

> **Vectors** (`Vec<T>`, Lesson 13) are the growable, heap-allocated alternative.

---

## 3. `std::mem::size_of`

You can check how many **bytes** a type occupies:
```rust
use std::mem::size_of;
println!("{}", size_of::<i32>());  // 4
println!("{}", size_of::<char>()); // 4
println!("{}", size_of::<bool>()); // 1
```

---

## 4. Activity: Type explorer

1. Create a variable of **each** scalar type (one `i`-type, one `u`-type, `f32`, `f64`, `bool`, `char`).
2. Print the **byte size** of each with `std::mem::size_of`.
3. Create a **tuple** and an **array**, and destructure/access them.
4. Try accessing an array index **out of bounds** and observe the panic message.

---

## 5. Assessment

Choose the appropriate Rust type for **5 real-world scenarios** and justify each:

| Scenario | Type? | Why? |
|----------|-------|------|
| Age of a person | `u8` | Non-negative, small |
| Bank account balance | `f64` | Need decimals, precision |
| Index into a collection | `usize` | Arch-appropriate indexing |
| Temperature in °C | `f64` | Allow negatives + decimals |
| Number of people in a room | `u32` | Non-negative count |

**Assessment Evidence:** The instructor provides a code snippet with deliberately wrong types; you identify and fix each.

---

## How to run this lesson's examples

```bash
rustc main.rs && ./main
```
