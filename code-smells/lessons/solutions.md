# Solutions · Code Smells Course

Short, pedagogical answers. The app's Before/After are the canonical reference.

| Smell | Identify | Fix | Key metric move |
|---|---|---|---|
| Long Method | One method does multiple steps; too large to hold in mind. | Extract Method (+ Extract Query, Parameter Object). | longestMethod ↓, methodCount ↑ slightly, readability ↑. |
| God Class | One class knows/does too many responsibilities. | Extract Class per concern; keep Facade if needed. | fieldCount distributed, typeMentions per class ↓. |
| Data Class | Fields + getters/setters only; behaviour lives elsewhere. | Move Behaviour into the class. | methodCount on class ↑, service coupling ↓. |
| Primitive Obsession | Domain ideas modelled as String/double/int. | Replace Primitive with Object (value objects). | typeMentions ↑ (new types), validation duplication ↓. |
| Temporary Field | Fields only meaningful on some paths (sometimes-null). | Extract Class or hoist to method parameters. | fieldCount ↓, null guards ↓. |
| Feature Envy | Method reads more from another class than its own. | Move Method to the data owner. | coupling (getter-chains) ↓. |
| Inappropriate Intimacy | Classes know each other's internals (bidirectional). | Unidirectional, Extract Class, Law of Demeter. | bidirectional edges ↓, encapsulation ↑. |
| Divergent Change | One class changed for unrelated reasons. | Extract Class per reason to change (SRP). | changes per file ↓. |
| Switch Statements | Same switch/if-chain repeated for same type. | Replace Conditional with Polymorphism. | branches in one place (type) instead of many. |
| Lazy Class | Too little responsibility to justify existence. | Inline Class / Collapse Hierarchy. | classes/files ↓. |
| Class Obsession | Deep/empty hierarchy for no payback. | Collapse layers; prefer composition. | hierarchy depth ↓. |
| Middle Man | Every method just delegates. | Remove Middle Man (call real class). | indirection ↓. |
| Speculative Generality | Abstractions built “just in case”. | Delete (YAGNI); keep concrete. | indirection/files ↓. |
| Comments | Redundant/misleading/dead comments. | Rename for intent; delete dead; only keep “why”. | commentLines ↓, truthfulness ↑. |
| Hidden Bugs | Silent truncation, wrong units, off-by-one, unvalidated trust. | Widen types, name units, bounds, explicit checks + tests. | failure becomes loud; invariants explicit. |
