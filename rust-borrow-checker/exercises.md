# Exercises — Rust's Borrow Checker Finally Makes Sense

Exercises follow the video's reframe arc and mirror the repo's
`lessons/lesson-08-ownership` / `lesson-09-borrowing` material. Levels follow
the notebook ladder (blank / rookie / experienced). Answers in
`solutions.md`. Compile & run any snippet with the pinned edition:

```bash
rustc --edition 2021 file.rs -o file.bin && ./file.bin
```

## Session 1 — Why the compiler "argues"

**1.01 (blank)** State the two things the borrow checker tries to prove before
the program ever runs.

**1.02 (bench)** For each error code below, say which of the two guarantees it
enforces (outliving data? mutation while relied upon? both?):
- `E0106` (missing lifetime specifier on `fn dangle() -> &String`)
- `E0502` (cannot borrow `v` as mutable because also borrowed as immutable)

**1.03 (experienced)** The video says the checker isn't a "pile of annoying
compiler errors" and isn't garbage collection. In one sentence each, say what
it *actually* is, and why that reframe ("proving" not "fighting") changes how
you read an error.

## Session 2 — Many readers OR one writer

**2.01 (blank)** Complete the rule: "at any time you may have ________ shared
references **or** ________ mutable reference(s), never ______."

**2.02 (rookie)** Classify each snippet as `many-readers`, `one-writer`, or
`reader+writer-ok`:
```rust
// (a)
let mut x = 5;
let r = &x;
let r2 = &x;
println!("{} {}", r, r2);
```
```rust
// (b)
let mut x = 5;
let r = &x;
let m = &mut x;   // compiles or not?
println!("{}", r);
```
```rust
// (c)
let mut x = 5;
let m = &mut x;
*m += 1;
let m2 = &mut x;   // compiles or not? why?
println!("{} {}", *m, *m2);
```

**2.03 (experienced)** Explain how the word *exclusive* in `&mut T` explains
most borrow errors. What is the one diagnostic question the video says to ask
instead of "why does Rust hate two references?"

## Session 3 — The vector trap

**3.01 (blank)** Run this and report the error code + the line it points at:
```rust
fn main() {
    let mut v = vec![1, 2, 3];
    let first = &v[0];
    v.push(4);            // would reallocate
    println!("{}", first);
}
```

**3.02 (rookie)** Why is `v.push(4)` the dangerous line here, concretely? What
happens to the buffer `first` points at?

**3.03 (experienced)** Fix the trap by reordering so `first`'s last use comes
before the `push` (non-lexical lifetimes). Verify your fixed snippet compiles.

**3.04 (experienced)** Now fix it a *second* way with an explicit block scope
`{ ... }` that ends the borrow before `push`. Same result — different
technique. Which one (NLL reorder vs block) expresses *borrowing a smaller
slice first* more literally?

## Session 4 — Lifetimes: relationships, not lifespans

**4.01 (blank)** Write `fn dangle() -> &String { let s = String::from("hi"); &s }`
in a file, compile it, and quote the error code. What does the video say about
*even being able to write the signature*?

**4.02 (rookie)** A lifetime is not about keeping an object alive. What does a
lifetime actually describe? (One sentence.)

**4.03 (rookie)** Write the signature for this and make it compile:
```rust
// takes two &str, returns the longer one
fn longest(/* ? */) -> /* ? */ {
    if x.len() >= y.len() { x } else { y }
}
```

**4.04 (experienced)** If a function takes `&'a str` and `&'b str` and returns
`&'a str`, and you pass it a short-lived borrow as the second argument, does it
compile? Why (or why not)? Does the same hold when both inputs are `'a`?

## Session 5 — NLL & the three questions

**5.01 (blank)** Does this compile under non-lexical lifetimes? Run it and
report the output or the error:
```rust
fn main() {
    let mut v = vec![10, 20, 30];
    let head = &v[0];
    println!("{}", head);   // last use of head
    v.push(40);
    println!("{:?}", v);
}
```

**5.02 (blank)** Name the three questions experts ask when the borrow checker
complains.

**5.03 (rookie)** For the snippet below, apply the three questions and state
the diagnosis. Is an annotation the fix? What is?
```rust
fn main() {
    let mut s = String::from("hello");
    let r = &s;           // borrow
    s.push_str(" world"); // mutation while r exists?
    println!("{}", r);
}
```

**5.04 (experienced)** Apply the fix-menu to this failing code: choose between
*shorten the borrow*, *borrow a smaller piece*, or *take ownership*. Show the
fixed version and verify it compiles:
```rust
fn first_char(s: &String) -> &char {
    &s.chars().next().unwrap()
}
fn main() {
    println!("{}", first_char(&"hi".to_string()));
}
```

## Session 6 — When shared mutation is genuinely needed

**6.01 (blank)** Say what each wrapper is for, and what it actually enforces:
`RefCell<T>`, `Mutex<T>`, `Rc<T>`, `Arc<T>`.

**6.02 (rookie)** Match each scenario to a wrapper: (a) multiple ownership in
single-threaded code, (b) shared access across threads, (c) borrow checking
moved from compile time to run time, (d) shared thread-safe ownership.

**6.03 (rookie)** The video's closing reframe: what does the moment you stop
asking "how do I make this compile?" and start asking "what safety guarantee
can the compiler **not** prove yet?" unlock?

**6.04 (experienced)** `RefCell` compiles here — but it can still panic at
runtime. Write the code that *compiles* and panics with the double-borrow
runtime error (`already borrowed`), then confirm the panic message.

---

## Back to the whole system (cross-cutting)

**7.01 (experienced)** Write a function `sum_refs<'a>(x: &'a Vec<i32>,
y: &'a Vec<i32>) -> i32` that returns `x.iter().sum::<i32>() +
y.iter().sum::<i32>()` and drives it from `main`. Now change the second
parameter to an unrelated lifetime `'b`: does it still compile? What proof did
you just exercise about annotations being *relationships*?

**7.02 (experienced)** Predict-then-verify: without running it, say exactly
which of these three lines fail under the 1.91.1 `--edition 2021` checker, and
why, then compile each to confirm your prediction matches reality:
```rust
let mut v = vec![1, 2, 3];
let r = &v;
v.push(4);           // (a)
println!("{}", r[0]); // (b)
r;                   // (c)
```