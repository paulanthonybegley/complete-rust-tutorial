# System Design Lesson Plan

## Design Framework

This lesson plan follows the structured framework for AI-assisted lesson design:

| Component | Purpose |
|-----------|---------|
| **Learning Goal** | Clarifies what success looks like |
| **Lesson Sequence** | Sets flow and pacing |
| **Assessment Evidence** | Defines how learning will be shown |
| **Learner Profile** | Who the learners are |
| **Prior Knowledge** | What learners already know |
| **Learning Activities** | Tasks learners will do |
| **Output Requirements** | Formats and constraints |
| **Accessibility & Supports** | Inclusive options for every learner |
| **Teacher Decisions** | Preferences and priorities |

---

## Learner Profile

- **Audience:** Software engineers preparing for system design interviews
- **Level:** Beginner to intermediate
- **Context:** Classroom or self-paced learning
- **Prior Knowledge:** Basic programming concepts, client-server model

---

## Course Overview

| Module | Topic | Duration |
|--------|-------|----------|
| 0 | Foundations & Pillars | 60 min |
| 1 | Building Blocks | 60 min |
| 2 | Scaling Strategies | 60 min |
| 3 | Caching Deep Dive | 60 min |
| 4 | Database Design | 60 min |
| 5 | API Design | 60 min |
| 6 | Real-World Systems | 90 min |

---

## Lesson 1: Introduction to System Design (60 min)

### Learning Goal
Define system design and identify the 5 pillars that guide architectural decisions.

### Prior Knowledge
- Basic programming concepts
- What a server is
- What a database is

### Lesson Sequence
1. Warm-up discussion (5 min)
2. Core concepts lecture (20 min)
3. Pillar identification activity (15 min)
4. Assessment (10 min)

### Learning Activities
- Discuss: How do apps handle millions of concurrent users?
- Group exercise: Identify pillars in existing systems
- Mini-quiz on pillar definitions

### Assessment Evidence
- Quiz on 5 pillars definitions
- Short answer: Why is reliability different from availability?

### Output Requirements
- One-paragraph explanation of Rust's value proposition
- Architecture diagram with labeled pillars

### Accessibility & Supports
- Visual diagrams for visual learners
- Glossary of terms provided
- Pair discussion for verbal processors

### Teacher Decisions
- Emphasize real-world examples (Netflix, Uber)
- Connect to students' existing projects

---

## Lesson 2: Building Blocks (60 min)

### Learning Goal
Identify and understand the core components that make up modern distributed systems.

### Prior Knowledge
- Completed Lesson 1 (Foundations)
- Basic web architecture understanding

### Lesson Sequence
1. Review pillars (5 min)
2. Component lecture (20 min)
3. Diagramming activity (20 min)
4. Assessment (15 min)

### Learning Activities
- Diagram: Draw a 3-tier architecture
- Hands-on: Label components in a sample system
- Component matching game

### Assessment Evidence
- Labeled architecture diagram
- Component purpose explanations

### Output Requirements
- Complete architecture diagram
- Written explanation of each component's role

### Accessibility & Supports
- Color-coded component diagrams
- Interactive drag-and-drop activity
- Reference card for components

### Teacher Decisions
- Use consistent colors for component types
- Connect to real systems students use daily

---

## Lesson 3: Scaling Strategies (60 min)

### Learning Goal
Compare vertical and horizontal scaling approaches and choose appropriate strategies.

### Prior Knowledge
- Completed Lessons 1-2
- Understanding of servers and load balancers

### Lesson Sequence
1. Review building blocks (5 min)
2. Scaling concepts lecture (20 min)
3. Case study analysis (20 min)
4. Assessment (15 min)

### Learning Activities
- Case study: Netflix's scaling journey
- Decision exercise: Which approach for given scenarios?
- Trade-off analysis discussion

### Assessment Evidence
- Scaling decision matrix
- Written justification for choices

### Output Requirements
- Comparison table with pros/cons
- Scaling recommendation for given scenario

### Accessibility & Supports
- Side-by-side visual comparison
- Real-world case studies
- Decision flowchart

### Teacher Decisions
- Emphasize cost implications
- Connect to startup vs enterprise contexts

---

## Lesson 4: Caching Deep Dive (60 min)

### Learning Goal
Implement caching strategies and understand cache invalidation techniques.

### Prior Knowledge
- Completed Lessons 1-3
- Understanding of databases and performance

### Lesson Sequence
1. Review scaling (5 min)
2. Caching concepts lecture (20 min)
3. Pattern analysis activity (20 min)
4. Assessment (15 min)

### Learning Activities
- Calculate cache hit ratios
- Design caching for a URL shortener
- Cache pattern matching exercise

### Assessment Evidence
- Cache ratio calculations
- Caching strategy recommendation
- Invalidation approach explanation

### Output Requirements
- Cache implementation diagram
- Written trade-off analysis

### Accessibility & Supports
- Step-by-step cache flow diagrams
- Calculator for ratios
- Pattern reference sheet

### Teacher Decisions
- Use Redis examples (industry standard)
- Connect to real performance improvements

---

## Lesson 5: Database Design (60 min)

### Learning Goal
Choose between SQL and NoSQL and understand sharding strategies.

### Prior Knowledge
- Completed Lessons 1-4
- Basic database concepts

### Lesson Sequence
1. Review caching (5 min)
2. Database types lecture (20 min)
3. Design activity (20 min)
4. Assessment (15 min)

### Learning Activities
- Schema design exercise
- Sharding strategy discussion
- SQL vs NoSQL decision matrix

### Assessment Evidence
- Database choice justification
- Schema design sample
- Sharding plan explanation

### Output Requirements
- Database selection rationale
- Sample schema with relationships
- Sharding strategy diagram

### Accessibility & Supports
- Comparison tables
- Visual schema diagrams
- Decision flowchart

### Teacher Decisions
- Emphasize CAP theorem implications
- Connect to real-world data patterns

---

## Lesson 6: API Design (60 min)

### Learning Goal
Design RESTful APIs and compare REST vs GraphQL vs gRPC.

### Prior Knowledge
- Completed Lessons 1-5
- HTTP basics

### Lesson Sequence
1. Review databases (5 min)
2. API paradigms lecture (20 min)
3. Design activity (20 min)
4. Assessment (15 min)

### Learning Activities
- Design API for a blog system
- Implement rate limiting logic
- API versioning discussion

### Assessment Evidence
- API endpoint documentation
- Rate limiting implementation
- Versioning strategy explanation

### Output Requirements
- Complete API specification
- Rate limiting approach
- Versioning plan

### Accessibility & Supports
- API documentation templates
- Interactive endpoint builder
- Common patterns reference

### Teacher Decisions
- Use industry-standard practices
- Connect to tools students may use

---

## Lesson 7: Real-World System Design (90 min)

### Learning Goal
Apply all concepts to design complete systems and defend design decisions.

### Prior Knowledge
- Completed Lessons 1-6
- All foundational concepts

### Lesson Sequence
1. Framework review (10 min)
2. Design exercise (50 min)
3. Peer review (20 min)
4. Reflection (10 min)

### Learning Activities
- Full system design exercise (45 min)
- Peer review and presentation
- Trade-off defense discussion

### Assessment Evidence
- Complete system design
- Architecture diagram
- Trade-off analysis
- Peer feedback

### Output Requirements
- Full design document
- Architecture diagram
- Written trade-off analysis
- Presentation to peers

### Accessibility & Supports
- Design template
- Peer collaboration structure
- Reflection prompts

### Teacher Decisions
- Emphasize iterative design
- Connect to interview preparation

---

## Assessment Rubric

| Criterion | Points |
|-----------|--------|
| Requirements clarification | 15 |
| Scale estimation | 10 |
| Core entity definition | 10 |
| API design | 15 |
| High-level architecture | 20 |
| Deep dive / trade-offs | 20 |
| Communication & clarity | 10 |
