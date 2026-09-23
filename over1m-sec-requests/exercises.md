# over1m exercises — one measured wall per station

1. **bench** — Run the three Dockerbuild rows. Which framework wins, and why is
   that number *not* why the video is scary?
2. **pm2** — Go 1→4→12 instances. Does RPS scale linearly, and what stops it
   from scaling forever?
3. **nic** — Switch payload from empty to 30KB to 64KB. Recalculate "request
   bytes × RPS" and compare it with a 6 GB/s NIC.
4. **db** — Double the IOPS knob. How much do writes/s and the monthly bill
   move? Predict the gp3→io2 jump before you click it.
5. **cache** — Turn Redis on. Watch reads that used to hit Postgres stop
   hitting it. What's the new DB load percentage?
6. **async** — Post an order and watch the 202 come back before the drain.
   Which wall does async write put off?
7. **cluster** — Grow 1→2→4→15→30 nodes. Where does the line cross 1M RPS, and
   which number (not the framework) becomes the ceiling?
8. **uuids** — At 1M UUIDs/s count how many generations of typing it would take
   before one collision becomes likely. (Answer: you will not live to type it.)
