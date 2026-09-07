// Lesson 8: Ownership — Main Examples (WORKING)
// Run:
//   rustc main.rs && ./main

fn main() {
    // --- Drop at end of scope ---
    {
        let s = String::from("hello");
        println!("Inside scope: {}", s);
    } // s dropped here

    // --- Move semantics ---
    let s1 = String::from("hello");
    let s2 = s1; // s1 MOVED into s2
    println!("s2 = {}", s2);
    // println!("{}", s1); // ERROR: value moved

    // --- Copy types (i32 is Copy) ---
    let x = 5;
    let y = x; // copied, not moved
    println!("x = {}, y = {}", x, y); // both valid

    // --- Clone (deep copy) ---
    let a = String::from("deep");
    let b = a.clone();
    println!("a = {}, b = {}", a, b); // both valid!

    // --- Function takes ownership ---
    let owned = String::from("moving");
    takes_ownership(owned); // owned moved in
    // println!("{}", owned); // ERROR: moved

    // --- Function gives back ownership ---
    let c = String::from("round trip");
    let c = gives_back(c); // moved in, returned, re-bound
    println!("c = {}", c);
}

fn takes_ownership(s: String) {
    println!("took ownership of: {}", s);
} // s dropped here

fn gives_back(s: String) -> String {
    s
}
