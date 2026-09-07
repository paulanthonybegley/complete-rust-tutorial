// Lesson 3: Cargo & Hello World
// Drop this file into a `cargo new` project's src/main.rs, then `cargo run`.
// Demonstrates a few println! formatting options.

fn main() {
    // Basic println!
    println!("Hello, world!");

    // Printing with a format argument
    let name = "Rust";
    println!("Hello, {}!", name);

    // Multiple arguments / positional formatting
    println!("{0} starts with {1} and ends with {1}.", "Rust", "t!");

    // Named arguments
    println!("Learning {language} is {adjective}.",
             language = "Rust",
             adjective = "safe");

    // Debug formatting
    let numbers = [1, 2, 3];
    println!("An array in debug form: {:?}", numbers);
}
