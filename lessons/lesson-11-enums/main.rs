// Lesson 11: Enums & Pattern Matching — Main Examples (WORKING)
// Run:
//   rustc main.rs && ./main

#[derive(Debug)]
enum IpAddr {
    V4(u8, u8, u8, u8),
    V6(String),
}

impl IpAddr {
    fn display(&self) -> String {
        match self {
            IpAddr::V4(a, b, c, d) => format!("{}.{}.{}.{}", a, b, c, d),
            IpAddr::V6(s) => s.clone(),
        }
    }
}

#[derive(Debug)]
enum UsState {
    Alabama,
    Alaska,
    // ... other states
}

#[derive(Debug)]
enum Coin {
    Penny,
    Nickel,
    Dime,
    Quarter(UsState),
}

fn value_in_cents(coin: &Coin) -> u8 {
    match coin {
        Coin::Penny => 1,
        Coin::Nickel => 5,
        Coin::Dime => 10,
        Coin::Quarter(_state) => 25,
    }
}

fn main() {
    // --- Enums with data ---
    let home = IpAddr::V4(127, 0, 0, 1);
    let loopback = IpAddr::V6(String::from("::1"));
    println!("home = {}, loopback = {}", home.display(), loopback.display());

    // --- Option ---
    let some_number = Some(5);
    let no_number: Option<i32> = None; // Option needs a type when None
    println!("some_number = {:?}, no_number = {:?}", some_number, no_number);

    // --- Match with Option ---
    let x = plus_one(some_number);
    let y = plus_one(no_number);
    println!("plus_one(Some(5)) = {:?}, plus_one(None) = {:?}", x, y);

    // --- Match on coins ---
    let penny = Coin::Penny;
    let quarter = Coin::Quarter(UsState::Alabama);
    println!("penny = {} cents, quarter = {} cents",
             value_in_cents(&penny), value_in_cents(&quarter));

    // --- if let ---
    let config_max = Some(3u8);
    if let Some(max) = config_max {
        println!("The maximum is configured to be {}", max);
    } else {
        println!("No max configured");
    }

    // --- catch-all with _ ---
    let number = 7u32;
    match number {
        1 => println!("One"),
        2 | 3 => println!("Two or three"),
        4..=6 => println!("Four through six"),
        _ => println!("Something else: {}", number),
    }
}

fn plus_one(x: Option<i32>) -> Option<i32> {
    match x {
        Some(i) => Some(i + 1),
        None => None,
    }
}
