# Lesson 6: API Design — Walkthrough

## Learning Goal
Design RESTful APIs, compare API paradigms, and implement rate limiting.

---

## 1. REST API Principles

| Principle | Description |
|-----------|-------------|
| **Stateless** | Each request contains all needed info |
| **Resource-based** | URLs represent resources (nouns) |
| **HTTP methods** | GET, POST, PUT, DELETE for CRUD |
| **Uniform interface** | Consistent URL structure |

### Example Endpoints
```
GET    /api/products        # List products
POST   /api/products        # Create product
GET    /api/products/:id    # Get single product
PUT    /api/products/:id    # Update product
DELETE /api/products/:id    # Delete product
```

---

## 2. REST vs GraphQL vs gRPC

| Feature | REST | GraphQL | gRPC |
|---------|------|---------|------|
| **Protocol** | HTTP/1.1 | HTTP | HTTP/2 |
| **Data Format** | JSON | JSON | Protocol Buffers |
| **Flexibility** | Fixed endpoints | Client queries | Code-first |
| **Use Case** | Web apps | Flexible queries | Microservices |

---

## 3. API Versioning

**Why:** Changes can break existing clients.

### Strategies
1. **URL versioning:** `/api/v1/products`
2. **Header versioning:** `Accept: application/vnd.api.v1+json`
3. **Query param:** `/api/products?version=1`

---

## 4. Rate Limiting

**Purpose:** Prevent abuse and ensure fair usage.

### Algorithms
| Algorithm | Description |
|-----------|-------------|
| **Fixed Window** | X requests per Y seconds |
| **Sliding Window** | Rolling time window |
| **Token Bucket** | Tokens replenish over time |

### Response Headers
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1625000000
```

---

## 5. Authentication

| Method | Description |
|--------|-------------|
| **API Key** | Simple, passed in header |
| **OAuth 2.0** | Token-based, delegated access |
| **JWT** | Self-contained tokens |

---

## 6. Activity: API Design

Design APIs for a blog system:
1. Define endpoints for posts, comments, users
2. Choose HTTP methods for each
3. Add pagination for list endpoints
4. Include error response format

---

## 7. Assessment

Why is rate limiting important? What happens without it?
