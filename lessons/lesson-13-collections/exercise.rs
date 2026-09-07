// Lesson 13: Collections (Vec, String, HashMap) — Exercise
// Complete the TODOs to build a simple contact manager.
// Run:
//   rustc exercise.rs && ./exercise

use std::collections::HashMap;

fn main() {
    // TODO 1: Create a Vec of numbers 1..=5 (use a range + collect).
    let numbers: Vec<i32> = (1..=5).collect();
    println!("numbers = {:?}", numbers);

    // TODO 2: Append 6 to that Vec (needs to be mutable).
    // let mut numbers = ...;
    // println!("after push: {:?}", numbers);

    // TODO 3: Build a HashMap<String,String> contact list.
    // Add "Alice" -> "555-0100" and "Bob" -> "555-0111".
    let mut contacts: HashMap<String, String> = HashMap::new();
    // TODO insert contacts

    // TODO 4: Look up Bob's number safely and print it (handle missing).
    // Use .get() which returns Option, and a default for missing.
    // let bob = contacts.get("Bob");
    // println!("Bob: {}", bob.unwrap_or(&String::from("unknown")));

    // TODO 5: For the word counter, use entry() to count each word
    // in the sentence below.
    let sentence = "the cat sat on the mat the";
    let mut word_count: HashMap<&str, u32> = HashMap::new();
    for word in sentence.split_whitespace() {
        // TODO: word_count.entry(word).or_insert(0); *count += 1;
    }
    println!("word_count = {:?}", word_count);
}
