// Lesson 1: Introduction to Rust — Examples
// Conceptual preview of Rust syntax. You'll learn these in detail later.
// To run (after completing Lesson 3 / Cargo setup):
//   rustc examples.rs
//   ./examples

fn main() {
    // Rust is safe: no `null`, instead Option<T>
    let maybe_number: Option<i32> = Some(42);
    println!("Rust has no null pointers — it uses Option: {:?}", maybe_number);

    // Rust is fast: compiles to native machine code
    println!("Rust compiles directly to native machine code (no VM, no GC)");

    // Rust values are immutable by default
    let greeting = "Hello, Rust!";
    println!("{}", greeting);

    // Rust's compiler catches bugs before your program runs
    println!("Memory safety and concurrency bugs are caught at COMPILE time.");
}
