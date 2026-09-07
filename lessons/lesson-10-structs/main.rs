// Lesson 10: Structs — Main Examples (WORKING)
// Run:
//   rustc main.rs && ./main

#[derive(Debug)]
struct User {
    active: bool,
    username: String,
    email: String,
    sign_in_count: u64,
}

#[derive(Debug)]
struct Rectangle {
    width: u32,
    height: u32,
}

impl Rectangle {
    // Method: immutable borrow (read)
    fn area(&self) -> u32 {
        self.width * self.height
    }
    fn width(&self) -> bool {
        self.width > 0
    }
    // Method: takes another rect
    fn can_hold(&self, other: &Rectangle) -> bool {
        self.width > other.width && self.height > other.height
    }
    // Associated function (no self) — a "constructor"
    fn square(size: u32) -> Rectangle {
        Rectangle { width: size, height: size }
    }
}

fn main() {
    // Create an instance
    let mut user1 = User {
        active: true,
        username: String::from("alice"),
        email: String::from("alice@example.com"),
        sign_in_count: 1,
    };

    // Access & (with mut) modify fields
    println!("email: {}", user1.email);
    user1.email = String::from("new@example.com");
    println!("updated email: {}", user1.email);

    // Debug print
    println!("{:#?}", user1);

    // Struct update syntax — moves String fields out of user1
    let user2 = User {
        email: String::from("bob@example.com"),
        ..user1
    };
    println!("user2: {} / {}", user2.username, user2.email);

    // Methods
    let rect1 = Rectangle { width: 30, height: 50 };
    let rect2 = Rectangle { width: 10, height: 40 };
    let sq = Rectangle::square(8);

    println!("rect1 area = {}", rect1.area());
    println!("rect1 has nonzero width: {}", rect1.width());
    println!("rect1 can hold rect2: {}", rect1.can_hold(&rect2));
    println!("square area = {}", sq.area());
}
