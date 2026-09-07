// Lesson 7: Control Flow — Exercise
// Complete the TODO functions. The file compiles once all TODOs are done.
// Run:
//   rustc exercise.rs && ./exercise

fn main() {
    // Test guess_game helper: call any_number_button() 3 times
    for _ in 0..3 {
        println!("Rolled: {}", any_number_button(7));
    }

    // Test the assessment: rewrite if-chain with match
    for n in [0, 1, 2, 9] {
        println!("describe({}) = {}", n, describe(n));
    }
}

// TODO 1: Return a "sum" of numbers from 1 to `n` using a while loop
// (instead of the for loop shown in the lesson, use `while`).
fn sum_up_to(n: u32) -> u32 {
    let mut total = 0;
    let mut i = 1;
    // TODO: finish with a while loop
    total
}

// TODO 2: This function "rolls" — if the guess equals number, print "hit!"
// and break/return; otherwise print "miss" and continue.
// Use a `loop` with `break`.
fn any_number_button(number: u32) -> &'static str {
    let mut guess = 0;
    // TODO: implement a loop that increments guess and breaks when guess == number
    "hit"
}

// TODO 3 (Assessment): Rewrite this if-chain using `match`.
fn describe(n: u32) -> &'static str {
    if n == 0 {
        "zero"
    } else if n == 1 {
        "one"
    } else if n == 2 {
        "two"
    } else {
        "many"
    }
}
