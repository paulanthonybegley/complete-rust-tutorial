#posting #systems #webdev

The scariest sentence in web performance isn't about your framework — it's
about your NIC.

10 minutes into "Let's Handle One Million Requests per Second, It's Scarier
Than You Think!", they empty the payload and Fastify still can't pass ~73k RPS
on one thread. The NIC at 6 GB/s is fine. The CPU is fine. The framework just
isn't the bottleneck — and that's the whole video.

I turned all eight measured stations into one Spring Boot sim (Thymeleaf +
htmx, no Docker, one port):

  · bench — Express vs Fastify vs a C++ server, one thread
  · pm2 — the same app.js scaled to 12 processes
  · nic — pay the NIC bytes, not the framework
  · db — Postgres gets cheaper RPS all the way to $1,200/mo
  · cache — Redis yanks reads off the DB
  · async — 202 first, drain later
  · cluster — 30 Redis nodes finally cross 1M RPS
  · uuids — 122 bits of UUID-v4: the birthday paradox apologizes

Run every recorded number on your own laptop:

  git clone … && cd over1m-sec-requests && make start-over1m

Scalability needs thinking about hardware before heroes.
