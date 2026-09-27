# Exercises · Code Smells Course

For each lab: (1) identify the smell in one sentence, (2) state the fix, (3) write a
5–10 line variant (Before) that exhibits the same tell, (4) show the 5–10 line After.

| Lab | Exercise |
|---|---|
| L1 Long Method | Take a method with 3 paragraphs of logic → extract 3 named methods. |
| L2 God Class | Split a class doing billing + emailing + reporting into two classes + a thin coordinator. |
| L3 Data Class | Move one validation rule from a service into the data class. |
| L4 Primitive Obsession | Replace `String email`, `double price` with tiny value objects (validate once). |
| L5 Temporary Field | Remove a sometimes-null field by creating a small parameter object. |
| L6 Feature Envy | Move a getter-heavy method to the class whose data it reads most. |
| L7 Inappropriate Intimacy | Break a bidirectional field read/write by adding a query method on one side. |
| L8 Divergent Change | Split a class that must change for schema + for layout into two single-reason classes. |
| L9 Switch Statements | Introduce a small polymorphic hierarchy for the type you switch on. |
| L10 Lazy Class | Inline a 1-method class into its sole caller. |
| L11 Class Obsession | Collapse one empty intermediate abstract class. |
| L12 Middle Man | Delete a class whose every method is `return delegate.foo()`. |
| L13 Speculative Generality | Remove an unused interface + its single-implementation factory layer. |
| L14 Comments | Rename two variables/methods so 3 redundant comments become unnecessary. |
| L15 Hidden Bugs | Widen a counter to long, enforce bounds, convert a silent equality exclusion to explicit. |
