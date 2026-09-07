// Lesson 10: Structs — Exercise
// Complete the TODOs to build a Student struct with methods.
// Run:
//   rustc exercise.rs && ./exercise

#[derive(Debug)]
struct Student {
    name: String,
    year: u32,
    grades: Vec<f64>,
}

impl Student {
    // TODO 1: Write an associated function `new` that takes a name and
    // year and returns a Student with an empty grades vector.
    fn new(name: String, year: u32) -> Student {
        // TODO
        Student { name: String::new(), year: 0, grades: Vec::new() }
    }

    // TODO 2: Write a method `add_grade` that takes &mut self and a grade,
    // and pushes it onto self.grades.
    fn add_grade(&mut self, grade: f64) {
        // TODO: self.grades.push(grade)
    }

    // TODO 3: Write a method `average_grade` (&self) returning f64.
    // Return the average of self.grades (0.0 if empty).
    fn average_grade(&self) -> f64 {
        // TODO
        0.0
    }
}

fn main() {
    let mut student = Student::new(String::from("Alice"), 2);
    student.add_grade(85.0);
    student.add_grade(90.0);
    student.add_grade(78.0);

    println!("{:#?}", student);
    println!("Average grade: {}", student.average_grade());
}
