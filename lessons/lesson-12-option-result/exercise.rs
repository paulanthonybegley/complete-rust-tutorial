// Lesson 12: Option, Result & Error Handling — Exercise
// Build a number-summing program without panics.
// Complete the TODOs.
// Run:
//   rustc exercise.rs && ./exercise

use std::io;

fn main() {
    // TODO 1: Read a line of user input, parse it as an i32, and
    // handle the Result gracefully. Print the number or a friendly error.
    println!("Enter a number:");
    let mut input = String::new();
    // io::stdin().read_line(&mut input) returns Result — handle it with ? or match
    let _trimmed = input.trim();

    // TODO 2: Given the Option below, print a default of 100 if None.
    let maybe: Option<i32> = None;
    // println!("value = {}", ...); // use unwrap_or

    // TODO 3: Complete this function to parse and double a string.
    let a = try_double("21");
    let b = try_double("not a number");
    println!("try_double('21') = {:?}", a);
    println!("try_double('bad') = {:?}", b);
}

// Return Option<i32>: Some(value*2) if it parses, None otherwise.
// Do NOT panic — use match or ok()/map.
fn try_double(s: &str) -> Option<i32> {
    // Use s.parse::<i32>() which returns Result<i32, _>
    // Convert to Option, double it.
    // TODO
    None
}
