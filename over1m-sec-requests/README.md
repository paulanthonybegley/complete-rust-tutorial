# over1m-sec-requests

*Let's Handle One Million Requests per Second — It's Scarier Than You Think!*

An eight-station lab that replays the real autocannon runs from the 2024 video
("Let's Handle One Million Requests per Second, It's Scarier Than You Think!",
where a 12-core box and a 6 GB/s NIC are the two honest walls). Every number
comes from `SimLoad`, every page renders with Thymeleaf + htmx, and nothing
needs Docker.

## Run it

```console
make start-over1m    # Spring Boot on :8094
make stop-over1m
```

## The eight stations

1. **bench** — Express vs Fastify vs Cpeak on one thread (a framework is not the wall).
2. **pm2** — same app.js, more processes; the single-thread 8k → 42k arc.
3. **nic** — the 6 GB/s NIC counts bytes, not requests (payload is the real RPS killer).
4. **db** — Postgres writes/s die at IOPS and monthly cost (iostat, not curl).
5. **cache** — Redis moves hot reads off Postgres (reads no longer pay writes).
6. **async** — 202 Accepted first, worker drains later (the request stops waiting).
7. **cluster** — Redis cluster shards keys; 1M RPS finally clears on the recorded box.
8. **uuids** — UUID-v4 at 1M/s: 122 bits mean the birthday paradox never catches you.

Visit them in that order; the little meter in the corner tracks where you are.

## Why it's scarier than you think

The video's point is not that frameworks are slow — it's that after you cross a
few hundred thousand RPS nothing you're writing in app code moves the needle.
One NIC, one Postgres writer, one Redis shard each become the wall long before
your framework does.
