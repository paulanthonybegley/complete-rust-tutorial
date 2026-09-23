# Let's Handle One Million Requests per Second — It's Scarier Than You Think!

A self-paced, eight-station simulator based on the 2024 video (Alta3 Research).
Each station measures one real wall from that recorded box:

| station | what gets measured | the wall |
|--------:|:-------------------|:---------|
| bench   | Express·Fastify·Cpeak on one thread | NIC on empty payloads (no, the framework) |
| pm2     | 1→4→12 processes, same app.js | single-thread event loop |
| nic     | 30KB vs 64KB payloads | 6 GB/s NIC bytes |
| db      | Postgres write IOPS | $/month and WAL writes/s |
| cache   | Redis GET vs DB hit | DB read misses |
| async   | 202-then-drain | front-door socket only |
| cluster | Redis shards 1→30 nodes | each node's NIC again |
| uuids   | UUID-v4 at 1M/s | 122 bits, birthday paradox |

The lab deliberately runs **without Docker**: the same Spring Boot app, the same
Thymeleaf templates, and the same htmx round-trips as the k8s simulator, so a
learner can run every recorded number on a plain laptop.
