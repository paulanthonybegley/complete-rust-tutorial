# Lesson plan — 1M RPS in eight stations (45–60 min)

**Setup (5 min)** — `make start-over1m`, open :8094.

**Stations 1–2 (12 min)** — Bench the three frameworks on one thread (say the
6 GB/s NIC is the real constant, not the framework), then raise the process
count with pm2 and watch the single-thread wall bend.

**Stations 3–4 (15 min)** — NIC bytes with bigger payloads (the wall that
actually shows up first), then Postgres IOPS vs monthly cost (iostat replaces
curl; money replaces CPU as the variable).

**Stations 5–6 (12 min)** — Redis warms reads off the DB path, then async
writes return 202 before the drain — the request no longer waits on the write.

**Stations 7–8 (12 min)** — Shard Redis all the way to 30 nodes and watch the
arc cross 1,000,000 RPS where the video crosses it; close with UUID-v4's 122
bits and why the birthday paradox is safe at this rate.

**Close (4 min)** — One sentence per station: "the wall moved from CPU to NIC
to IOPS to $, and your app code only ever sat on top of one of them."
