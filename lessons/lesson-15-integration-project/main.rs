// Integration Project — Full working example: word_counter
// Reads a file, counts lines/words, and reports the top N most frequent words.
// Combines: std::env args, Result error handling, Vec/String/&str,
// HashMap with entry() counting, struct, and impl.
//
// Run:
//   cargo new word_counter
//   # copy this into word_counter/src/main.rs
//   cargo run -- <path-to-text-file>
//
// Model your own project on this.

use std::collections::HashMap;
use std::env;
use std::fs;
use std::process;

fn main() {
    // 1. Read command-line arguments -> Vec<String>
    let args: Vec<String> = env::args().collect();
    if args.len() != 2 {
        eprintln!("Usage: word_counter <file>");
        process::exit(1);
    }
    let filename = &args[1];

    // 2. Read file with Result handling
    let contents = read_file(filename);

    // 3. Analyze
    let report = analyze(&contents);

    // 4. Print report
    report.print();
}

// --- Error handling with Result ---
fn read_file(filename: &str) -> String {
    // `?` would be nice but main() isn't returning Result here,
    // so we match explicitly.
    match fs::read_to_string(filename) {
        Ok(contents) => contents,
        Err(e) => {
            eprintln!("Error reading '{}': {}", filename, e);
            process::exit(1);
        }
    }
}

// --- Struct ---
struct Report {
    lines: usize,
    words: usize,
    chars: usize,
    top_words: Vec<(String, usize)>,
}

// --- Analysis using Vec + String + HashMap ---
fn analyze(contents: &str) -> Report {
    let lines = contents.lines().count();
    let chars = contents.chars().count();

    // Count word frequencies with HashMap + entry()
    let mut freq: HashMap<String, usize> = HashMap::new();
    for word in contents.split_whitespace() {
        // Normalize: lowercase and strip punctuation
        let clean: String = word
            .chars()
            .filter(|c| c.is_alphanumeric())
            .collect::<String>()
            .to_lowercase();
        if !clean.is_empty() {
            *freq.entry(clean).or_insert(0) += 1;
        }
    }
    let words = freq.values().sum::<usize>();

    // Collect and sort: Vec of (String, usize) pairs, sorted by count desc
    let mut items: Vec<(String, usize)> = freq.into_iter().collect();
    items.sort_by(|a, b| b.1.cmp(&a.1)); // descending

    Report {
        lines,
        words,
        chars,
        top_words: items.into_iter().take(5).collect(),
    }
}

impl Report {
    fn print(&self) {
        println!("===== Word Counter Report =====");
        println!("Lines : {}", self.lines);
        println!("Words : {}", self.words);
        println!("Chars : {}", self.chars);
        println!("Top 5 most frequent words:");
        for (word, count) in &self.top_words {
            println!("  {:>6}  {}", count, word);
        }
    }
}

// --- Unit tests (at least 5) ---
#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn analyze_counts_lines() {
        let report = analyze("one\ntwo\nthree\n");
        assert_eq!(report.lines, 3);
    }

    #[test]
    fn analyze_counts_words() {
        let report = analyze("the cat sat on the mat");
        assert_eq!(report.words, 6);
    }

    #[test]
    fn analyze_counts_chars() {
        let report = analyze("abc");
        assert_eq!(report.chars, 3);
    }

    #[test]
    fn analyze_normalizes_case_and_punctuation() {
        let report = analyze("Hello, HELLO hello!");
        // all become "hello" -> count 3
        assert_eq!(report.words, 3);
        assert_eq!(report.top_words[0], ("hello".to_string(), 3));
    }

    #[test]
    fn analyze_sorts_top_words_descending() {
        let report = analyze("a a a b b c");
        assert_eq!(report.top_words[0].0, "a");
        assert_eq!(report.top_words[0].1, 3);
        assert_eq!(report.top_words[1].0, "b");
        assert_eq!(report.top_words[2].0, "c");
    }

    #[test]
    fn analyze_handles_empty_input() {
        let report = analyze("");
        assert_eq!(report.lines, 0);
        assert_eq!(report.words, 0);
        assert!(report.top_words.is_empty());
    }
}
