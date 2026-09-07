// Lesson 12: Option, Result & Error Handling — Main Examples (WORKING)
// Run:
//   rustc main.rs && ./main

use std::fs::File;
use std::io::{self, Read};

fn main() {
    // --- Option basics ---
    let present = Some(5);
    let absent: Option<i32> = None;

    println!("present.is_some() = {}", present.is_some());
    println!("absent.is_none() = {}", absent.is_none());

    // Safe ways to get values out
    println!("present.unwrap_or(0) = {}", present.unwrap_or(0));
    println!("absent.unwrap_or(0) = {}", absent.unwrap_or(0));

    // match on Option
    let val = match present {
        Some(v) => v * 2,
        None => 0,
    };
    println!("matched Some(5)*2 = {}", val);

    // Don't compile: Option<i32> + i32 is an error
    // let sum = present + 1; // ERROR

    // --- Result (file reading with ?) ---
    match read_username_from_file() {
        Ok(name) => println!("Read user: {}", name),
        Err(e) => println!("Could not read user: {}", e),
    }

    // --- Parsing a number returns Result ---
    let parsed: Result<i32, _> = "42".parse();
    match parsed {
        Ok(n) => println!("Parsed 42 as: {}", n),
        Err(e) => println!("Parse error: {}", e),
    }

    let parsed_bad: Result<i32, _> = "not-a-number".parse();
    println!("'not-a-number' parse result is_err: {}", parsed_bad.is_err());

    // --- unwrap / expect (use sparingly) ---
    // These panic if things go wrong:
    // let n = "42".parse::<i32>().unwrap();       // fine here
    // let n = "abc".parse::<i32>().expect("parse failed"); // would panic
}

// Uses `?` to propagate errors — clean, no panics.
fn read_username_from_file() -> Result<String, io::Error> {
    let mut file = File::open("hello.txt")?; // if Err, return it
    let mut s = String::new();
    file.read_to_string(&mut s)?;
    Ok(s)
}
