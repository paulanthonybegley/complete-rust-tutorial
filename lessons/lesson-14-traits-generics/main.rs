// Lesson 14: Traits & Generics — Main Examples (WORKING)
// Run:
//   rustc main.rs && ./main

use std::fmt::Display;

// --- A trait ---
pub trait Summary {
    fn summarize(&self) -> String;
}

// --- Generic function with trait bound ---
fn notify<T: Summary>(item: &T) {
    println!("Breaking news: {}", item.summarize());
}

// --- Two structs implementing Summary ---
pub struct Article { pub headline: String, pub body: String }
pub struct Tweet { pub text: String, pub author: String }

impl Summary for Article {
    fn summarize(&self) -> String {
        format!("{}, ...", self.headline)
    }
}
impl Summary for Tweet {
    fn summarize(&self) -> String {
        format!("{}: {}", self.author, self.text)
    }
}

// --- Generic largest function ---
fn largest<T: PartialOrd + Copy>(list: &[T]) -> T {
    let mut largest = list[0];
    for &item in list {
        if item > largest {
            largest = item;
        }
    }
    largest
}

// --- Multiple trait bounds with where clause ---
fn print_summary<T>(item: &T)
where
    T: Summary + Display,
{
    println!("{} — {}", item, item.summarize());
}
// (Article & Tweet implement Summary but not Display; we only call notify with them.)

// --- impl Trait return position ---
fn make_article() -> impl Summary {
    Article { headline: String::from("Rust is great"), body: String::from("...") }
}

// --- Dynamic dispatch: Vec<Box<dyn Summary>> ---
fn collect_summaries() -> Vec<Box<dyn Summary>> {
    vec![
        Box::new(Article { headline: String::from("One"), body: String::from("a") }),
        Box::new(Tweet { text: String::from("hi"), author: String::from("alice") }),
    ]
}

fn main() {
    let article = Article { headline: String::from("World news"), body: String::from("...") };
    let tweet = Tweet { text: String::from("Hello world"), author: String::from("bob") };

    notify(&article);
    notify(&tweet);

    // Generic largest
    let nums = [34, 7, 100, 1, 55];
    println!("largest number: {}", largest(&nums));
    let chars = ['y', 'm', 'a', 'q'];
    println!("largest char: {}", largest(&chars));

    // impl Trait return
    let s = make_article();
    println!("make_article summarize: {}", s.summarize());

    // Dynamic dispatch
    let list = collect_summaries();
    for item in &list {
        println!("-> {}", item.summarize());
    }
}
