# Lesson 7: Real-World System Design — Walkthrough

## Learning Goal
Apply all concepts to design complete systems and defend design decisions.

---

## 1. Design Framework Recap

| Step | Activity |
|------|----------|
| 1 | Clarify Requirements |
| 2 | Estimate Scale |
| 3 | Define Core Entities |
| 4 | Design APIs |
| 5 | High-Level Design |
| 6 | Deep Dive |

---

## 2. Design Prompt: URL Shortener

### Functional Requirements
- Generate short URL from long URL
- Redirect short URL to original
- Optional: Custom aliases
- Optional: Expiration dates

### Non-Functional Requirements
- Low latency (<100ms redirect)
- High availability
- 1 billion URLs/month

### Core Entities
- **URL**: id, short_code, original_url, created_at, expires_at
- **User** (optional): id, email, urls

### API Design
```
POST   /api/shorten      # Create short URL
GET    /:shortCode        # Redirect
GET    /api/stats/:code   # Click statistics
```

### High-Level Architecture
```
Client → Load Balancer → API Servers → Cache (Redis)
                                    → Database (sharded)
```

---

## 3. Design Prompt: Chat Application

### Functional Requirements
- 1:1 messaging
- Group chats
- Online/offline status
- Message history

### Non-Functional Requirements
- Real-time delivery (<100ms)
- Message ordering guaranteed
- Support 500M messages/day

### Core Entities
- **User**: id, name, status
- **Chat**: id, participants, type
- **Message**: id, chat_id, sender_id, content, timestamp

### Key Design Decisions
- **WebSocket** for real-time communication
- **Message Queue** for async delivery
- **Database**: Cassandra for message storage (write-heavy)

---

## 4. Trade-off Analysis Template

| Decision | Option A | Option B | Choice | Reason |
|----------|----------|----------|--------|--------|
| Database | SQL | NoSQL | NoSQL | Scale, write-heavy |
| Cache | Redis | Memcached | Redis | Data structures needed |
| Protocol | REST | WebSocket | WebSocket | Real-time required |

---

## 5. Activity: Full Design Exercise

Choose one prompt and complete the full design:
1. YouTube video streaming
2. Uber ride matching
3. Twitter-like feed

**Time:** 45 minutes
**Deliverables:** Architecture diagram + trade-off analysis

---

## 6. Assessment

Present your system design to a peer. Defend your choices when challenged.
