# Lesson 7: Control Flow — Walkthrough

## Learning Goal
Use conditionals, loops, and pattern matching to control program flow.

---

## 1. `if` / `else if` / `else`

```rust
let number = 7;

if number < 5 {
    println!("small");
} else if number < 10 {
    println!("medium");
} else {
    println!("large");
}
```

**Important:** The condition **must** be a `bool`. Rust has **no truthy/falsy** values:

```rust
if 1 { }                // ERROR: expected `bool`, found integer
if number != 0 { }      // OK — explicit comparison
```

### `if` is an expression
Because `if` is an expression, you can assign its result:

```rust
let x = if condition { 5 } else { 6 };
```
Both arms must yield the **same type**.

---

## 2. Loops

### `loop` — infinite loop (runs forever unless broken)
```rust
loop {
    println!("forever...");
    break; // exit the loop
}
```

`loop` can **return a value**:
```rust
let mut counter = 0;
let result = loop {
    counter += 1;
    if counter == 10 {
        break counter * 2;   // break with a value
    }
};
// result = 20
```

### `while` — loop while a condition is true
```rust
let mut n = 3;
while n > 0 {
    println!("{n}");
    n -= 1;
}
```

### `for` — iterate over a range or collection
```rust
for i in 0..5 {          // 0,1,2,3,4  (exclusive)
    println!("{i}");
}
for i in 0..=5 {         // 0,1,2,3,4,5 (inclusive)
    println!("{i}");
}
for item in collection { // iterate a collection
    println!("{item}");
}
```

---

## 3. `break` and `continue`

- `break` — exit the loop immediately
- `continue` — skip to the next iteration

```rust
for i in 0..10 {
    if i == 2 {
        continue;   // skip 2
    }
    if i == 5 {
        break;      // stop at 5
    }
    println!("{i}"); // prints 0,1,3,4
}
```

---

## 4. `match` — powerful pattern matching (preview)

We'll cover this in depth in Lesson 11, but here's the idea:
```rust
let n = 3;
match n {
    1 => println!("one"),
    2 => println!("two"),
    _ => println!("other"),  // _ = catch-all
}
```

---

## 5. Activity

Implement:
1. **FizzBuzz** (1–100; print Fizz for multiples of 3, Buzz for 5, FizzBuzz for both)
2. **Factorial** — `5! = 120` using a loop
3. **Number guessing game** — use `loop` + `break` with a value

---

## 6. Assessment

Rewrite this nested `if`-chain using `match`:
```rust
let n = 5;
let description = if n == 0 { "zero" }
                  else if n == 1 { "one" }
                  else if n == 2 { "two" }
                  else { "many" };
```

---

## How to run this lesson's examples

```bash
rustc main.rs && ./main
```
