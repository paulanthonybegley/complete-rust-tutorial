# Solutions — over1m-sec-requests

1. **bench** — Cpeak wins (~73k), but the winning number is tiny next to the
   NIC: empty payloads only move ~73k × ~200 bytes/s. The framework is the
   solver for the wrong problem.
2. **pm2** — yes, roughly linear (1→4→12), until a shared socket or the NIC
   saturates — look at how much *bytes* move at 12 × 42k.
3. **nic** — 30KB × 200k ≈ 6 GB/s: the NIC is the wall; "requests" misleading
   unless payload stays empty.
4. **db** — IOPS double → writes/s roughly double, bill roughly doubles; io2
   buys little more without the sharding story.
5. **cache** — Redis GETs stand in for DB trips: reads no longer pay writes,
   DB load % drops to the miss path.
6. **async** — the write stops blocking the request (202 before drain); the
   DB write wall is deferred, not removed.
7. **cluster** — the arc crosses 1M near 15 nodes and clears at 30; the NIC
   per node is the new constant.
8. **uuids** — at 1M/s, expected collision time under v4's 122 bits dwarfs the
   age of the universe — the shard key is safe; the earlier walls were not.
