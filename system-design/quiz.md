# System Design Quiz

## Part 1: Multiple Choice (20 points)

1. What is the difference between availability and reliability?
   a) They are the same thing
   b) Availability means online, reliability means working correctly
   c) Reliability means online, availability means working correctly
   d) Neither matters

2. Which scaling approach adds more servers?
   a) Vertical scaling
   b) Horizontal scaling
   c) Both
   d) Neither

3. What is a cache hit?
   a) Data not found in cache
   b) Data found in cache
   c) Cache server crashed
   d) Cache is full

4. Which database type offers strong consistency?
   a) NoSQL
   b) SQL
   c) Redis
   d) MongoDB

5. What does a load balancer do?
   a) Stores data
   b) Caches responses
   c) Distributes traffic
   d) Runs queries

---

## Part 2: Short Answer (30 points)

6. Explain the 5 pillars of system design. (10 points)

7. Compare vertical and horizontal scaling with pros and cons. (10 points)

8. What is cache invalidation and why is it important? (10 points)

---

## Part 3: Design Scenario (50 points)

9. Design a URL shortener:
   - List 3 functional requirements
   - List 2 non-functional requirements
   - Estimate scale for 100M URLs/month
   - Design the core API endpoints
   - Draw a high-level architecture diagram
   - Discuss one trade-off you would make

---

## Answer Key

### Part 1
1. b) Availability means online, reliability means working correctly
2. b) Horizontal scaling
3. b) Data found in cache
4. b) SQL
5. c) Distributes traffic

### Part 2 (Sample Answers)
6. The 5 pillars are: Scalability (handle growth), High Availability (stay online), Reliability (work correctly), Efficiency (fast + high throughput), Manageability (easy to maintain)

7. Vertical: upgrade single server (simple, expensive, single point of failure). Horizontal: add more servers (complex, cost-effective, fault-tolerant)

8. Cache invalidation removes stale data from cache when source data changes. Important because serving outdated data can cause errors or inconsistencies.
