# System Design Exercises

## Exercise 1: Component Identification
**Time**: 15 minutes

Given this scenario: "Design a URL shortener like bit.ly"

1. List 5 functional requirements
2. List 3 non-functional requirements
3. Identify the core entities
4. Estimate scale (1 billion URLs/month)

---

## Exercise 2: Architecture Diagram
**Time**: 20 minutes

Draw an architecture diagram for a URL shortener with:
- Client application
- Load balancer
- API servers
- Database
- Cache (Redis)

Label all data flows.

---

## Exercise 3: API Design
**Time**: 15 minutes

Design REST APIs for the URL shortener:
- `POST /api/shorten` - Create short URL
- `GET /:shortCode` - Redirect to original URL
- `GET /api/stats/:shortCode` - Get click statistics

Define request/response bodies.

---

## Exercise 4: Trade-off Analysis
**Time**: 15 minutes

For the URL shortener:
1. Why Redis for caching instead of Memcached?
2. Should we use SQL or NoSQL? Justify.
3. What are the trade-offs of using a hash vs counter for short codes?

---

## Exercise 5: Scaling Discussion
**Time**: 20 minutes

Your URL shortener goes from 1M to 1B requests/day:
1. What breaks first?
2. How do you add caching?
3. How do you shard the database?
4. What about geographic distribution?

---

## Exercise 6: Failure Scenarios
**Time**: 15 minutes

What happens when:
1. A database server goes down?
2. The cache is cleared?
3. A load balancer fails?
4. Network latency increases 10x?

---

## Exercise 7: Full System Design
**Time**: 45 minutes

Design a chat application (WhatsApp):
1. Clarify requirements (functional + non-functional)
2. Estimate scale (users, messages/day)
3. Define core entities (User, Message, Chat)
4. Design API endpoints
5. Draw high-level architecture
6. Discuss deep dive topics (message ordering, delivery guarantees)

---

## Exercise 8: Real-World Analysis
**Time**: 30 minutes

Choose one: YouTube, Uber, or Twitter
1. What are the main components?
2. How do they handle scale?
3. What caching strategy do they likely use?
4. What are the main bottlenecks?

---

## Peer Review Checklist

When reviewing a classmate's design:
- [ ] Did they clarify requirements first?
- [ ] Did they estimate scale?
- [ ] Are core entities defined?
- [ ] Are APIs RESTful and well-documented?
- [ ] Is the architecture diagram clear?
- [ ] Did they discuss trade-offs?
- [ ] Did they address failure scenarios?
