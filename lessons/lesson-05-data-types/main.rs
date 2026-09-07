// Lesson 5: Data Types — Main Examples
// Run:
//   rustc main.rs && ./main

use std::mem::size_of;

fn main() {
    // --- Integer types with suffixes ---
    let a = 57u8;
    let b = 1_000_000;      // i32 (default)
    let c = 0xff;           // hex  -> 255
    let d = 0o77;           // octal -> 63
    let e = 0b1111_0000;    // binary -> 240
    println!("Integers: {} {} {} {} {}", a, b, c, d, e);

    // --- Floats ---
    let f = 2.0;    // f64
    let g: f32 = 3.0;
    println!("Floats: {} {} (f64 and f32)", f, g);

    // --- Booleans ---
    let t = true;
    let f_val: bool = false;
    println!("Booleans: {} {}", t, f_val);

    // --- Characters (Unicode) ---
    let heart = '❤';
    let z = 'ℤ';
    println!("Chars: {}, {}", heart, z);

    // --- Tuples ---
    let tup: (i32, f64, u8) = (500, 6.4, 1);
    let (x, y, z2) = tup;   // destructuring
    println!("Tuple destructured: {} {} {}", x, y, z2);
    println!("Tuple index access: {}", tup.0);

    // --- Arrays ---
    let arr = [1, 2, 3, 4, 5];     // [i32; 5]
    let repeated = [3; 5];          // [3, 3, 3, 3, 3]
    println!("Array first element: {}", arr[0]);
    println!("Repeated array: {:?}", repeated);

    // --- Byte sizes ---
    println!("--- Byte sizes ---");
    println!("i32: {} bytes", size_of::<i32>());
    println!("f64: {} bytes", size_of::<f64>());
    println!("char: {} bytes", size_of::<char>());
    println!("bool: {} bytes", size_of::<bool>());
    println!("usize: {} bytes", size_of::<usize>());

    // --- Uncomment this line to see a bounds-check panic ---
    // println!("{}", arr[10]); // PANICS: index out of bounds
}
