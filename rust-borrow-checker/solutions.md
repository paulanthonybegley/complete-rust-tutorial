# Solutions — Rust's Borrow Checker Finally Makes Sense

Every snippet below was compiled **and run** on the pinned toolchain `rustc`
1.91.1 / `cargo` 1.91.1 with `--edition 2021` (same toolchain as the root Rust
course). "FAILS with E####" means `rustc` produced exactly that code; "OK"
means it compiled and the shown output was observed. Reproduce each one with:

```bash
rustc --edition 2021 file.rs -o file.bin && ./file.bin
```

## Session 1 — Why the compiler "argues"

**1.01** The borrow checker tries to prove two things before the program ever
runs:
1. **No reference can outlive the data it points to.**
2. **Memory cannot be mutated while another part of the program is relying on
   it.**

Both are compile-time claims — the checker proves them statically, which is
exactly what "not garbage collection, not reference counting" means: neither
runs anything; both proofs happen before execution.

**1.02**
- `E0106` (missing lifetime on `fn dangle() -> &String`) enforces guarantee
  **#1** — the returned reference would outlive the local `String` it points
  to. (Verified: `fn dangle() -> &String` fails immediately with `E0106`,
  "this function's return type contains a borrowed value, but there is no
  value for it to be borrowed from".)
- `E0502` (cannot borrow `v` as mutable because it is also borrowed as
  immutable) enforces guarantee **#2** — `push` mutates a `Vec` while `&v[0]`
  is still relied upon.

**1.03** The borrow checker is a **compile-time system that tracks ownership,
aliasing, mutation, and reference validity closely enough to eliminate entire
categories of memory bugs before the program runs.** Read errors as "the
compiler is asking me to make this safety claim *provable*", not "the compiler
dislikes my code" — the reframe turns debugging from appeasement into
reasoning about what can be proven.

## Session 2 — Many readers OR one writer

**2.01** "At any time you may have **any number of** shared references **or
exactly one** mutable reference, **never** both." (General rule: many `&T`
*or* one `&mut T`; never a `&T` and a `&mut T` in the same live region.)

**2.02**
- (a) **many-readers — compiles.** Two immutable borrows of `x` coexist fine.
  Verified OK.
- (b) **reader + writer — FAILS with `E0502`.** `r` (immutable) is created
  before `m` (mutable), and `r` is used *after* `m` exists, so the two
  borrows overlap in a region where `m` could mutate what `r` reads. Verified.
- (c) **two writers — FAILS with `E0499`** (cannot borrow `x` as mutable more
  than once at a time). Even though `m` was used (`*m += 1`) before `m2` was
  created, `*m` is used again in the `println!` *after* `m2`, so both mutable
  borrows are live. Verified.

**2.03** The word **exclusive** in `&mut T` encodes the guarantee: a mutable
reference is temporary *exclusive* access, so any simultaneously-live shared
reference is a contradiction in terms. The diagnostic question to ask is not
"why does Rust hate two references?" but **"could one of these references read
or write memory that the other assumes is stable?"** If the answer is yes —
whether that's two writers or a reader and a writer — Rust refuses.

## Session 3 — The vector trap

**3.01** FAILS with **`E0502`**, pointing at the `v.push(4)` line (the
"immutable borrow occurs here" marker sits on `&v[0]`, and "immutable borrow
later used here" on the `println!`).

**3.02** `v.push(4)` can force the vector to **grow**, and growth copies the
entire contents to a new, larger allocation. If that happens, the buffer
`first` points at **no longer belongs to anything** — the old allocation is
freed. `first` would dangle. Because `push` *might* reallocate, the checker
rejects the pattern outright rather than wait to see if the buffer happened to
have spare capacity.

**3.03** Non-lexical-lifetime fix — move the last use of `first` before the
`push`:
```rust
fn main() {
    let mut v = vec![1, 2, 3];
    let first = &v[0];
    println!("{}", first);   // last use of the borrow
    v.push(4);               // OK: borrow ended at its last use
    println!("{:?}", v);
}
```
Verified OK; prints `1` then `[1, 2, 3, 4]`.

**3.04** Block-scope fix — end the borrow by closing a scope:
```rust
fn main() {
    let mut v = vec![1, 2, 3];
    {
        let first = &v[0];
        println!("{}", first);
    }                       // borrow ends here, spelled out
    v.push(4);              // OK
    println!("{:?}", v);
}
```
Verified OK. The **block scope** is the more *literal* expression of "borrow a
smaller piece": the extent of the borrow is written in the source. The NLL
reorder relies on the compiler inferring the same boundary from the *last use*
— shorter on the page, but the guarantee is identical. Both are valid; prefer
whichever makes the borrow's extent visible to the reader.

## Session 4 — Lifetimes: relationships, not lifespans

**4.01** FAILS with **`E0106`**: "missing lifetime specifier … this function's
return type contains a borrowed value, but there is no value for it to be
borrowed from." The compiler even offers `'static` (with the caveat it's
uncommon for this) and then the real suggestion: **return an owned value**
(`fn dangle() -> String`). This is the video's point — *you can't even write
the signature*; the relationship is missing, so the type itself is
unwritable. (`s` is dropped at the end of the function, so the reference would
point at memory that no longer exists.)

**4.02** A lifetime describes **how long a reference is valid relative to
other values** — it is a *relationship*, not a lifespan. It doesn't keep an
object alive; it lets the compiler prove the returned reference can't outlive
the data behind it.

**4.03**
```rust
fn longest<'a>(x: &'a str, y: &'a str) -> &'a str {
    if x.len() >= y.len() { x } else { y }
}
```
Verified OK. All three `'a`s are the *same* relationship: the returned
reference is tied to both inputs' shared lifetime region. The body (which
input wins is unknown until runtime) is exactly why the compiler can't infer
this alone — both `x` and `y` are candidates, so the annotation states the
constraint the return respects.

**4.04** With *distinct* lifetimes the **short-lived second argument compiles
fine** — the output is only tied to `'a`, so `&'b` is independent:
```rust
fn longest<'a, 'b>(x: &'a str, _y: &'b str) -> &'a str { x }
```
Verified OK (the `&"short"` literal borrow doesn't constrain the result).
But with **both inputs `'a`**, passing a short-lived borrow *does* constrain
the output: the returned reference is only valid for the region common to both
inputs. Try to use it after the short-lived one dies and you get **`E0597`
(`b` does not live long enough)**:
```rust
let a = String::from("long-lived");
let result;
{                          // b is dropped here
    let b = String::from("short");
    result = longest(&a, &b);
}
println!("{}", result);    // E0597: b dropped while still borrowed
```
This is a perfect demonstration that `'a` isn't "the lifespan of both inputs"
but a *shared relationship* — widening it, narrowing it, or splitting it
changes what the output may claim.

## Session 5 — NLL & the three questions

**5.01** **Yes, it compiles** — the borrow ends after its **last use**, not at
the closing brace. With `println!("{}", head)` as the final use of `head`, the
later `push` has no live immutable borrow to conflict with. Verified OK;
prints `1` then `[1, 2, 3, 4]`.

**5.02** The three questions, in order:
1. **Who owns this data?**
2. **Who currently has permission to access it?**
3. **How long does that permission actually need to last?**

**5.03** Diagnosis with the three questions: `main` owns `s`; `r` has shared
(read) permission via `&s`; `push_str` needs *exclusive* (mutable) permission
*while `r`'s permission is still being relied on* (the `println!("{}", r)`
afterwards). But — shortening the borrow fixes it:
```rust
fn main() {
    let mut s = String::from("hello");
    let r = &s;
    println!("{}", r);          // r's last use — permission no longer needed
    s.push_str(" world");        // OK now
    println!("{}", s);
}
```
**No annotation is involved.** The answer to "how long does the permission
need to last" is *just past the `println!`*, and NLL honors that. Verified OK.

**5.04** The failure is **`E0515`: cannot return reference to temporary
value** — `s.chars().next().unwrap()` creates a temporary `char`, and the
function would return a reference to data owned by the current function (a
dangling reference). Diagnosis: the *temporary*, not a borrow conflict, is the
offender. The fix-menu answer here is **take ownership** of the value the
temporary produced:
```rust
fn first_char(s: &String) -> Option<char> {
    s.chars().next()
}
fn main() {
    println!("{:?}", first_char(&"hi".to_string()));   // Some('h')
}
```
Verified OK. (Borrowing a smaller piece can't help — there is no live piece to
borrow; the value is owned by the iterator chain. Returning owned `char` /
`Option<char>` is the idiomatic fix, echoing `E0106`'s "return an owned
value".)

## Session 6 — When shared mutation is genuinely needed

**6.01** The four wrappers and what each enforces:
- **`RefCell<T>`** — moves borrow checking from **compile time to run time**
  (interior mutability in single-threaded code; it panics on conflicting
  borrows at runtime).
- **`Mutex<T>`** — allows **shared access across threads** by enforcing
  exclusive mutation through **locking**.
- **`Rc<T>`** — gives **multiple ownership in single-threaded code**
  (reference-counted drops).
- **`Arc<T>`** — gives **shared thread-safe ownership** (atomic refcount);
  combine with `Mutex` for thread-safe interior mutation.

**6.02** (a) `Rc<T>` · (b) `Mutex<T>` · (c) `RefCell<T>` · (d) `Arc<T>`.

**6.03** The reframe: instead of "how do I make this compile?" you ask **"what
safety guarantee can the compiler not prove yet?"** The borrow checker stops
being a barrier and becomes a *proof obligation* — you look at the code and
reason about which of the two guarantees the checker can't yet establish, then
restructure the code so the claim becomes provable (or pick a wrapper whose
explicit control moves the obligation to a runtime check or a lock).

**6.04** Compiled-and-panicked at runtime (the point: the compiler accepts it,
`RefCell` delays the check):
```rust
use std::cell::RefCell;
fn main() {
    let cell = RefCell::new(5);
    let _shared = cell.borrow();
    let _exclusive = cell.borrow_mut();   // panic: already borrowed
    println!("{} {}", *_shared, *_exclusive);
}
```
Verified — compiles, then at runtime: **`thread 'main' panicked: RefCell
already borrowed`**. `RefCell` moved the many-readers/one-writer rule from
compile time to run time, so the failure becomes a panic instead of a compile
error — which is exactly the "control visible in the type" trade the video
names.

## Back to the whole system

**7.01**
```rust
fn sum_refs<'a>(x: &'a Vec<i32>, y: &'a Vec<i32>) -> i32 {
    x.iter().sum::<i32>() + y.iter().sum::<i32>()
}
```
Verified OK (prints `36` for `[1,2,3]` + `[10,20]`). With an unrelated
second lifetime:
```rust
fn sum_refs<'a, 'b>(x: &'a Vec<i32>, y: &'b Vec<i32>) -> i32 { ... }
```
**Also compiles** — because the function returns `i32` (owned), nothing ties
the two inputs together; the relationship annotations make that explicit.
What you just exercised: **annotations describe the *relationships* between
references, not their lifespans** — when nothing needs to outlive a borrow,
the relationship is empty and either signature is provable.

**7.02** Prediction first, then the verified truth for the whole block as
written:
```rust
let mut v = vec![1, 2, 3];
let r = &v;
v.push(4);            // (a)
println!("{}", r[0]); // (b) — r used here, AFTER the push
r;                    // (c)
```
The unit **fails at (a) with `E0502`** (not b/c — the mutable borrow is
reported where it happens; `r` is "later used here" at (b)). Compile it as one
program and `rustc` reports `E0502` on the `push` line. The nuance that makes
this worth verifying:

- Remove only the (b)/(c) uses (`let r = &v; r; v.push(4);`) → **compiles**
  (NLL ends the borrow at `r;`, the push then has no live conflict).
- Reorder so the *last use* precedes the push (the 3.03 shape) → **compiles**.

So the honest answer to "which lines fail": **line (a) is rejected** — because
the reference created above it is still alive at (b). The rule is not "push is
banned after a borrow"; it's "the borrow must end (last use) before the
mutation". Your prediction should be about *which borrow is live when* — the
three-questions discipline again.