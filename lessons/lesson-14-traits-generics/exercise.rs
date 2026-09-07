// Lesson 14: Traits & Generics — Exercise
// Complete the TODOs to define a Shape trait and use generics.
// Run:
//   rustc exercise.rs && ./exercise
// (You'll need to finish the TODOs first.)

use std::fmt::Display;

// TODO 1: Define a trait `Shape` with an `area(&self) -> f64` method.
// pub trait Shape { fn ... }

// TODO 2: Define a Circle struct { radius: f64 } and implement Shape for it.
pub struct Circle { pub radius: f64 }

// TODO 3: Define a Square struct { side: f64 } and implement Shape for it.
pub struct Square { pub side: f64 }

// TODO 4: Write a generic function `print_area<T: Shape>(s: &T)`
// that prints the area using {:.2} formatting.
// fn print_area<T: Shape>(s: &T) {
//     println!("Area: {:.2}", s.area());
// }

// TODO 5: Implement Display for Circle (format like "Circle(r=2.0)")
// so it can be used in multi-bound contexts.

fn main() {
    let c = Circle { radius: 2.0 };
    let s = Square { side: 3.0 };

    // Test area() on each
    // println!("Circle area: {:.2}", c.area());
    // println!("Square area: {:.2}", s.area());

    // Test the generic print_area
    // print_area(&c);
    // print_area(&s);

    // TODO 6 (bonus): Build a Vec<Box<dyn Shape>> of both shapes and
    // print their total area by summing .area().
}
