# System Design Lesson Plan

## Lesson 1: Introduction to System Design (60 min)

### Learning Objectives
- Define system design and its importance
- Understand HLD vs LLD
- Identify the 5 pillars of great design

### Key Concepts
1. **Scalability** - Handle growth (10x, 1000x users)
2. **High Availability** - Stay online despite failures
3. **Reliability** - Work correctly, not just online
4. **Efficiency** - Fast response (latency) + high throughput
5. **Manageability** - Easy to debug and update

### Activities
- Discuss: How do apps handle millions of concurrent users?
- Group exercise: Identify pillars in existing systems

### Assessment
- Quiz on 5 pillars definitions
- Short answer: Why is reliability different from availability?

---

## Lesson 2: Building Blocks (60 min)

### Learning Objectives
- Identify core system components
- Understand component interactions
- Draw basic architecture diagrams

### Key Components
| Component | Purpose |
|-----------|---------|
| Load Balancer | Distribute traffic across servers |
| Cache | Store frequent data temporarily |
| CDN | Serve content from edge locations |
| Message Queue | Async communication between services |
| Database | Persistent data storage |

### Activities
- Diagram: Draw a 3-tier architecture
- Hands-on: Label components in a sample system

---

## Lesson 3: Scaling Strategies (60 min)

### Learning Objectives
- Compare vertical vs horizontal scaling
- Choose appropriate scaling approach
- Understand load balancer role

### Scaling Comparison
| Aspect | Vertical | Horizontal |
|--------|----------|------------|
| Method | Upgrade single server | Add more servers |
| Cost | Expensive at scale | Cost-effective |
| Complexity | Simple | Requires load balancing |
| Single Point of Failure | Yes | No |

### Activities
- Case study: Netflix's scaling journey
- Decision exercise: Which approach for given scenarios?

---

## Lesson 4: Caching Deep Dive (60 min)

### Learning Objectives
- Implement caching strategies
- Understand cache hit/miss ratios
- Apply cache invalidation techniques

### Caching Types
1. **Browser Cache** - Client-side storage
2. **CDN Cache** - Edge location storage
3. **Application Cache** - Server-side (Redis, Memcached)
4. **Database Cache** - Query result caching

### Cache Patterns
- **Cache-Aside**: App manages cache explicitly
- **Write-Through**: Write to cache + DB simultaneously
- **Write-Behind**: Write to cache, async to DB
- **Write-Around**: Write directly to DB, skip cache

### Activities
- Calculate cache hit ratios
- Design caching for a URL shortener

---

## Lesson 5: Database Design (60 min)

### Learning Objectives
- Choose SQL vs NoSQL
- Understand normalization vs denormalization
- Apply sharding strategies

### SQL vs NoSQL
| Feature | SQL | NoSQL |
|---------|-----|-------|
| Schema | Rigid | Flexible |
| Scaling | Vertical | Horizontal |
| ACID | Yes | Eventually consistent |
| Best for | Complex queries | Large scale, simple queries |

### Activities
- Schema design exercise
- Sharding strategy discussion

---

## Lesson 6: API Design (60 min)

### Learning Objectives
- Design RESTful APIs
- Compare REST vs GraphQL vs gRPC
- Implement rate limiting

### API Paradigms
| Type | Use Case | Format |
|------|----------|--------|
| REST | Web applications | JSON |
| GraphQL | Flexible queries | JSON |
| gRPC | Microservices | Protocol Buffers |

### Activities
- Design API for a blog system
- Implement rate limiting logic

---

## Lesson 7: Real-World System Design (90 min)

### Learning Objectives
- Apply all concepts to design complete systems
- Identify bottlenecks and trade-offs
- Present and defend design decisions

### Design Prompts
1. URL Shortener (bit.ly)
2. WhatsApp/Telegram
3. YouTube/Netflix
4. Uber/Lyft

### Activities
- Full system design exercise (45 min)
- Peer review and presentation

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
