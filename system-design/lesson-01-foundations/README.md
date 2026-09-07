# Lesson 1: System Design Foundations — Walkthrough

## Learning Goal
Understand what system design is, why it matters, and the core pillars that guide architectural decisions.

---

## 1. What is System Design?

System design is the process of defining the architecture, components, modules, interfaces, and data flow of a system to satisfy specified requirements.

| Aspect | Description |
|--------|-------------|
| **High-Level Design (HLD)** | Overall system architecture, component interactions |
| **Low-Level Design (LLD)** | Detailed component implementation, class diagrams |

**Key Insight:** "What separates your weekend project from Netflix or Uber? It's not just more code—it's system design."

---

## 2. The 5 Pillars of Great Design

| Pillar | Definition | Example |
|--------|------------|---------|
| **Scalability** | Handle growth (10x, 1000x users) | Netflix handles 200M+ users |
| **High Availability** | Stay online despite failures | AWS guarantees 99.99% uptime |
| **Reliability** | Work correctly, not just online | ATM dispenses correct amount |
| **Efficiency** | Fast response + high throughput | Google returns results in milliseconds |
| **Manageability** | Easy to debug and update | Easy rollbacks, clear logs |

---

## 3. System Design Interview Framework (6 Steps)

| Step | Activity | Time |
|------|----------|------|
| 1 | Clarify Requirements | 10 min |
| 2 | Estimate Scale | 5 min |
| 3 | Define Core Entities | 5 min |
| 4 | Design APIs | 10 min |
| 5 | High-Level Design | 10 min |
| 6 | Deep Dive | 10 min |

---

## 4. Activity: Pillar Identification

Given a scenario, identify which pillar is most critical:

1. "Users in Europe experience 5-second load times" → **Efficiency (latency)**
2. "Server crashes but app stays online" → **High Availability**
3. "App goes viral, needs to handle 100x traffic" → **Scalability**
4. "Database returns wrong data occasionally" → **Reliability**
5. "Team can't debug production issues" → **Manageability**

---

## 5. Assessment

Explain to a colleague why "reliability" and "availability" are different concepts. Use a real-world example.

---

## How to run this lesson's examples

This lesson is conceptual. Refer to the student handout for vocabulary and frameworks.
