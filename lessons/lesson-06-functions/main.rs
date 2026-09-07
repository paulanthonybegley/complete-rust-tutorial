// Lesson 6: Functions — Main Examples
// Run:
//   rustc main.rs && ./main

fn main() {
    // Call functions
    let sum = add(3, 4);
    println!("add(3, 4) = {}", sum);

    let area = rectangle_area(5, 10);
    println!("Area of 5x10 = {}", area);

    let temp = fahrenheit_to_celsius(212.0);
    println!("212°F = {}°C", temp);

    let nums = [34, 7, 100, 1, 55];
    let (min, max) = stats(&nums);
    println!("min/max of {:?} = {} / {}", nums, min, max);

    // Statement vs expression demo
    let y = {
        let x = 3;
        x + 1 // expression, no semicolon
    };
    println!("block expression y = {}", y);

    // if as an expression
    let label = if y > 3 { "big" } else { "small" };
    println!("y is {}", label);
}

fn add(a: i32, b: i32) -> i32 {
    a + b // last expression (no semicolon) -> returns a + b
}

fn rectangle_area(width: u32, height: u32) -> u32 {
    width * height
}

fn fahrenheit_to_celsius(f: f64) -> f64 {
    (f - 32.0) * 5.0 / 9.0
}

fn stats(nums: &[i32]) -> (i32, i32) {
    let mut min = nums[0];
    let mut max = nums[0];
    for &n in nums {
        if n < min { min = n; }
        if n > max { max = n; }
    }
    (min, max) // return a tuple
}
