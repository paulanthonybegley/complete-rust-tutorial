// Lesson 9: Borrowing & References — Exercise
// Complete the TODOs / answer the questions. Fix the deliberate errors.
// Run:
//   rustc exercise.rs && ./exercise

fn main() {
    // TODO 1: Fix this so it compiles — there's a borrow conflict.
    // Uncomment and fix:
    /*
    let mut s = String::from("hello");
    let r1 = &s;
    let r2 = &mut s;   // conflict!
    println!("{}", r1);
    println!("{}", r2);
    */

    // TODO 2: Complete this function-taking-reference pattern.
    let greeting = String::from("Rustians");
    let length = get_length(&greeting);
    println!("'{}' has length {}", greeting, length);

    // TODO 3: Write a function first_word that returns the first word of &str.
    let sentence = String::from("hello world rust");
    let first = first_word(&sentence);
    println!("first word of '{}' is '{}'", sentence, first);
}

fn get_length(s: &String) -> usize {
    // TODO: return the length of s using .len()
    0
}

fn first_word(s: &String) -> &str {
    // TODO: find the first space and return the slice before it.
    // Use a loop over s.bytes() or s.chars().enumerate().
    // If no space, return the whole string.
    let bytes = s.as_bytes();
    for (i, &item) in bytes.iter().enumerate() {
        if item == b' ' {
            return &s[..i];
        }
    }
    &s[..]
}
