# LinkedIn post — "I stopped fearing AWS, one docker-compose at a time"

> Suggested hook + body for the creator's timeline feed. Drop the blockquotes
> (they are staging notes, not post text) and paste the plain paragraphs.

---

**"I finally stopped finding AWS intimidating — by running it on my own laptop."**

Cloud providers feel like a wall until you realize every scary name is just a
familiar primitive wearing a costume:

- **Amazon S3** → MinIO → *put a file, get a file*
- **Amazon RDS** → Postgres → *rows that must never disagree*
- **Amazon ElastiCache** → Redis → *the hot copy in front of those rows*
- **Amazon DynamoDB** → DynamoDB Local → *scale-out key/value*
- **Amazon SQS + Lambda** → LocalStack → *nobody waits for anybody*

That's the whole trick. The vendor deepens the same stone; they don't replace
it.

So I built **fioci-demo**: a tiny Spring Boot app where each AWS concept is a
route, each route is a card, and each card talks to a real container behind one
`docker-compose.yml`. No AWS account. No credit card. No cloud bill.

The honest part — it broke on day one. Every `/station/{slug}` rendered a white
page heavily. The log said: *"Property 'dockerCompose' cannot be found."*
My data model had one field while the HTML read two. White page ≠ mystery; it's
a template that failed to render. Fix the record, restart, re-sweep all 18
routes → **every one 200**. The whole trail is in the repo's `issue-fixed.md`.

The part I liked: when Docker isn't running, the API answers in
`simulated` mode instead of crashing — so the demo teaches the concept *before*
you've touched the tooling.

**Try it if you've ever felt small in front of the AWS console:**
`make start-fioci-demo` → http://localhost:8095 — eight stations, one API,
all local.

#aws #springboot #docker #devops #learning
