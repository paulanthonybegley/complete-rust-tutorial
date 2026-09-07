// Lesson 4: Variables & Mutability — DELIBERATE ERROR example
// Each function below does NOT compile. Uncomment them ONE AT A TIME,
// run `rustc errors.rs`, and read the compiler error + help message.
// This is how you learn to read Rust's errors.

/*
fn assign_immutable() {
    let x = 5;
    x = 6; // ERROR: cannot assign twice to immutable variable
}
*/

/*
fn mutate_const() {
    const C: i32 = 3;
    C = 4; // ERROR: cannot assign to constant
}
*/

/*
fn change_type_with_mut() {
    let mut guess = "5";   // &str
    guess = 5;             // ERROR: expected &str, found integer
}
*/

fn main() {
    println!("Uncomment the broken functions one at a time and run `rustc errors.rs`.");
    println!("Read the `help:` line the compiler suggests — it often contains the fix.");
}
