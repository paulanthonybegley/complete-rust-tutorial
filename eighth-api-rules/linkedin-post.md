# LinkedIn Post: Eighth API Rules

---

**🎓 8 API Laws Every Backend Developer Should Know**

I just finished teaching an API design course, and I've condensed the biggest takeaways into 8 rules that separate junior from senior backend work.

**1️⃣ Design around resources, not actions**

Your URLs should name things, not verbs. `/products`, not `/getProducts`. Let HTTP methods do the talking.

**2️⃣ Make URLs predictable**

One naming convention, everywhere, forever. Plural nouns. camelCase. Consistent nesting. If a client can guess your next endpoint, you've won.

**3️⃣ Use HTTP methods for their purpose**

GET reads. POST creates. PUT replaces. PATCH patches. DELETE removes. And respect idempotency — safe methods stay safe.

**4️⃣ Make status codes useful**

201 for created. 204 for deleted. 400/404/409/422 for the bad stuff. Never return "200 + error body". Your status code IS your first error message.

**5️⃣ Keep error responses consistent**

One error shape for every endpoint:
```
{"status":404,"code":"PRODUCT_NOT_FOUND","message":"Product 999 was not found"}
```

**6️⃣ Don't put everything in the path**

Identity goes in the path (`/products/5`), refinement goes in query parameters (`?category=home&sort=price:desc&page=0&size=20`).

**7️⃣ Treat API changes carefully**

Prefer additive changes (new fields, new endpoints). Version deliberately — and stick to one strategy. Same URL, two contracts:
- `API-Version: 1.0` → `{"id":1,"name":"Clean Code","price":39.99}`
- `API-Version: 1.1` → adds `currency` + `stock` (non-breaking)

**8️⃣ Keep formats consistent**

camelCase keys. ISO-8601 dates. One pagination envelope everywhere. Consistency is what lets clients stop reading your docs.

---

**Pro Tip:** A good API is one clients can understand without constantly checking documentation. Start by defining the patterns, not the endpoints.

The whole course ships as a runnable Spring Boot 4 app — every law demonstrated in working code, with `API-Version` headers, consistent error bodies, and 19 passing tests.

Which bad API habit have you seen most in the wild? Drop it in the comments 👇

---

*#API #Backend #SoftwareEngineering #SpringBoot #APIDesign #WebDevelopment #TechLearning #Programming*