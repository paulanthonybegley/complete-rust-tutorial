# One Million Requests per Second — *It's Scarier Than You Think!*

A one-hour talk built on the video's eight measured stations.

1. **The supply-side lie** — benchmarks say Node can't do it; the video says
   Wire your NIC up correctly first.
2. **One thread, three frameworks** (bench) — Express 19k, Fastify 66k,
   Cpeak 73k. The framework is not the wall.
3. **More processes, same code** (pm2) — 8k → 42k by process count alone.
4. **The NIC is the first scary thing** (nic) — 30KB payloads cut RPS by ~⅔
   because bytes, not requests, cross the wire.
5. **Postgres writes = money** (db) — IOPS knob vs writes/s vs monthly cost.
6. **Redis moves reads off the DB** (cache).
7. **Async returns 202, drains later** (async).
8. **Shard until 1M** (cluster) — 30 nodes, one recorded crossing.
9. **UUIDs are free at this scale** (uuids) — 122 bits beat the birthday
   paradox by ~10¹⁶.
10. **Takeaway** — CPU stops mattering first; then NIC; then IOPS and money.
    Your app sits on top of whichever wall ships first.
