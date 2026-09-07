# Lesson 10: Structs — Walkthrough

## Learning Goal
Define and use custom data structures with structs and methods.

---

## 1. What is a struct?

A struct is a custom data type that groups named fields. It's like a "record" or "class without inheritance behaviors" — a blueprint for related data.

```rust
struct User {
    active: bool,
    username: String,
    email: String,
    sign_in_count: u64,
}
```

---

## 2. Creating instances

```rust
let user1 = User {
    active: true,
    username: String::from("alice"),
    email: String::from("alice@example.com"),
    sign_in_count: 1,
};
```

Access fields with dot notation:
```rust
println!("{}", user1.email);
user1.email = String::from("new@example.com"); // only if fields are mutable
```

**Field init shorthand** — when a variable has the same name as a field:
```rust
fn build_user(email: String, username: String) -> User {
    User {
        active: true,
        username,        // shorthand for username: username
        email,           // shorthand for email: email
        sign_in_count: 1,
    }
}
```

### Struct update syntax — reuse most fields from another instance
```rust
let user2 = User {
    email: String::from("bob@example.com"),
    ..user1   // copy the remaining fields from user1
};
```
> `user1` is **moved** because `String` fields are moved (email & username). See the "rest of the fields" move note below.

---

## 3. Tuple structs

Structs without named fields:
```rust
struct Color(i32, i32, i32);
struct Point(i32, i32, i32);

let black = Color(0, 0, 0);
let origin = Point(0, 0, 0);
```
`Color` and `Point` are **different types** even though both are 3×i32.

---

## 4. Unit structs

Structs with no fields (rarely used directly, mostly with traits later):
```rust
struct AlwaysEqual;
```

---

## 5. Debug printing

By default you cannot `println!("{:?}", user)` — the struct must implement `Debug`. Derive it:
```rust
#[derive(Debug)]
struct User { ... }
```
Now `println!("{:?}", user)` and `println!("{:#?}", user)` (pretty) work. Without `#[derive(Debug)]`, you get a compile error explaining exactly this.

---

## 6. Methods with `impl`

Methods are functions that take `self` and are tied to a struct:
```rust
impl Rectangle {
    fn area(&self) -> u32 {           // &self = borrow (read-only)
        self.width * self.height
    }
    fn can_hold(&self, other: &Rectangle) -> bool {
        self.width > other.width && self.height > other.height
    }
}
```

- `&self` — immutable borrow (reading)
- `&mut self` — mutable borrow (modifying)
- `self` — takes ownership (consumes; rarely used)

### Associated functions (no `self`) — constructors
```rust
impl Rectangle {
    fn square(size: u32) -> Rectangle {  // NO self -> associated function
        Rectangle { width: size, height: size }
    }
}
// Called with :: not . :
let sq = Rectangle::square(3);
```

---

## 7. Activity

Design structs for **one** of:
- **Library system**: `Book` (title, author, year, available) + `Library`
- **Student records**: `Student` (name, id, grades: Vec<f64>)
- **Game entities**: `Player` (name, health, x, y) + `Enemy`

Add methods using `impl` (e.g., `Book::is_available()`, `Student::average_grade()`).

---

## 8. Assessment

1. Add methods to a struct using `impl`.
2. Explain what the **`self` parameter** does and the difference between `&self`, `&mut self`, and `self`.
3. When to use an **associated function** (`::`) instead of a method (`.`)?

---

## How to run this lesson's examples

```bash
rustc main.rs && ./main
```
