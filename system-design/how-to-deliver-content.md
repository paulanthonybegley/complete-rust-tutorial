# How to Deliver System Design Content — Teacher's Guide

A step-by-step guide for delivering system design lessons in front of students.

---

## Before Class (15 min)

1. **Test your setup** — Open all slides, diagrams, and tools you'll use
2. **Review the lesson plan** — Know your learning goal and assessment
3. **Prepare materials** — Whiteboard markers, student handouts, timer
4. **Set the room** — Ensure visibility, seating, and connectivity

---

## Opening (5 min)

### Step 1: Welcome and Set Context
- Greet students warmly
- State the lesson topic clearly
- Connect to previous lesson (if applicable)

> "Today we're learning about scaling — how systems handle growth from 100 to 1 million users."

### Step 2: State the Learning Goal
- One sentence, student-friendly language
- Write it on the board
- Explain why it matters

> "By the end of this lesson, you'll be able to choose between vertical and horizontal scaling for any system."

### Step 3: Preview the Flow
- Briefly outline what's coming
- Set expectations for participation

> "We'll review a concept, do an activity together, then you'll practice in pairs."

---

## Core Delivery (30-40 min)

### Step 4: Teach the Concept (15-20 min)
- Start with **why** before **what**
- Use one concept at a time
- Check for understanding after each point

**Techniques:**
- **Think aloud** — Narrate your reasoning
- **Use analogies** — "A load balancer is like a traffic cop"
- **Visual first** — Show the diagram before explaining
- **Pause and check** — "Does this make sense? Questions?"

**Example:**
> "Scalability means handling growth. Think about Netflix — they started small, now they serve 200 million users. How did they do that? Let's look at their architecture..."

### Step 5: Model the Activity (5-10 min)
- Do the first example together
- Think aloud as you work
- Show expected output

**Example:**
> "Let's practice. Given this scenario: 'Design a URL shortener.' First, I'll clarify requirements. What does the system need to do? Let me write down functional requirements..."

### Step 6: Release to Practice (10-15 min)
- Give clear instructions
- Set a timer
- Circulate and observe

**Instructions template:**
> "Now it's your turn. In pairs, complete Exercise 3. You have 10 minutes. Focus on [specific aspect]. When you're done, be ready to share."

---

## Engagement Techniques

### Step 7: Use Cold Calling (Randomly)
- Ask questions to randomly selected students
- Give think time (5-10 seconds)
- Accept all answers without judgment

> "What's one difference between SQL and NoSQL? [Pause] Sarah?"

### Step 8: Use Think-Pair-Share
1. **Think** — Individual reflection (1 min)
2. **Pair** — Discuss with neighbor (2 min)
3. **Share** — Volunteers share with class

### Step 9: Use Whiteboarding
- Have students draw diagrams
- Walk around and observe
- Celebrate good examples publicly

> "Everyone grab a marker. Draw a load balancer connected to 3 servers. You have 2 minutes."

### Step 10: Use Real-World Connections
- Reference systems students use daily
- Ask "How do you think X handles this?"
- Keep it relevant and current

> "When you open Instagram, how do you think your feed loads so fast? Caching. Let's see how..."

---

## During Activities

### Step 11: Circulate Actively
- Walk the room constantly
- Listen to discussions
- Note common mistakes
- Offer hints, not answers

**What to say:**
- "Interesting approach. What happens if the cache fails?"
- "Have you considered the scale?"
- "Try drawing the data flow."

### Step 12: Manage Time Visibly
- Use a timer projected on screen
- Give time warnings: "5 minutes left... 2 minutes... Time!"
- Respect the schedule

### Step 13: Capture Student Thinking
- Write student ideas on the board
- Credit contributors by name
- Use their examples in explanations

> "Marcus just said something great — he noticed that horizontal scaling needs a load balancer. Let's add that to our diagram."

---

## Closing (10 min)

### Step 14: Review the Learning Goal
- Revisit the goal stated at the start
- Ask students to self-assess
- Celebrate progress

> "At the start, I said you'd learn to choose between scaling approaches. Can anyone give me an example of when to use horizontal scaling?"

### Step 15: Summarize Key Takeaways
- 3-5 bullet points maximum
- Use student language, not jargon
- Make it memorable

> "Today we learned:
> - Vertical = upgrade one server (simple, expensive)
> - Horizontal = add more servers (complex, cost-effective)
> - Choose based on scale, cost, and fault tolerance"

### Step 16: Preview Next Lesson
- Brief teaser of what's coming
- Connect to today's learning
- Build anticipation

> "Next time, we'll dive into caching — how to make systems faster. You'll use that load balancer knowledge again."

### Step 17: Assign Follow-Up (Optional)
- Keep it minimal
- Make it meaningful practice
- Provide clear instructions

> "For next time, read the student handout and try Exercise 4. It's optional but will help you prepare."

---

## Handling Challenges

### If Students Are Quiet
- Use think-pair-share before cold calling
- Ask easier questions first
- Praise any participation
- Use anonymous polling if available

### If Students Are Confused
- Go back to the analogy
- Draw a simpler diagram
- Break the concept into smaller pieces
- Ask: "What part is unclear?"

### If You Run Out of Time
- Prioritize the learning goal
- Skip nice-to-have activities
- Summarize key points quickly
- Assign rest as homework

### If Technology Fails
- Have printed backups
- Use whiteboard instead of slides
- Engage in discussion
- Don't let tech interrupt flow

---

## Sample Lesson Script (10 min segment)

```
[0:00] "Alright, let's talk about caching. Who's ever wondered why some websites load instantly while others take forever?"

[0:15] [Wait for hands] "Cache. It's like keeping a copy of frequently used info on your desk instead of walking to the library every time."

[0:30] [Draw diagram] "Here's the flow: User → App → Cache → Database. If data is in cache — boom, fast. If not, we fetch from database and store it for next time."

[0:50] "Quick check — what's a cache hit? [Pause] Yes, data found in cache. And a cache miss?"

[1:00] "Exactly — data not in cache. We want high hit ratios. Let's calculate one together..."

[1:15] [Release to activity] "In your pairs, calculate the cache ratio for this scenario. You have 3 minutes. Go!"
```

---

## Post-Class Reflection

After each lesson, note:
1. What worked well?
2. What confused students?
3. What should I change next time?
4. Which students need follow-up?

---

## Quick Reference Card

| Phase | Time | Key Actions |
|-------|------|-------------|
| Opening | 5 min | Welcome, state goal, preview flow |
| Core | 30-40 min | Teach, model, practice |
| Closing | 10 min | Review goal, summarize, preview |

**Mantras:**
- "Why before what"
- "Show then tell"
- "Practice more than lecture"
- "Pause and check"
- "Credit student thinking"
