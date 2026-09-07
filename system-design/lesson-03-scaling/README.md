# Lesson 3: Scaling Strategies — Walkthrough

## Learning Goal
Compare vertical and horizontal scaling approaches and understand when to use each.

---

## 1. Vertical Scaling (Scale Up)

**Method:** Upgrade the existing server (more CPU, RAM, storage)

| Pros | Cons |
|------|------|
| Simple to implement | Expensive at high capacity |
| No code changes needed | Single point of failure |
| Strong consistency easier | Hardware limits |

**Example:** Upgrading from t3.medium to m5.2xlarge on AWS

---

## 2. Horizontal Scaling (Scale Out)

**Method:** Add more servers to distribute load

| Pros | Cons |
|------|------|
| Cost-effective at scale | Requires load balancing |
| Fault-tolerant | Data consistency challenges |
| Near-infinite scaling | More complex architecture |

**Example:** Running 50 identical web servers behind a load balancer

---

## 3. Comparison Matrix

| Aspect | Vertical | Horizontal |
|--------|----------|------------|
| **Implementation** | Simple | Complex |
| **Cost** | Expensive at scale | Cost-effective |
| **Fault Tolerance** | Single point of failure | High |
| **Data Consistency** | Easier | Challenging |
| **Limit** | Hardware maximum | Near infinite |

---

## 4. When to Use Each

### Use Vertical Scaling When:
- Application is simple
- Team is small
- Starting a new project
- Strong consistency required

### Use Horizontal Scaling When:
- High traffic expected
- Need fault tolerance
- Global distribution required
- Cost efficiency matters

---

## 5. Real-World Examples

| Company | Scaling Approach |
|---------|------------------|
| **Netflix** | Horizontal (thousands of servers) |
| **Startup MVP** | Vertical (simple, fast) |
| **AWS** | Horizontal (massive scale) |
| **Gaming Server** | Vertical first, then horizontal |

---

## 6. Activity: Scaling Decision

For each scenario, choose vertical or horizontal:
1. Prototype for 100 users → **Vertical**
2. Social app with 1M+ users → **Horizontal**
3. Financial system needing consistency → **Vertical**
4. Video streaming platform → **Horizontal**

---

## 7. Assessment

Explain why Netflix couldn't use vertical scaling. What problems would they face?
