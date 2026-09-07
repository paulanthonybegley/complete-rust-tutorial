# Integration Project — Walkthrough

## Learning Goal
Build a **complete Rust application** that combines everything learned: ownership, borrowing, structs, enums, Option/Result, collections, traits, generics, and tests.

This is the capstone. Pick one project shape and complete it with tests.

---

## Project Options

### Option 1: CLI Tool — Word/Line Counter
- Read a file path from the command line
- Count lines, words, and characters
- Use `Result` for file errors, `HashMap` for word frequencies
- Print a formatted report

### Option 2: Library crate — Reusable data structures
- Define structs + traits
- Implement methods with ownership/borrowing correctly
- Unit tests for every method

### Option 3: Game — Text-based adventure
- Use `enum` for game states / commands
- Use `struct` for player/room
- Use `match` for command handling
- Save/load with `Result`

---

## Common Requirements

Your project **must**:
1. Use **ownership and borrowing** correctly (move vs borrow vs clone — deliberately)
2. Implement **error handling with `Result`** (no panics for expected failures)
3. Use **collections** to manage data (`Vec`, `String`, `HashMap`)
4. Define **traits** for extensibility
5. Include **at least 5 unit tests** in a `#[cfg(test)]` module
6. Be runnable via `cargo run` and `cargo test`

---

## Getting started (CLI tool example)

```bash
cargo new word_counter
cd word_counter
```

Edit `src/main.rs` — see the included `main.rs` for a complete working example you can model after.

---

## Project structure to aim for

```
word_counter/
├── Cargo.toml
└── src/
    ├── main.rs        # entry point — calls library code
    └── lib.rs         # (optional) logic + tests, if you split it
```

---

## Unit tests — how to structure

```rust
#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_word_count_basic() {
        let result = count_words("the cat sat");
        assert_eq!(result, 3);
    }

    // ... at least 5 tests total
}
```

Run them with:
```bash
cargo test
```

---

## Assessment / Presentation

Deliver a **code walkthrough**:
- Explain the ownership/borrowing choices you made (where did you move vs borrow?)
- Show how you handle errors with `Result`
- Justify your trait design
- Run `cargo test` to prove all tests pass

**Peer review focus:** Does it use **idiomatic Rust** patterns (no needless `.clone()`, proper `&`/`&mut`, `?` operator, idiomatic `match`/`if let`)?

---

## Example project: word_counter (full working code in `main.rs`)

Build a small tool that reads a file and reports:
- total lines
- total words
- top 5 most frequent words

The provided `main.rs` is a complete, compilable reference implementation. Study it, then build your own or extend it.
