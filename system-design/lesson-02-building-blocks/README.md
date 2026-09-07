# Lesson 2: Building Blocks — Walkthrough

## Learning Goal
Identify and understand the core components that make up modern distributed systems.

---

## 1. Core Components

| Component | Purpose | Analogy |
|-----------|---------|---------|
| **Load Balancer** | Distribute traffic across servers | Traffic cop at highway entrance |
| **Cache** | Store frequent data temporarily | Keeping frequently used info on your desk |
| **CDN** | Serve content from edge locations | Library branches in your neighborhood |
| **Message Queue** | Async communication between services | Post office mailboxes |
| **Database** | Persistent data storage | Filing cabinet |

---

## 2. Load Balancers

### Types
| Level | Operates On | Example |
|-------|-------------|---------|
| **L4** | TCP/UDP headers | AWS NLB |
| **L7** | HTTP content | AWS ALB, NGINX |

### Algorithms
- **Round Robin**: Distribute sequentially
- **Least Connections**: Send to server with fewest active connections
- **IP Hash**: Route based on client IP

---

## 3. Caching

### Cache Hit vs Miss
- **Cache Hit**: Data found in cache → fast response
- **Cache Miss**: Data not in cache → must fetch from source

### Cache Ratio
```
Cache Ratio = (Cache Hits / Total Requests) × 100%
```

### Common Cache Locations
1. Browser cache (client-side)
2. CDN cache (edge locations)
3. Application cache (Redis, Memcached)
4. Database cache (query results)

---

## 4. CDNs (Content Delivery Networks)

**Purpose:** Serve static content from locations physically closer to users.

**How it works:**
1. User requests content
2. CDN routes to nearest edge location
3. Edge location serves cached content
4. If cache miss, fetch from origin server

**Use cases:** Images, videos, CSS, JavaScript files

---

## 5. Message Queues

**Purpose:** Enable asynchronous communication between services.

| Pattern | Description |
|---------|-------------|
| **Point-to-Point** | One sender, one receiver |
| **Pub/Sub** | One publisher, multiple subscribers |

**Examples:** RabbitMQ, Apache Kafka, AWS SQS

---

## 6. Activity: Component Mapping

Draw a diagram for an e-commerce site and label:
- Load balancer
- Web servers
- Application servers
- Database
- Cache layer
- CDN for static assets

---

## 7. Assessment

Explain why a system might need both a cache AND a CDN. What problem does each solve?
