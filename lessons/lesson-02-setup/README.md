# Lesson 2: Setup & Environment — Walkthrough

## Learning Goal
Install the Rust toolchain and configure a working development environment.

---

## 1. What we're installing

| Tool | Purpose |
|------|---------|
| **rustup** | The Rust *toolchain manager* — installs/updates Rust versions |
| **rustc** | The Rust **compiler** — turns `.rs` source into binaries |
| **cargo** | The Rust **build system & package manager** |
| **rust-analyzer** | IDE language server (smart completion, on-the-fly errors) |

---

## 2. Install Rust with rustup

Rust's official installer is **rustup**. It works on Linux, macOS, and Windows.

### macOS / Linux
```bash
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
```

### Windows
Download and run `rustup-init.exe` from [rustup.rs](https://rustup.rs/).

Follow the on-screen prompts (default options are fine).

### Verify the installation
Open a **new** terminal and run:
```bash
rustc --version
cargo --version
rustup --version
```

You should see output like:
```
rustc 1.91.1 (ea2d97820 2025-10-10)
cargo 1.91.1 (ea2d97820 2025-10-10)
rustup 1.28.1
```

> **Troubleshooting:** If `cargo` isn't found, ensure `~/.cargo/bin` is on your `PATH`.

---

## 3. `rustc` vs `cargo` — know the difference

| | `rustc` | `cargo` |
|--|---------|---------|
| Role | The **compiler** itself | The **build tool / package manager** |
| Compiles | A single file | A whole project with dependencies |
| Example | `rustc hello.rs` | `cargo build` |
| Used for | Learning, quick tests | Real projects |

> **Rule of thumb:** For real projects, always use `cargo`. `rustc` is mainly for quick single-file tests.

---

## 4. Set up your editor (VS Code)

1. Install [Visual Studio Code](https://code.visualstudio.com/)
2. Install the **rust-analyzer** extension
   - Open Extensions (`Cmd+Shift+X`)
   - Search "rust-analyzer"
   - Install (from the Rust language server project)

Optional but helpful:
- **CodeLLDB** — debugging support
- **Even Better TOML** — colors for `Cargo.toml`

**rust-analyzer** gives you live type information, autocomplete, go-to-definition, and real-time compiler errors — this is what makes working in Rust pleasant.

---

## 5. Activity: Create and run your first project

```bash
cargo new hello_world
cd hello_world
cargo run
```

You should see:
```
   Compiling hello_world v0.1.0 (...)
    Finished `dev` profile [unoptimized + debuginfo] target(s) in ...
     Running `target/debug/hello_world`
Hello, world!
```

Take a **screenshot** of your terminal output for your records.

---

## 6. Assessment

1. Successfully install Rust and run your first project (above).
2. Document **any issues** you encountered while installing and how you solved them.
   - What OS are you on?
   - Any PATH or permission problems?
   - What did the fix look like?

---

## How to run this lesson's examples

The `verify.rs` file prints your environment details. Run it:

```bash
rustc verify.rs
./verify
```

This confirms `rustc` is working. (cargo projects come in Lesson 3.)
