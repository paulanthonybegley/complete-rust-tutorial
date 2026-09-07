// Lesson 6: Functions — Exercise
// The functions below are BROKEN. Fix each and note why it fails.
// Run:
//   rustc exercise.rs && ./exercise  (after you fix them)

fn main() {
    // Fix 1: this function body has a problem (semicolon makes it a statement)
    let a = broken1();
    println!("broken1 -> {}", a);

    // Fix 2: parameter needs mutability to be modified
    let mut value = 10;
    broken2(&mut value);
    println!("broken2 -> {}", value);
}

// Fix 1: change the body so it returns 5 (hint: remove the semicolon)
fn broken1() -> i32 {
    5;
}

// Fix 2: make this perform the increment correctly
fn broken2(x: &mut i32) {
    x = x + 1; // this is wrong — you can't reassign a reference. Fix it.
}

/*
TODO for you:
1. Explain WHY broken1 returns () instead of 5.
2. Explain the difference between x = x + 1 and *x = *x + 1 for a &mut i32.
*/
