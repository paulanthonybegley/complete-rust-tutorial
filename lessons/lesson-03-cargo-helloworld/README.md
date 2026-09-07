# Lesson 3: Cargo & Hello World — Walkthrough

## Learning Goal
Use Cargo to create, build, and run Rust projects, and understand the project structure.

---

## 1. Cargo commands cheat sheet

| Command | What it does |
|---------|--------------|
| `cargo new my_project` | Create a new binary project |
| `cargo new my_lib --lib` | Create a new library project |
| `cargo build` | Compile the project |
| `cargo run` | Compile **and** run the project |
| `cargo check` | Check for errors without producing a binary (fast) |
| `cargo test` | Run tests |
| `cargo add crate_name` | Add a dependency |
| `cargo clean` | Remove build artifacts |

---

## 2. Project structure

When you run `cargo new hello_world`, you get:

```
hello_world/
├── Cargo.toml      # The manifest — project config & dependencies
├── Cargo.lock      # Locks exact dependency versions (auto-generated)
└── src/
    └── main.rs     # Your source code entry point
```

### Cargo.toml — the manifest
```toml
[package]
name = "hello_world"       # Project name
version = "0.1.0"          # Version (semver)
edition = "2021"           # Rust edition (language version standard)

[dependencies]             # External crates go here
# serde = "1.0"
```

### src/main.rs — the entry point
```rust
fn main() {
    println!("Hello, world!");
}
```

> **Every Rust binary project** has a `fn main()` — it's the program's entry point.

---

## 3. The build/run workflow

```bash
cd hello_world

# Fast check (no binary produced)
cargo check

# Build a binary
cargo build

# Build AND run
cargo run

# Release build (optimized, for production)
cargo build --release
```

Build output goes to `target/` (for a release build: `target/release/hello_world`).

---

## 4. Activity: Modify and run

1. Create a project: `cargo new hello_world`
2. Edit `src/main.rs` to print a personal message, e.g.:
   ```rust
   fn main() {
       println!("Hi, I'm learning Rust!");
   }
   ```
3. Run `cargo run`
4. Try `cargo check` then `cargo build`, and observe the difference in speed/output

---

## 5. Assessment

Explain, in your own words:
1. What is the purpose of **`Cargo.toml`**?
2. What is the purpose of **`src/main.rs`**?
3. What is the difference between `cargo run` and `cargo check`?

---

## How to run this lesson's examples

This lesson is about Cargo, so the example is a self-contained `main.rs` you can drop into any `cargo new` project:

```bash
cargo new demo
cd demo
# copy this lesson's main.rs into src/main.rs
cargo run
```

Result:
```
Hello, world!
```
