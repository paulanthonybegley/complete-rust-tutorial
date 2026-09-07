// Lesson 8: Ownership — DELIBERATE ERROR examples
// Uncomment one function at a time and run `rustc errors.rs`
// to read the ownership compiler error.

/*
fn use_after_move() {
    let s1 = String::from("hello");
    let s2 = s1;            // s1 is moved
    println!("{}", s1);     // ERROR: value used here after move
    println!("{}", s2);
}
*/

/*
fn use_after_function_move() {
    let s = String::from("hello");
    takes_ownership(s);
    println!("{}", s); // ERROR: borrow of moved value
}

fn takes_ownership(s: String) {
    println!("{}", s);
}
*/

fn main() {
    println!("Uncomment the broken functions one at a time and run `rustc errors.rs`.");
    println!("These show the '#[derive]' and 'move' errors clearly.");
}
