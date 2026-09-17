# Exercises: The 8 API Laws

> All exercises reference the Spring Boot 4 application in [`app/`](./app).
> Solutions and sample answers live in [`solutions.md`](./solutions.md).

---

## Lesson 1 — Design around resources, not actions

**1.1** Rewrite each anti-pattern URL as a RESTful resource path plus HTTP method:

| Anti-pattern | Resource path | HTTP method |
|---|---|---|
| `GET /getUserOrders` | | |
| `POST /createProduct` | | |
| `DELETE /removeProduct` | | |
| `GET /searchProducts` | | |
| `PUT /updatePrice` | | |

**1.2** Open `app/src/main/java/com/example/apirules/web/ProductController.java`.
For every handler method in the controller, write one sentence explaining how
the URL identifies the resource while the method identifies the action.

---

## Lesson 2 — Make URLs predictable

**2.1** Open both controllers (`ProductController`, `OrderController`) and list
every public endpoint in a table with four columns: method, URL pattern, what
it does in plain English, and which lesson it exemplifies best.

**2.2** You are asked to add a "reviews" sub-resource on products (users write
reviews about products). Without writing any code, answer:
- What is the URL for the collection? For a single review?
- What method would create one? Delete one?
- If a review has a rating, would rating go in the path or the query string?
  Why?

---

## Lesson 3 — Use HTTP methods for their actual purpose

**3.1** For each row, choose the correct HTTP method (GET, POST, PUT, PATCH,
DELETE) and state whether it is idempotent:

| Operation | Method | Idempotent? | Why? |
|---|---|---|---|
| Fetch a user's profile | | | |
| Replace an entire product | | | |
| Create a new order | | | |
| Update only the status of an order | | | |
| Remove a product from the catalogue | | | |

**3.2** A client sends `POST /products` with `{"name":"Laptop",...}` twice
within one second because of a network timeout. What happens in the app?
Contrast this with a client sending `PUT /products/1` with a full body twice.
Where in the source is this behavior defined? Reference the file and the
relevant HTTP method annotation.

---

## Lesson 4 — Make status codes useful

**4.1** Match each scenario to the correct HTTP status code and explain why
that code is more informative than a generic `200`:

| Scenario | Code |
|---|---|
| Product was found and returned successfully | |
| Product was created and now exists on the server | |
| Request JSON has a syntax error | |
| Product does not exist (never created) | |
| Order can't be updated because a second transition is illegal | |
| Request was valid but body fields failed constraints | |

**4.2** Read `exception/GlobalExceptionHandler.java`. For each `@ExceptionHandler`
method, write a one-sentence summary: what kind of request causes it, and what
status the client sees.

---

## Lesson 5 — Keep error responses consistent

**5.1** Read `model/ApiError.java`. Draw or write the JSON shape for a
validation failure (422) that caught errors on the `name` and `price` fields.

**5.2** You are adding a "rate-limited" error (HTTP 429). Write the `ApiError`
JSON that would be returned, including a sensible `code` and `message`.

**5.3** Why does the `code` field exist alongside `message`? Answer in two
sentences.

---

## Lesson 6 — Don't put everything into the URL path

**6.1** For each feature, state whether it belongs in the path or as a query
parameter and justify the choice:

| Feature | Path or query? | Why? |
|---|---|---|
| Fetching a specific product by ID | | |
| Filter by category | | |
| Filter by minimum price | | |
| Sort results by name descending | | |
| Page number | | |
| Searching for products containing a keyword | | |

**6.2** Write a `curl` command that fetches `products` in the `home` category,
sorted by `price:desc`, showing only page 2 with 1 page size. Use the `API-Version: 1.1` header. Paste the command and the first product from the response.

---

## Lesson 7 — Treat API changes carefully

**7.1** Read the two versioned GET handlers in `ProductController.java`.
Paste the JSON response for `GET /products/1` with `API-Version: 1.0`, and then
with `API-Version: 1.1`. List every field that differs.

**7.2** You need to add a `tags` field (a list of strings) to the product
response for a future v1.2. Is this a breaking or additive change? What does
that mean for v1.1 clients?

**7.3** Read `config/WebConfig.java`. What happens if a request is missing the
`API-Version` header entirely? Verify by running a `curl` without the header.

---

## Lesson 8 — Keep request and response formats consistent

**8.1** Audit the app's responses and note every convention followed (at least
five). For each, explain what the developer gains from it.

**8.2** Imagine a new endpoint `GET /products/{id}/reviews` that returns:
```json
[
  {
    "created_at": "2026-09-01",
    "rating": 4,
    "body": "Great product"
  }
]
```
Find every inconsistency with the app's existing conventions and rewrite the
payload to be consistent.

---

## Integration Project — Design Review Rubric

Use this rubric when reviewing a peer's integration project:

| Law | What to check | Points |
|---|---|---|
| 1 – Resources not actions | No action verbs in any URL | /10 |
| 2 – Predictable URLs | One naming convention followed everywhere | /10 |
| 3 – Correct HTTP methods | Each method matches its semantic meaning; POST explained for create, PUT for replace, PATCH for partial | /10 |
| 4 – Useful status codes | 200/201/204 for success; correct 4xx for each failure type; no "200 + error in body" | /10 |
| 5 – Consistent errors | One error body reused; includes code + message + fieldErrors | /10 |
| 6 – Path vs query | Identity in path, refinement in query; no query-param actions | /10 |
| 7 – Versioning | At least one additive change between versions; version request/header shown; consumers have a migration path | /10 |
| 8 – Format consistency | camelCase keys, ISO dates, one pagination envelope across all endpoints | /10 |
| **Total** | | **/80** |