// Lesson 4: Variables & Mutability — Main Examples
// This is the WORKING example. The compile-error version is in errors.rs.
// Run:
//   rustc main.rs && ./main

const MAX_POINTS: u32 = 100_000; // Constants: always immutable, type always annotated

fn main() {
    // Immutable by default
    let x = 5;
    println!("x is immutable: x = {}", x);
    // x = 6; // ERROR: cannot assign twice to immutable variable

    // Mutable
    let mut y = 5;
    println!("Before: y = {}", y);
    y = 6;
    println!("After mutating: y = {}", y);

    // Explicit type annotation
    let z: i64 = 5;
    println!("z is an i64: {}", z);

    // Type inference
    let inferred = 5; // inferred as i32
    println!("inferred is an i32: {}", inferred);

    // Constants
    println!("MAX_POINTS = {}", MAX_POINTS);

    // Shadowing (re-declare with let)
    let spaces = "   ";
    println!("spaces (as &str) = {:?}", spaces);
    let spaces = spaces.len(); // type changed from &str to usize
    println!("spaces (as usize) = {}", spaces);

    // Shadowing with computation
    let n = 5;
    let n = n + 1;
    let n = n * 2;
    println!("After shadowing chain, n = {}", n);
}

/*
Compare with errors.rs to see how changing types with `mut` FAILS,
and why shadowing is needed instead.
*/
