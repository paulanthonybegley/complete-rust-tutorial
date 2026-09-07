// Lesson 11: Enums & Pattern Matching — Exercise
// Build a TrafficLight enum with a method, and handle a Command enum via match.
// Complete the TODOs.
// Run:
//   rustc exercise.rs && ./exercise

#[derive(Debug)]
enum TrafficLight {
    Red,
    Yellow,
    Green,
}

impl TrafficLight {
    // TODO 1: Return how many seconds this light stays in its color.
    // Red: 60, Yellow: 5, Green: 45
    fn duration(&self) -> u32 {
        // TODO: match on self
        0
    }
}

#[derive(Debug)]
enum Command {
    Add(String),      // add a string to a list
    Remove(usize),    // remove by index
    Clear,            // clear the list
    Quit,             // exit
}

fn main() {
    let red = TrafficLight::Red;
    let yellow = TrafficLight::Yellow;
    let green = TrafficLight::Green;
    println!("Red: {}s, Yellow: {}s, Green: {}s",
             red.duration(), yellow.duration(), green.duration());

    // TODO 2: Write a match expression that handles EVERY Command variant,
    // printing an appropriate message for each. Test all four variants.
    let commands = vec![
        Command::Add(String::from("first")),
        Command::Remove(0),
        Command::Clear,
        Command::Quit,
    ];
    for cmd in &commands {
        // TODO: match cmd (all variants) and print a message
        match cmd {
            // ...
            _ => unreachable!(), // replace this with real arms
        }
    }
}

// TODO 3 (Assessment): Bonus — write the same handling with `if let`
// for just the Quit case, to practice if-let sugar.
