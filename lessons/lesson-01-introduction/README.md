# Lesson 1: Introduction to Rust — Walkthrough

## Learning Goal
Understand what Rust is, why it exists, and where it's used — before writing any code.

---

## 1. What is Rust?

Rust is a **systems programming language** that prioritizes three things:

| Priority | Meaning |
|----------|---------|
| **Safety** | Memory safety without a garbage collector |
| **Speed** | Comparable performance to C/C++ (zero-cost abstractions) |
| **Concurrency** | Fearless concurrency — data races are caught at compile time |

**Origin:** Created by Graydon Hoare at Mozilla (2006), released 1.0 in 2015. Now maintained by the independent Rust Foundation.

**Design philosophy:** *"If it compiles, it works"* — Rust's compiler is famously strict, catching bugs (use-after-free, data races, null dereference) at compile time rather than runtime.

---

## 2. Who uses Rust?

| Company | What they use it for |
|---------|----------------------|
| **Mozilla** | The original home (Firefox's CSS engine: Servo) |
| **Firefox** | Rendering engine components |
| **Android** | Memory-safe components in the OS |
| **Linux kernel** | Rust support officially merged (2022+) |
| **Discord** | Read states, backend services |
| **Cloudflare** | Edge networking infrastructure |
| **Microsoft** | Memory-safe systems (Azure, Windows components) |
| **Amazon (Firecracker)** | Serverless virtualization |

---

## 3. Why choose Rust over C/C++?

C/C++ give you speed but not safety — buffer overflows, null pointers, and use-after-free bugs are common and catastrophic. Rust gives you **the same speed with safety guaranteed by the compiler**.

| Feature | C/C++ | Rust |
|---------|-------|------|
| Memory safety | Manual (easy to get wrong) | Compile-time enforced |
| Garbage collector | None | None |
| Null pointers | Yes (segfaults) | `Option<T>` (safe) |
| Data races | Possible | Impossible at compile time |
| Build tool | Fragmented (make, cmake) | Cargo (unified) |

---

## 4. Activity: Research a Real-World Rust Project

Present a 3-minute summary of one Rust project. Identify:
- What the project does
- Why **Rust** specifically was chosen (over C/C++/Go/Java)
- What safety or performance problem Rust solved

**Suggested projects:**
- Firecracker (Amazon) — microVMs for serverless
- ripgrep — a ridiculously fast search tool
- Alacritty — GPU-accelerated terminal
- Diesel — ORM (database) for Rust
- Tokio — async runtime
- Servo — parallel browser engine

---

## 5. Assessment

Write a **one-paragraph** explanation of Rust's value proposition for a **non-technical** audience (your manager, a product owner, or a friend). Avoid jargon or explain it if you use it. Answer: *"Why should an organization use Rust?"*

---

## How to run this lesson's examples

This lesson is conceptual. The accompanying file `examples.rs` contains simple demos you can run after completing Lesson 3 (Cargo setup).

```bash
# After setting up Rust (Lesson 2):
rustc examples.rs
./examples
```
