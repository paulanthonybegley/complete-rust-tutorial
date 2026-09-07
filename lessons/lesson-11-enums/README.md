# Lesson 11: Enums & Pattern Matching — Walkthrough

## Learning Goal
Define enums with associated data and use `match` and `if let` for safe, exhaustive pattern matching.

---

## 1. What is an enum?

An enum (`enum`) lets you define a type that can be **one of several variants**. Unlike a struct (all fields present), an enum has *one* variant at a time.

```rust
enum IpAddrKind {
    V4,
    V6,
}
```

---

## 2. Enums can hold data — this is the power

Variants can carry values (like small embedded structs/data):

```rust
enum Message {
    Quit,                                  // no data
    Move { x: i32, y: i32 },               // anonymous struct fields
    Write(String),                         // a String
    ChangeColor(i32, i32, i32),            // three i32s (tuple-like)
}
```

`Option<T>` is the most famous enum — it means "either a value or nothing":
```rust
enum Option<T> {
    Some(T),   // has a value of type T
    None,      // no value
}
```

---

## 3. `match` — exhaustive pattern matching

`match` compares a value against patterns and runs the first matching arm. **The compiler requires every possible case to be handled** (exhaustiveness).

```rust
enum Coin {
    Penny,
    Nickel,
    Dime,
    Quarter,
}

fn value_in_cents(coin: Coin) -> u8 {
    match coin {
        Coin::Penny => 1,
        Coin::Nickel => 5,
        Coin::Dime => 10,
        Coin::Quarter => 25,
        // Every variant is covered — no need for a fallback
    }
}
```

### Bind to values
```rust
enum UsState { Alabama, Alaska, /* ... */ }

enum Coin { Penny, Nickel, Dime, Quarter(UsState) }

fn value(coin: &Coin) -> u8 {
    match coin {
        Coin::Quarter(state) => {
            println!("State quarter from {state:?}!");
            25
        }
        _ => 0,   // catch-all using `_`
    }
}
```

---

## 4. Patterns in depth

| Pattern | Matches |
|---------|---------|
| Literal | `1`, `"hello"`, `Coin::Penny` |
| Variable binding | `Coin::Quarter(state)` — binds `state` |
| `_` | Anything (catch-all; value discarded) |
| Multiple | `Coin::Penny | Coin::Nickel => ...` |
| Guard | `n if n > 5 => ...` |

### Match is an expression — returns a value
```rust
let guess = match value {
    0 => "zero",
    1 => "one",
    _ => "many",
};
```

---

## 5. `if let` — single-pattern matching sugar

When you only care about **one** pattern, `if let` is shorter than `match`:

```rust
let config_max = Some(3u8);
match config_max {
    Some(max) => println!("Max is {max}"),
    _ => (),   // must still handle None — boilerplate
}

// Same with if let:
if let Some(max) = config_max {
    println!("Max is {max}");
}
```
`if let` is a **concise way to match one pattern and ignore the rest.**

---

## 6. Exhaustiveness prevents bugs

If you `match` on an enum and forget a variant, the compiler **refuses to compile**:
```
error[E0004]: non-exhaustive patterns: `Coin::Nickel` not covered
```
This forces you to handle every case — eliminating whole classes of "forgot the edge case" bugs.

---

## 7. Enums with methods

Once you can match, add `impl` blocks with methods that use `match` internally:

```rust
impl Message {
    fn call(&self) {
        // match on self and handle each variant
    }
}
```

---

## 8. Activity

Model **one** of:
- **Traffic light**: `Red, Yellow, Green` with a method returning wait duration
- **IP addresses**: `V4(u8,u8,u8,u8)` and `V6(String)` + a method to display
- **Command system**: `Add(String)`, `Subtract(i32)`, `Quit` handled via match

---

## 9. Assessment

1. Write `match` expressions that safely handle **all** variants of your enum.
2. **Assessment Evidence:** Write **unit tests** that verify each variant behaves correctly.

```rust
#[test]
fn test_ip_display_v4() {
    let ip = IpAddr::V4(127, 0, 0, 1);
    assert_eq!(ip.display(), "127.0.0.1");
}
```

---

## How to run this lesson's examples

```bash
rustc main.rs && ./main
# Tests: rustc --test main.rs (or use Cargo with #[cfg(test)])
```
