# System Design Student Handout

## The 6-Step Framework for System Design Interviews

### Step 1: Clarify Requirements (10 min)
- **Functional Requirements**: What does the system do?
- **Non-Functional Requirements**: How does it behave? (scale, latency, consistency)
- **Out of Scope**: What are we NOT building?

### Step 2: Estimate Scale (5 min)
- Active users (daily/monthly)
- Requests per second (QPS)
- Storage requirements
- Bandwidth needs

### Step 3: Define Core Entities (5 min)
- Primary data models
- Relationships between entities
- Key attributes

### Step 4: Design APIs (10 min)
- REST endpoints
- Request/Response formats
- Authentication approach

### Step 5: High-Level Design (10 min)
- Draw the architecture diagram
- Show component interactions
- Label all major components

### Step 6: Deep Dive (10 min)
- Bottlenecks and solutions
- Failure scenarios
- Trade-off analysis

---

## Key Vocabulary

| Term | Definition |
|------|------------|
| **Latency** | Time for a request to travel to server and back |
| **Throughput** | Number of requests handled per unit time |
| **Availability** | Percentage of time system is operational |
| **Consistency** | All nodes see the same data at the same time |
| **Load Balancer** | Distributes incoming traffic across servers |
| **CDN** | Content Delivery Network - serves content from edge locations |
| **Cache** | Temporary storage for frequently accessed data |
| **Sharding** | Splitting database across multiple servers |
| **Replication** | Copying data across multiple nodes |
| **Message Queue** | Async communication between services |

---

## Common Trade-offs

### SQL vs NoSQL
- **SQL**: Strong consistency, complex queries, rigid schema
- **NoSQL**: Horizontal scaling, flexible schema, eventual consistency

### Vertical vs Horizontal Scaling
- **Vertical**: Simple, expensive, single point of failure
- **Horizontal**: Complex, cost-effective, fault-tolerant

### Synchronous vs Asynchronous
- **Sync**: Simple, blocking, lower latency per request
- **Async**: Non-blocking, better throughput, eventual consistency

---

## Practice Design Prompts

1. **URL Shortener**: bit.ly, tinyurl
2. **Chat Application**: WhatsApp, Slack
3. **Social Feed**: Twitter, Instagram
4. **Video Streaming**: YouTube, Netflix
5. **Ride Sharing**: Uber, Lyft
6. **E-commerce**: Amazon, Shopify

---

## Interview Timeline Template

| Time | Activity |
|------|----------|
| 0:00 - 0:10 | Requirements & Scale |
| 0:10 - 0:20 | API & High-Level Design |
| 0:20 - 0:35 | Component Deep Dive |
| 0:35 - 0:45 | Bottlenecks & Trade-offs |
