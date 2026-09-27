package com.example.codesmells.domain;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The course syllabus: every video in The Serious CTO's playlist
 * "Code Smells: Identifying and Refactoring Troubled Code", turned into one lab.
 * The lab order is a learning sequence, not the playlist order:
 * start from the master skill (extract method) and work up to team/human factors.
 */
@Component
public class SmellRegistry {

    private final List<Smell> smells = List.of(
            s("long-method", 1,
                    "Long Method", "One method five screens long. The master smell.",
                    "xXUBmlFwz_s",
                    "When a method passes a comfortable size (the video's rule of thumb: "
                            + "you stop being able to hold it in your head at once), every feature becomes a "
                            + "needle-in-a-haystack edit. The code does five jobs and the names lie about "
                            + "which one is executing. Fixing this smell is the master skill: once you can "
                            + "extract cleanly, every other refactoring in this course becomes easier.",
                    "A method that scrolls past a screen, with one comment per paragraph and "
                            + "four temporary variables carrying state through the body.",
                    "Extract Method (and friends: Extract Query, Parameter Object).",
                    "Extraction gives every job a name and every name a reason to exist. The "
                            + "long method shrinks to a reading list of smaller steps, so the next "
                            + "programmer can see what the method does without stepping through it. "
                            + "The metrics make the win visible: longest method and method count "
                            + "are the columns that move.",
                    List.of("Long Method", "Feature Envy", "Lazy Class"), 0,
                    List.of("Extract Method + Extract Query + Parameter Object", "Inline Method", "Move Method"), 0),
            s("god-class", 2,
                    "God Class", "One class that knows everything and does everything.",
                    "jFt5uMkTQ6U",
                    "A God Class (the video calls it a God Object) starts as a convenient "
                            + "utility class and grows until it owns orders, customers, payments, "
                            + "and emails at once. Every change to any of those concerns lands in the same "
                            + "file, so merges become conflicts and tests become a gauntlet. It is the "
                            + "behaviour-level twin of Long Method: too many responsibilities, one place.",
                    "A class that imports half the domain, has twenty fields, and every request "
                            + "handler in the team routes through it.",
                    "Extract Class for each responsibility, keep the original as a thin Facade.",
                    "Splitting the god object gives each concern one owner and one reason to "
                            + "change. The facade keeps the callers working while the extraction "
                            + "happens, which is exactly how you refactor without a big-bang "
                            + "rewrite. Watch what the split does to the metrics: the seven fields of "
                            + "the god class do not shrink, they disperse — each extracted class "
                            + "carries only the state its own concern needs.",
                    List.of("God Class", "Data Class", "Middle Man"), 0,
                    List.of("Extract Class per responsibility, keep a Facade", "Convert to a Singleton", "Add getters and setters"), 0),
            s("data-class", 3,
                    "Data Class", "A data holder with behaviour parked somewhere else.",
                    "4381wH2PEdQ",
                    "A Data Class is a bag of fields with getters and setters and no behaviour. "
                            + "The behaviour lives in a service that interrogates the bag — "
                            + "that service reads half the fields, decides, writes a couple back. The "
                            + "video's framing: the class and its behaviour were separated at birth, and "
                            + "every consumer re-implements the same logic, slightly differently. Put the "
                            + "methods where the data is.",
                    "A class with only private fields + get/set pairs, and the real logic "
                            + "scattered across services that reach into it.",
                    "Encapsulate Record / Move Behaviour into the Class.",
                    "Data plus its behaviour becomes one unit of reasoning: the methods enforce "
                            + "invariants (a shipped order cannot un-ship), and callers stop duplicating "
                            + "rules. The panel shows the trade: method count on the data class rises "
                            + "from three getters to six methods that carry the rules, and the type "
                            + "mentions drop because the service that used to do the thinking is gone.",
                    List.of("Data Class", "Lazy Class", "Temporary Field"), 0,
                    List.of("Move Behaviour into the Class (encapsulate)", "Add more getters", "Rename the fields"), 0),
            s("primitive-obsession", 4,
                    "Primitive Obsession", "Price as double, email as String, everything as a primitive.",
                    "u5EL-QPOxUU",
                    "Primitive Obsession is modelling domain ideas with the language's basic "
                            + "types. Money becomes a double, email becomes a String, SKU becomes a text "
                            + "field, and every operation on them is re-typed (and re-mistyped) wherever "
                            + "it happens. The video's Dystopia vs Utopia contrast: primitives are "
                            + "promiscuous (they accept any value); a value object is opinionated.",
                    "Magic numbers, naked Strings that mean something, and the same validation "
                            + "snippet pasted into five call sites.",
                    "Replace Primitive with Object — introduce small Value Objects (Money, "
                            + "EmailAddress, SKU).",
                    "A value object owns its meaning: Money cannot mix currencies, EmailAddress "
                            + "validated once at construction, SKU can be format-checked. The compiler "
                            + "becomes the unit test. Metrics: type mentions rise (new types) while the "
                            + "number of bare primitives and repeated validation lines fall.",
                    List.of("Primitive Obsession", "Data Class", "Divergent Change"), 0,
                    List.of("Replace Primitive with Object (value objects)", "Add primitive helper functions", "Use more Strings"), 0),
            s("temporary-field", 5,
                    "Temporary Field", "An object whose fields only exist sometimes.",
                    "tKodfHQ0bYI",
                    "Temporary Field is the silent killer: an object carries fields that are only "
                            + "written on some paths and ignored on others. The error-reporting field that "
                            + "is blank on the happy path; the discount that only applies on Tuesdays. The "
                            + "field looks like state but is a parameter in disguise, and because it is "
                            + "sometimes-null it breeds NPEs and confusion about when the object is "
                            + "actually valid.",
                    "A field that is null/zero on most code paths, and an if-null guard at "
                            + "every read site.",
                    "Extract Class for the conditional fields, or hoist them to method "
                            + "parameters where they really belong.",
                    "The conditional data stops pretending to be object state: it becomes "
                            + "either a small type of its own or an explicit argument. The invariant "
                            + "“the object is always fully valid” is restored, and the if-null "
                            + "guards disappear. Field count drops on the refactored class.",
                    List.of("Temporary Field", "Lazy Class", "Middle Man"), 0,
                    List.of("Extract Class / hoist to method parameters", "Initialize the field in every method", "Add a null check helper"), 0),
            s("feature-envy", 6,
                    "Feature Envy", "A method that spends more time with someone else's data.",
                    "dG2y0FHguqw",
                    "Feature Envy is the jealous method: it lives in one class but reads "
                            + "everything from another. The discount calculator that calls "
                            + "customer.getTier(), customer.getEmail(), customer.getLoyaltyPoints() a "
                            + "dozen times is behaving like a customer method in the wrong house. The "
                            + "video's rule: the method and the data it truly needs should be roommates.",
                    "A method whose body is almost all getters on another object preceded by "
                            + "“that.”.",
                    "Move Method (and Move Field) into the class whose data it envies.",
                    "Moving the method where the data lives collapses the getter-chains into a "
                            + "single honest call, and the data's invariants become enforceable locally. "
                            + "The metric to watch is per-method coupling dropping on the "
                            + "envy-free design.",
                    List.of("Feature Envy", "Inappropriate Intimacy", "God Class"), 0,
                    List.of("Move Method to the class whose data it uses", "Add more getters", "Duplicate the logic"), 0),
            s("inappropriate-intimacy", 7,
                    "Inappropriate Intimacy", "Two classes too familiar with each other's internals.",
                    "3FUXLpjuYwU",
                    "Inappropriate Intimacy lives in the fondue ad — "
                            + "classes that reach through each other's doors and borrow internals "
                            + "normally private. The video's framing: tightly coupled components "
                            + "that make the codebase unsafe to change. What starts as a shortcut "
                            + "becomes a tangled dependency graph where touching one class "
                            + "quietly reshapes another.",
                    "Sibling classes freely accessing each other's fields and methods, "
                            + "bidirectional calls, a friend-network of knows-too-much.",
                    "Change Bidirectional Association to Unidirectional, Extract Class for "
                            + "shared parts, Law of Demeter hygiene.",
                    "Cutting the entanglement gives each class a front door: dependencies "
                            + "become visible in the signature instead of hidden in the body. "
                            + "The department's promotion loop halves in size, and the employee "
                            + "grows a contract (promoted(), monthlyPay()) instead of exposing "
                            + "its privates.",
                    List.of("Inappropriate Intimacy", "Feature Envy", "Hidden Bugs"), 0,
                    List.of("Make the association unidirectional, extract the shared part", "Add getters to both", "Merge the classes into one"), 0),
            s("divergent-change", 8,
                    "Divergent Change", "One class, changed for many different reasons.",
                    "iwOMCqJNfFA",
                    "Divergent Change is the structural twin of Shotgun Surgery, standing still "
                            + "so you can examine it: one class that must be edited every time "
                            + "anything unrelated happens. The class in the video handles persistence "
                            + "mapping, report formatting, AND email, so a schema change, a layout "
                            + "change, or a subject-line change all bombs the same file. "
                            + "Single Responsibility is exactly this smell's cure.",
                    "A class with three different preamble comments (“when the DB schema "
                            + "changes…”, “when reports change…”, “when email changes…”).",
                    "Extract Class — one class per reason to change.",
                    "Each reason to change gets its own file and its own tests; a change to "
                            + "the database stops touching the email layout. This is the definition "
                            + "of Single Responsibility, made measurable: the same class no longer "
                            + "appears in unrelated commit messages.",
                    List.of("Divergent Change", "Data Class", "Class Obsession"), 0,
                    List.of("Extract Class per reason to change", "Add a settings flag to switch behaviour", "Merge with the caller"), 0),
            s("switch-statements", 9,
                    "Switch Statements", "The same switch/if-chain, everywhere the type appears.",
                    "EpsbKaFHM8w",
                    "A Switch Statement smell is not the syntax; it is the repetition: one "
                            + "switch on a type code here, its cousin if-else there, another open "
                            + "in two more files. Every new kind of order must touch every one of "
                            + "them, and forgetting one is how “we only changed one place” "
                            + "becomes a production incident. The video's nightmare: the type "
                            + "multiplies, the switches multiply faster.",
                    "More than one switch/if-chain branching on the same field or class, "
                            + "with identical case lists.",
                    "Replace Conditional with Polymorphism (+ Factory for construction).",
                    "Polymorphism moves each case's behaviour onto the type that owns it; "
                            + "adding a new kind stops being an edit-everywhere task and becomes "
                            + "a new class. The dashboard counts switch branches before and after.",
                    List.of("Switch Statements", "Long Method", "God Class"), 0,
                    List.of("Replace Conditional with Polymorphism", "Extract the switch into a helper", "Add a combined enum with behaviour"), 0),
            s("lazy-class", 10,
                    "Lazy Class", "A class that works a two-hour week.",
                    "RLYeybC8ZfU",
                    "A Lazy Class is a freeloader: a class whose only job is to contain one "
                            + "trivial method, or a hierarchy whose subclasses add nothing. It looks "
                            + "like architecture and reads as noise — every file is a place a "
                            + "bug can hide and a review must read. The video's pitch: your code "
                            + "feels lazy because it is full of classes that do too little to "
                            + "justify existing.",
                    "A class with one 2-line method, or a subclass with no overrides and "
                            + "no new fields.",
                    "Inline Class (and Collapse Hierarchy for the empty subclasses).",
                    "Deleting the freeloader moves its tiny job to the place that actually "
                            + "uses it — fewer files, fewer indirections, and the reader stops "
                            + "paying a toll to cross a parking lot. Method and file counts go "
                            + "down without losing any behaviour.",
                    List.of("Lazy Class", "Data Class", "Middle Man"), 0,
                    List.of("Inline Class / Collapse Hierarchy", "Add more features to justify it", "Make it abstract"), 0),
            s("class-obsession", 11,
                    "Class Obsession", "Classes because we love classes, not because we need them.",
                    "LZz_PxGWAKk",
                    "Class Obsession is the other side of Lazy Class: zeal for abstraction "
                            + "where a looser structure would do. The video walks the tell-tale signs "
                            + "— rigid hierarchies, needless layers, tight coupling in the name of "
                            + "flexibility. A four-level inheritance ladder for three behaviours "
                            + "is not engineering, it is a museum. The cure is not delete-all-"
                            + "classes, it is proportion: abstraction that pays for itself.",
                    "A deep inheritance hierarchy where each level adds nothing, or a pile "
                            + "of one-method classes wired together by names.",
                    "Collapse Hierarchy / reduce layers; favour composition with meaning.",
                    "Flattening the museum keeps the real contracts but removes the "
                            + "ritual levels, so a method call stops travelling through six "
                            + "frames to reach one line. The panel shows eight types collapsing to "
                            + "four, and the surviving methods get the flattened bodies back.",
                    List.of("Class Obsession", "Primitive Obsession", "Lazy Class"), 0,
                    List.of("Collapse the hierarchy, prefer meaningful composition", "Add two more layers", "Convert classes to primitives"), 0),
            s("middle-man", 12,
                    "Middle Man", "A class whose entire job is forwarding someone else's calls.",
                    "zpthKKRO19s",
                    "Middle Man syndrome is the fatal mistake the video names: an object exists "
                            + "only to delegate, and the delegation outnumbers the delegation's point. "
                            + "The OrderManager that does nothing but answer "
                            + "getStatus(), getCustomer(), getTotal() by forwarding to Order is "
                            + "a toll booth for no road. Every new method Order grows means "
                            + "another forwarding method — code that moves, never decides.",
                    "A class where every method is a one-line “delegate to X” and a "
                            + "google search of its callers shows they all call it and never "
                            + "call it for the extra reason it exists.",
                    "Remove Middle Man: callers talk to the real class directly.",
                    "Deleting the forwarding layer removes the indirection tax: one fewer "
                            + "class, one fewer place to keep method lists in sync, and the "
                            + "real class's intent is visible in the caller again.",
                    List.of("Middle Man", "Lazy Class", "Feature Envy"), 0,
                    List.of("Remove Middle Man (call the real class directly)", "Add more forwarding methods", "Make the middle man abstract"), 0),
            s("speculative-generality", 13,
                    "Speculative Generality", "Abstractions built for futures that never arrive.",
                    "YofkusanIRc",
                    "The video that opened the series' biggest wound: speculative generality "
                            + "is code written “in case we need it” — the abstract base with one "
                            + "implementation, the pluggable strategy with a single strategy, the "
                            + "factory whose factory has one consumer. It reads as flexibility and "
                            + "costs as YAGNI: every abstraction is a place readers pay attention "
                            + "and tests must be maintained, for a future the product never "
                            + "chose to buy.",
                    "Abstract class with exactly one subclass; unused interfaces; "
                            + "parameter that only ever receives one value.",
                    "You Ain't Gonna Need It: delete the speculative layer, collapse to the "
                            + "concrete.",
                    "Removing speculation pays immediately: fewer files, fewer indirections, "
                            + "and the surviving code is the honest version that does what today "
                            + "asks. It is the mental inverse of premature optimisation.",
                    List.of("Speculative Generality", "Lazy Class", "Class Obsession"), 0,
                    List.of("Delete the speculation (YAGNI), keep the concrete", "Add the second implementation now", "Extract two more interfaces"), 0),
            s("comments", 14,
                    "Comments", "Comments that apologise instead of explaining.",
                    "E83a4dBANoI",
                    "The comment smell is not “comments are bad”; it is three specific "
                            + "mistakes the video names: redundant comments that restate the code "
                            + "word for word, misleading comments that drift from the code they "
                            + "once described, and commented-out code left as a graveyard. All "
                            + "three replace understanding with noise, and the misleading one "
                            + "actively lies to the reader.",
                    "A line like // increments the counter above a line that decrements "
                            + "it; a 40-line commented-out block; comments repeating the method "
                            + "name.",
                    "Remove Redundant Comment; convert the real ones to code (intention-"
                            + "revealing names); delete dead code for real.",
                    "Self-documenting code says the same thing once. The why-comments "
                            + "that survive carry information the code cannot (the reason for a "
                            + "hack), and comment lines in the metric panel drop without "
                            + "any information being lost.",
                    List.of("Comments (redundant/misleading/dead code)", "Lazy Class", "Data Class"), 0,
                    List.of("Remove redundant/misleading comments, rename for intent", "Write more comments everywhere", "Move comments to a separate file"), 0),
            s("hidden-bugs", 15,
                    "Hidden Bugs", "The latent defects no one noticed until they cost millions.",
                    "3qimblc5nvw",
                    "The closing lab is not a classic smell but the reason smells matter: "
                            + "careful-looking code hiding a latent defect. The video tours the "
                            + "disasters — the silent integer overflow, the unit mix-up, the "
                            + "off-by-one, the unvalidated trust — that sat in plain sight for "
                            + "years. Each one is a refactoring in disguise: code that never "
                            + "repayed its meaning, its types, or its assumptions.",
                    "A counter that will overflow, a speed compared against a threshold "
                            + "in the wrong unit, an off-by-one filter that “looks right”, "
                            + "implicit trust in a caller.",
                    "Extract the computation behind a type, clamp and bound it, and name "
                            + "the units; add the test the shortcut skipped.",
                    "The refactor converts silent failure into loud failure: wide types, "
                            + "explicit units, bounds checks, and tests that would have caught "
                            + "the disaster in a week instead of a decade. This lab is the "
                            + "whole course's motive, made executable.",
                    List.of("Hidden Bugs (silent truncation, units, off-by-one)", "Long Method", "Middle Man"), 0,
                    List.of("Widen the type, name the units, bound inputs, test the edge", "Increase the buffer size", "Round the numbers first"), 0));

    public List<Smell> all() {
        return smells;
    }

    public Smell bySlug(String slug) {
        return smells.stream()
                .filter(s -> s.slug().equals(slug))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown smell: " + slug));
    }

    private static Smell s(String slug, int episode, String title, String subtitle, String videoId,
                           String summary, String symptom, String fix, String why,
                           List<String> identifyOptions, int identifyCorrect,
                           List<String> refactorOptions, int refactorCorrect) {
        return new Smell(slug, episode, title, subtitle, videoId, summary, symptom, fix, why,
                "/snippets/%s/Before.java".formatted(slug),
                "/snippets/%s/After.java".formatted(slug),
                identifyOptions, identifyCorrect, refactorOptions, refactorCorrect);
    }
}