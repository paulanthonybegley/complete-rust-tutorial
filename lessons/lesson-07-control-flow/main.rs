// Lesson 7: Control Flow — Main Examples
// Run:
//   rustc main.rs && ./main

fn main() {
    // --- if / else if / else ---
    let number = 7;
    if number < 5 {
        println!("small");
    } else if number < 10 {
        println!("medium");
    } else {
        println!("large");
    }

    // --- if as an expression ---
    let condition = true;
    let x = if condition { 5 } else { 6 };
    println!("if-expression x = {}", x);

    // --- loop with break value ---
    let mut counter = 0;
    let result = loop {
        counter += 1;
        if counter == 10 {
            break counter * 2;
        }
    };
    println!("loop return value = {}", result);

    // --- while loop ---
    let mut n = 3;
    while n > 0 {
        println!("while n = {}", n);
        n -= 1;
    }

    // --- for loop over a range ---
    println!("for 0..5:");
    for i in 0..5 {
        print!("{} ", i);
    }
    println!();

    println!("for 0..=5 (inclusive):");
    for i in 0..=5 {
        print!("{} ", i);
    }
    println!();

    // --- break and continue ---
    print!("break/continue: ");
    for i in 0..10 {
        if i == 2 {
            continue;
        }
        if i == 5 {
            break;
        }
        print!("{} ", i);
    }
    println!();

    // --- FizzBuzz (Activity 1) ---
    println!("FizzBuzz:");
    fizzbuzz(15);

    // --- Factorial (Activity 2) ---
    println!("5! = {}", factorial(5));
}

fn fizzbuzz(limit: u32) {
    for i in 1..=limit {
        if i % 15 == 0 {
            println!("FizzBuzz");
        } else if i % 3 == 0 {
            println!("Fizz");
        } else if i % 5 == 0 {
            println!("Buzz");
        } else {
            println!("{}", i);
        }
    }
}

fn factorial(n: u32) -> u64 {
    let mut result: u64 = 1;
    for i in 2..=n {
        result *= i as u64;
    }
    result
}
