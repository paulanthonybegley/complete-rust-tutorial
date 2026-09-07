// Lesson 9: Borrowing & References — Main Examples (WORKING)
// Run:
//   rustc main.rs && ./main

fn main() {
    // --- Immutable reference (borrow) ---
    let s = String::from("hello");
    let len = calculate_length(&s); // borrow, not move
    println!("The length of '{}' is {}.", s, len); // s still usable!

    // --- Multiple immutable borrows allowed ---
    let r1 = &s;
    let r2 = &s;
    println!("r1 = {}, r2 = {}, s = {}", r1, r2, s); // all fine

    // --- Mutable reference (single) ---
    let mut msg = String::from("hello");
    add_word(&mut msg);
    println!("After add_word: {}", msg);

    // --- Borrowing rules demonstration ---
    let mut data = String::from("data");
    {
        let _immut = &data;       // immutable borrow (multiple OK)
        let _immut2 = &data;
        // let _mut = &mut data;  // ERROR if uncommented: also borrowed immutably
        println!("immutable borrows: {} {}", _immut, _immut2);
    } // immutable borrows end here
    let mut_ref = &mut data;      // now OK — no active borrows
    mut_ref.push_str(" updated");
    println!("after mutable borrow: {}", data);

    // --- Slices ---
    let phrase = String::from("hello world");
    let hello = &phrase[0..5];
    let world = &phrase[6..11];
    println!("slices: '{}' and '{}'", hello, world);
}

fn calculate_length(s: &String) -> usize {
    s.len() // s is a reference; we borrow it
}

fn add_word(s: &mut String) {
    s.push_str(" world"); // mutate through the mutable reference
}
