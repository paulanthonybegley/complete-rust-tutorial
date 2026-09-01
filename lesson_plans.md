# Structured Lesson Plans: Rust Course 2026
## Applied from "Rust Course 2026 – Complete Rust Programming for Beginners"

### Core Concept: "A lesson plan is a system, not a prompt."
Each lesson combines the AI lesson planning framework with Rust programming topics.

---

## Lesson 1: Introduction to Rust

**Objective:** Understand what Rust is, why it exists, and where it's used.

**Key Teaching Points:**
- Rust is a systems programming language focused on safety, speed, and concurrency
- Created by Graydon Hoare at Mozilla, now maintained by the Rust Foundation
- Used by: Firefox, Android, Linux kernel, Discord, Cloudflare
- Compiles to native code, no garbage collector
- Memory safety without runtime overhead

**Activity:** Research and present one real-world Rust project. Identify why Rust was chosen over C/C++.

**Assessment:** Write a 1-paragraph explanation of Rust's value proposition for a non-technical audience.

**Learner Profile:** Beginners with some programming experience (any language).

**Prior Knowledge:** Basic understanding of what programming languages do.

---

## Lesson 2: Setup & Environment

**Objective:** Install Rust toolchain and configure a development environment.

**Key Teaching Points:**
- Install via `rustup` (rustup.rs) - the official installer
- `rustup` manages Rust versions and toolchains
- Install VS Code with rust-analyzer extension
- Verify installation: `rustc --version`, `cargo --version`
- Understand the difference between `rustc` (compiler) and `cargo` (build tool)

**Activity:** Install Rust, create a test project, and run it. Screenshot your terminal output.

**Assessment:** Successfully compile and run a basic program. Document any issues encountered and solutions.

**Accessibility:** Provide alternative installation paths for different OS (Windows, macOS, Linux).

---

## Lesson 3: Cargo & Hello World

**Objective:** Use Cargo to create, build, and run Rust projects.

**Key Teaching Points:**
- `cargo new project_name` - creates a new project
- `cargo build` - compiles the project
- `cargo run` - compiles and runs
- `cargo check` - checks for errors without compiling
- Project structure: `Cargo.toml` (manifest), `src/main.rs` (entry point)
- Dependencies managed in `Cargo.toml`

**Activity:** Create a Cargo project, modify the Hello World message, build and run it.

**Assessment:** Explain the purpose of `Cargo.toml` and the project directory structure.

**Learning Activities:**
- Live coding demonstration
- Pair programming exercise
- Independent practice

---

## Lesson 4: Variables & Mutability

**Objective:** Declare variables, understand mutability, and use constants.

**Key Teaching Points:**
- Variables are immutable by default (`let x = 5;`)
- Use `let mut` for mutable variables (`let mut x = 5;`)
- Type inference: Rust infers types when not specified
- Explicit typing: `let x: i32 = 5;`
- Constants: `const MAX_POINTS: u32 = 100_000;` (always immutable, must specify type)
- Shadowing: re-declaring a variable with `let` creates a new binding

**Activity:** Create variables of different types, attempt to mutate them, observe compiler errors.

**Assessment:** Predict whether code will compile or error, then verify. Write 3 code examples demonstrating immutability.

**Error Handling:** Compiler provides clear error messages - teach students to read them.

---

## Lesson 5: Data Types

**Objective:** Understand Rust's primitive data types and type system.

**Key Teaching Points:**
- Scalar types: integers (i8-i128, u8-u128, isize, usize), floats (f32, f64), booleans, characters
- Integer overflow: debug mode panics, release mode wraps
- Compound types: tuples (fixed size, mixed types), arrays (fixed size, same type)
- Tuple destructuring: `let (x, y, z) = (1, 2.0, 'hello');`
- Array access is bounds-checked at runtime

**Activity:** Create variables of each type, print their sizes using `std::mem::size_of`.

**Assessment:** Identify the appropriate data type for 5 different real-world scenarios.

**Assessment Evidence:** Code review of type usage in a provided snippet.

---

## Lesson 6: Functions

**Objective:** Define and call functions, understand parameters, return values, and expressions.

**Key Teaching Points:**
- Function syntax: `fn function_name(param: Type) -> ReturnType { ... }`
- Parameters must have declared types
- Return value: last expression (no semicolon) or explicit `return`
- Statements vs expressions: statements don't return, expressions do
- Multiple return values via tuples
- Function naming convention: snake_case

**Activity:** Write functions that calculate area, convert temperatures, and return multiple values.

**Assessment:** Debug 3 broken functions. Explain why each one fails and fix it.

**Teacher Decisions:** Emphasize expression-based returns as idiomatic Rust.

---

## Lesson 7: Control Flow

**Objective:** Use conditionals, loops, and pattern matching for program flow.

**Key Teaching Points:**
- `if/else if/else` - conditions must be `bool` (no truthy/falsy)
- `if` is an expression: `let x = if condition { 5 } else { 6 };`
- Loops: `loop` (infinite), `while`, `for` with ranges
- `break` and `continue` control loop flow
- `loop` can return values: `let result = loop { ... break value; };`
- `for` loops: `for i in 0..10`, `for item in collection`

**Activity:** Implement FizzBuzz, factorial calculator, and number guessing game.

**Assessment:** Rewrite nested if-else chains using match expressions.

**Learning Activities:**
- Code tracing exercises (predict output)
- Progressive challenge problems
- Pair programming

---

## Lesson 8: Ownership

**Objective:** Understand Rust's ownership system - the core of memory safety.

**Key Teaching Points:**
- Each value has exactly one owner
- When owner goes out of scope, value is dropped (freed)
- Ownership can be moved: `let s2 = s1;` (s1 is no longer valid)
- Clone for deep copies: `let s2 = s1.clone();`
- Stack vs heap: ownership affects heap-allocated data
- Functions take ownership: passing a value moves it

**Activity:** Create programs that demonstrate ownership rules. Trigger and fix ownership errors.

**Assessment:** Diagram the ownership flow of a given code snippet. Explain what happens at each step.

**Accessibility:** Use visual diagrams to show scope and ownership.

---

## Lesson 9: Borrowing & References

**Objective:** Use references to access data without taking ownership.

**Key Teaching Points:**
- Immutable references: `&T` (multiple allowed, can't modify)
- Mutable references: `&mut T` (only one allowed, can modify)
- Rules: many `&T` OR one `&mut T` (never both)
- Dangling references prevented at compile time
- Slicing: `&s[0..5]` for string/array slices

**Activity:** Refactor ownership code to use borrowing. Create functions that read and modify data via references.

**Assessment:** Identify borrowing violations in code. Explain why each is invalid.

**Assessment Evidence:** Code review with peer feedback.

---

## Lesson 10: Structs

**Objective:** Define and use custom data structures.

**Key Teaching Points:**
- Struct syntax: `struct StructName { field: Type, ... }`
- Creating instances: `let user = User { name: String::from("Alice"), age: 30 };`
- Field init shorthand
- Struct update syntax: `let user2 = User { name: String::from("Bob"), ..user };`
- Tuple structs: `struct Color(i32, i32, i32);`
- Unit structs: `struct AlwaysEqual;`
- `#[derive(Debug)]` for printing

**Activity:** Design structs for a library system, student records, or game entities.

**Assessment:** Add methods to a struct using `impl` blocks. Explain the `self` parameter.

**Teacher Decisions:** Introduce methods alongside structs for practical context.

---

## Lesson 11: Enums & Pattern Matching

**Objective:** Define enums and use powerful pattern matching.

**Key Teaching Points:**
- Enum variants can hold data: `enum Message { Quit, Move { x: i32, y: i32 }, Write(String) }`
- `match` expressions must handle all variants
- Pattern matching is exhaustive: compiler enforces completeness
- `_` as a catch-all pattern
- `if let` for single-pattern matching
- Enums with methods via `impl`

**Activity:** Model a traffic light, IP addresses, or command system using enums.

**Assessment:** Write match expressions that safely handle all enum variants.

**Assessment Evidence:** Unit tests that verify enum behavior.

---

## Lesson 12: Option, Result & Error Handling

**Objective:** Handle nullable values and errors without exceptions.

**Key Teaching Points:**
- `Option<T>`: `Some(value)` or `None` - replaces null
- `Result<T, E>`: `Ok(value)` or `Err(error)` - for fallible operations
- `unwrap()` and `expect()` - panic on None/Err (use sparingly)
- `match` for explicit handling
- `?` operator for error propagation
- Custom error types with `From` trait

**Activity:** Write a program that reads a file, parses numbers, handles missing values and errors.

**Assessment:** Refactor panic-prone code to use proper Result handling. Explain each change.

**Accessibility:** Provide error handling cheat sheet for reference.

---

## Lesson 13: Collections (Vec, String, HashMap)

**Objective:** Use Rust's standard collection types.

**Key Teaching Points:**
- `Vec<T>`: growable array, `vec![]` macro, push/pop/access
- `String`: growable UTF-8 string, `String::from()`, `.push_str()`
- `&str` vs `String`: borrowed vs owned string data
- `HashMap<K, V>`: key-value pairs, `.insert()`, `.get()`
- Iterating over collections
- Collections own their data, must be mutable to modify

**Activity:** Build a word counter, contact list, or shopping cart using collections.

**Assessment:** Choose the right collection for 5 different scenarios and justify your choice.

**Learning Activities:**
- Hands-on coding challenges
- Performance comparison exercises

---

## Lesson 14: Traits & Generics

**Objective:** Write reusable, type-safe code with generics and traits.

**Key Teaching Points:**
- Generics: `fn largest<T>(list: &[T]) -> &T { ... }`
- Traits: shared behavior via `trait` and `impl Trait for Type`
- Trait bounds: `fn print<T: Display>(item: T) { ... }`
- Multiple trait bounds: `<T: Display + Clone>`
- `impl Trait` syntax for simpler signatures
- Default implementations in traits
- Traits as dynamic dispatch: `Box<dyn Trait>`

**Activity:** Create a generic sorting function, implement Display for custom types, build a trait-based plugin system.

**Assessment:** Design a trait hierarchy for a real-world domain (animals, shapes, payments).

**Assessment Evidence:** Code review demonstrating trait usage and generic constraints.

---

## Integration Project

**Objective:** Build a complete Rust application using all concepts.

**Project Options:**
1. **CLI Tool** - File processor with error handling
2. **Library** - Reusable data structures with traits
3. **Game** - Text-based adventure with enums and structs

**Requirements:**
- Use ownership and borrowing correctly
- Implement error handling with Result
- Use collections to manage data
- Define traits for extensibility
- Include at least 5 unit tests

**Assessment:** Present project with code walkthrough. Peer review focusing on idiomatic Rust patterns.
