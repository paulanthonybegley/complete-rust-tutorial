# Lesson 14: Traits & Generics — Walkthrough

## Learning Goal
Write reusable, type-safe code with generics and traits (Rust's form of interfaces/shared behavior).

---

## 1. Generics — "works with many types"

Generic functions/structs don't hardcode one type — they work with any type that meets stated requirements.

```rust
fn largest<T>(list: &[T]) -> &T {
    let mut largest = &list[0];
    for item in list {
        if item > largest {   // <- requires T to support `>`
            largest = item;
        }
    }
    largest
}
```

**Why we need trait bounds:** `>` doesn't work on every type, so the compiler needs to know `T` supports comparison. That's where **traits** come in.

---

## 2. Traits — shared behavior ("interfaces")

A `trait` defines a set of methods a type **must** implement to have that behavior.

```rust
pub trait Summary {
    fn summarize(&self) -> String;   // required method (no body)
}
```

Implement the trait for a type with `impl ... for ...`:
```rust
pub struct Article { pub headline: String, pub body: String }

impl Summary for Article {
    fn summarize(&self) -> String {
        format!("{}, ...", self.headline)
    }
}
```

---

## 3. Trait bounds — "T must have this behavior"

| Syntax | Meaning |
|--------|---------|
| `fn f<T: Summary>(x: &T)` | `T` must implement `Summary` |
| `fn f<T: Summary + Clone>(x: &T)` | Multiple bounds |
| `fn f(x: &impl Summary)` | Shorthand (`impl Trait`) for single use |
| `fn f<T>(x: &T) where T: Summary + Clone` | `where` clause for readability |

```rust
use std::fmt::Display;

fn notify<T: Summary>(item: &T) {
    println!("Breaking news: {}", item.summarize());
}

// Equivalent shorthand:
fn notify_short(item: &impl Summary) {
    println!("Breaking news: {}", item.summarize());
}
```

---

## 4. Default implementations

A trait method can have a **default body** that types inherit unless they override it:

```rust
pub trait Summary {
    fn summarize_author(&self) -> String;   // required

    fn summarize(&self) -> String {          // has default
        format!("(Read more from {}...)", self.summarize_author())
    }
}
```

---

## 5. `impl Trait` syntax (return position)

You can return a type that implements a trait without naming it:
```rust
fn returns_summarizable() -> impl Summary {
    Article { headline: String::from("A headline"), body: String::from("...") }
}
```

> **Caveat:** `impl Trait` in return position must return *one concrete type* — you can't return different types conditionally. For that, use dynamic dispatch.

---

## 6. Dynamic dispatch — `Box<dyn Trait>`

`dyn Trait` is a **trait object** — it allows runtime polymorphism (like interfaces/abstract base classes). Use it when you need to hold different types behind the same trait:

```rust
fn make(screen: Option<String>) -> Box<dyn Summary> {
    if let Some(x) = screen { Box::new(Tweet(x)) } else { Box::new(Article(...)) }
}
// Both Tweet and Article implement Summary, and both can be Box<dyn Summary>.
```

- `dyn Trait` enables **heterogeneous collections**: `Vec<Box<dyn Summary>>`
- Costs a tiny runtime overhead (vtable lookup) compared to generics (monomorphization)
- Use **generics** where the type is known at compile time; use **`dyn`** where it varies at runtime

---

## 7. Activity

Pick one or more:
1. Create a **generic** `largest` function (above) and test with integers and chars.
2. Implement a `Summary` trait for two different structs (e.g., `Article`, `Tweet`).
3. Implement `Display` for a custom struct.
4. Build a trait-based **plugin/notification** system using `Vec<Box<dyn Summary>>`.

---

## 8. Assessment

Design a **trait hierarchy** for a real-world domain (choose one: animals/shapes/payments/vehicles).
- Define a base `trait`
- Implement it for 2+ concrete types
- Add a method that uses the trait as a bound (generic)
- Add a method that uses `dyn Trait` (dynamic dispatch)
- Explain when you'd choose generic vs `dyn` for each

**Assessment Evidence:** Code review demonstrating trait + generic constraint usage.

---

## How to run this lesson's examples

```bash
rustc main.rs && ./main
```
