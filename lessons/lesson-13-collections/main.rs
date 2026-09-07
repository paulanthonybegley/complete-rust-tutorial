// Lesson 13: Collections (Vec, String, HashMap) — Main Examples (WORKING)
// Run:
//   rustc main.rs && ./main

use std::collections::HashMap;

fn main() {
    // --- Vec ---
    let mut v = Vec::new();
    v.push(1); v.push(2); v.push(3);
    println!("v = {:?}, len = {}", v, v.len());
    v.pop();
    println!("after pop: {:?}", v);

    let v2 = vec![1, 2, 3];
    let first = v2.get(0);   // Option<&i32> — safe
    let out = v2.get(99);    // None — safe, no panic
    println!("v2.get(0) = {:?}, v2.get(99) = {:?}", first, out);
    // let bad = v2[99];     // PANICS — don't do this

    // --- String vs &str ---
    let mut s = String::from("hello");
    s.push('!');
    s.push_str(" world");
    println!("growable String: {}", s);

    let literal: &str = "fixed";   // &str is immutable
    println!("&str literal: {}", literal);

    // iterate chars (UTF-8 safe)
    for c in "héllo".chars() {
        print!("{} ", c);
    }
    println!();

    // --- HashMap ---
    let mut scores = HashMap::new();
    scores.insert(String::from("Blue"), 10);
    scores.insert(String::from("Yellow"), 50);

    match scores.get("Blue") {
        Some(&v) => println!("Blue = {}", v),
        None => println!("No Blue"),
    }
    println!("Green exists? {}", scores.contains_key("Green"));

    // word-count idiom with entry()
    let text = "hello world hello rust hello";
    let mut count_map = HashMap::new();
    for word in text.split_whitespace() {
        let count = count_map.entry(word).or_insert(0);
        *count += 1;
    }
    println!("Word counts: {:?}", count_map);

    // iterate a map (unordered)
    for (key, value) in &scores {
        println!("{} = {}", key, value);
    }
}
