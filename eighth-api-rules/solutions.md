# Solutions: The 8 API Laws

> Sample answers for all exercises in [`exercises.md`](./exercises.md).
> The app answers were verified against the running Spring Boot 4 application.

---

## Lesson 1 — Design around resources, not actions

**1.1**

| Anti-pattern | Resource path | HTTP method |
|---|---|---|
| `GET /getUserOrders` | `GET /users/{id}/orders` | `GET` |
| `POST /createProduct` | `POST /products` | `POST` |
| `DELETE /removeProduct` | `DELETE /products/{id}` | `DELETE` |
| `GET /searchProducts` | `GET /products?search=...` | `GET` (query refines the collection — see Lesson 6) |
| `PUT /updatePrice` | `PATCH /products/{id}` with `{"price": ...}` | `PATCH` |

**1.2** Example for two handlers in `ProductController.java`:

- `GET /products` (list): the URL names the `products` collection; GET says "give
  me the collection". Query parameters (category, price, sort, page) *refine* the
  result without inventing a new action URL.
- `POST /products` (create): the URL still names the collection `products`; POST
  says "add a new member to it". The URL never says `create`.

The key insight: the same URL `/products/1` works for retrieve (`GET`), replace
(`PUT`), partial update (`PATCH`) and delete (`DELETE`) — no new URLs needed for
new actions.

---

## Lesson 2 — Make URLs predictable

**2.1** Full endpoint table from the app:

| Method | URL | What it does | Lesson exemplified |
|---|---|---|---|
| GET | `/products` | Lists products, filterable/sortable/paginated | 2, 6, 8 |
| GET | `/products/{id}` | Returns one product (v1.0 or v1.1 shape) | 1, 2, 7 |
| POST | `/products` | Creates a product (201 + Location) | 3, 4 |
| PUT | `/products/{id}` | Replaces the whole product | 3 |
| PATCH | `/products/{id}` | Updates only sent fields | 3 |
| DELETE | `/products/{id}` | Removes a product (204) | 3, 4 |
| GET | `/orders` | Lists orders | 2, 8 |
| POST | `/orders` | Creates an order | 1, 3 |
| GET | `/orders/{id}` | Returns one order | 1, 2 |
| GET | `/orders/{id}/items` | Returns an order's items (sub-resource) | 2 |
| PATCH | `/orders/{id}/status` | Transitions order status | 4 |

**2.2** Suggested answers:

- Collection: `GET /products/{id}/reviews`; single review: `GET /products/{id}/reviews/{reviewId}`.
- Create one: `POST /products/{id}/reviews`; delete one: `DELETE /products/{id}/reviews/{reviewId}`.
- Rating is a *refinement*, so it belongs in the query string
  (`?minRating=4`), not in the path. The path identifies a specific resource;
  the query filters the collection. (`rating` in the path would create a "path
  creep" smell, Lesson 6.)

---

## Lesson 3 — Use HTTP methods for their actual purpose

**3.1**

| Operation | Method | Idempotent? | Why? |
|---|---|---|---|
| Fetch a user's profile | GET | Yes | Reading does not change state |
| Replace an entire product | PUT | Yes | Sending the same full body twice leaves the same state |
| Create a new order | POST | No | Two identical requests create two orders |
| Update only the status of an order | PATCH | Yes (with a fixed target status) | Applying the same transition twice leaves the same end state |
| Remove a product | DELETE | Yes | Deleting a missing resource is a no-op |

**3.2** The client `POST`s twice → the app creates two products (each gets a new
id from `AtomicLong` in `ProductService.create`). POST is NOT idempotent. By
contrast, `PUT /products/1` sent twice only replaces the same product twice —
the second replace overwrites identical data, so the end state is the same; PUT
IS idempotent. Behavior is defined in `web/ProductController.java` (`@PostMapping`
and `@PutMapping`) backed by `ProductService.create(...)` / `replace(...)`.

---

## Lesson 4 — Make status codes useful

**4.1**

| Scenario | Code |
|---|---|
| Product found and returned | 200 |
| Product created | 201 |
| JSON syntax error in body | 400 |
| Product never created | 404 |
| Illegal status transition | 409 |
| Body fields failed constraints | 422 |

Rationale: a `200` with a "failure" body forces the client to inspect the body
to learn whether the call worked; the status code already answers that question
(Earth-level: code = category, body = detail).

**4.2** In `GlobalExceptionHandler.java`:

- `handleNotFound` → any request referencing a resource that does not exist
  (e.g. `GET /products/999`) → 404.
- `handleConflict` → request conflicts with current state (insufficient stock,
  illegal transition) → 409.
- `handleBadRequest` → malformed semantic input such as an unknown enum value →
  400.
- `handleValidation` → `@Valid` constraints failed (blank name, negative price)
  → 422 with per-field errors.
- `handleUnreadableBody` → body is not valid JSON → 400.

---

## Lesson 5 — Keep error responses consistent

**5.1** A validation failure body:
```json
{
  "status": 422,
  "code": "VALIDATION_FAILED",
  "message": "One or more fields failed validation",
  "timestamp": "2026-09-15T11:43:17.131290Z",
  "fieldErrors": [
    { "field": "name", "message": "name is required" },
    { "field": "price", "message": "price must be positive" }
  ]
}
```

**5.2** Rate-limited (429) response — note it keeps the same six fields:
```json
{
  "status": 429,
  "code": "RATE_LIMITED",
  "message": "Too many requests. Retry after 60 seconds.",
  "timestamp": "2026-09-15T11:43:17.131290Z",
  "fieldErrors": []
}
```

**5.3** `code` is a stable, machine-readable identifier the client can branch
on (e.g. retry vs report a bug); `message` is text for a human. Bots react to
the code; developers and operators read the message.

---

## Lesson 6 — Don't put everything into the URL path

**6.1**

| Feature | Path or query? | Why? |
|---|---|---|
| Fetch a product by ID | Path | ID *identifies* a specific resource — it is the resource's identity |
| Filter by category | Query | Refines the set of results; not a resource ID |
| Minimum price | Query | Same reasoning as category |
| Sort order | Query | Presentation preference, not identity |
| Page number | Query | Pagination is refinement |
| Keyword search | Query | Refinement/optional criteria |

**6.2**
```bash
curl -H "API-Version: 1.1" "http://localhost:8080/products?category=home&sort=price:desc&page=1&size=1"
```
Seed data `home` category (Desk Lamp 24.99, Tea Kettle 34.50) sorted desc gives
`[Tea Kettle, Desk Lamp]`; page 1 size 1 returns `Tea Kettle`.

---

## Lesson 7 — Treat API changes carefully

**7.1**

`API-Version: 1.0` →
```json
{"id":1,"name":"Clean Code","category":"books","price":39.99,"createdAt":"2026-01-15T09:00:00Z"}
```
`API-Version: 1.1` →
```json
{"id":1,"name":"Clean Code","category":"books","price":39.99,"currency":"USD","stock":100,"createdAt":"2026-01-15T09:00:00Z"}
```
Differing fields: v1.1 adds `currency` and `stock`. Neither existing field
changed; the v1.0 contract is untouched.

**7.2** Adding `tags` is an **additive** change: existing fields keep their
meaning, new clients opt in via a new version, old clients ignore the extra
field — nothing breaks. Compare to renaming `price` → `amount`, which would be
breaking.

**7.3** In `config/WebConfig.java` the app resolves the version from the
`API-Version` header. When versioning is enabled, the version is required by
default, so a request without the header answers **400**. Verified:
```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:18080/products/1   # -> 400
```

---

## Lesson 8 — Keep request and response formats consistent

**8.1** Conventions followed by the app:

1. **camelCase JSON keys** — `createdAt`, `customerName`, not `created_at`.
2. **ISO-8601 timestamps** — `Instant` serializes as `2026-01-15T09:00:00Z`.
3. **One pagination envelope** (`PageResponse`) — `content`, `page`, `size`,
   `totalElements`, `totalPages` on *every* collection endpoint.
4. **One error shape** (`ApiError`) on every failure — `status`, `code`,
   `message`, `timestamp`, `fieldErrors`.
5. **Predictable plural resource names** — `/products`, `/orders`, plus nested
   sub-resources (`/orders/{id}/items`).

Gain: a developer who learns one endpoint can guess the rest without reading
docs — the video's definition of a good API.

**8.2** Inconsistencies and the consistent rewrite:

- `created_at` → `createdAt` (camelCase).
- Date must be full ISO-8601 with zone, not a bare `2026-09-01` (use `"2026-09-01T09:00:00Z"`).
- Collections should be wrapped in the standard `PageResponse` envelope for consistency:
```json
{
  "content": [
    {
      "createdAt": "2026-09-01T09:00:00Z",
      "rating": 4,
      "body": "Great product"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

---

## Integration Project — Worked Minimal Example (booking API)

A compact worked design that satisfies all eight laws (review a peer's project):

- `POST /venues` create venue (201 + Location); `GET /venues/{id}`; `GET /venues`
  with `?city=&sort=&page=&size=` (Laws 1, 2, 6, 8).
- `POST /venues/{id}/slots` create slot; `POST /slots/{id}/reservations` create
  reservation; `DELETE /slots/{id}/reservations/{id}` (nested sub-resources →
  Law 2; POST/DELETE semantics → Law 3).
- `PATCH /reservations/{id}` with `{"state":"cancelled"}` → 200; cancelling an
  already-cancelled reservation and over-committing a capacity-limited slot →
  409 (Laws 3, 4).
- Every failure returns the `ApiError` shape with `code`/`message` (Law 5).
- Version header `API-Version` configured in `WebConfig`; v1.0 returns
  `{...}` and v1.1 adds `capacity` (additive, Law 7).
- All keys camelCase, all dates ISO-8601, one `PageResponse` everywhere (Law 8).