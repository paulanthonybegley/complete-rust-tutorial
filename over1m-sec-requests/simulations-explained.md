# Simulations explained

Everything here is a **replay**, not a prediction: the same box, the same
payloads, the same 12-core machine the video measured. `SimLoad` owns every
number; `Over1mController` only asks which arc to show; the templates render
the measured values with zero recomputation. That keeps the lab honest — turn
a knob and the *range* moves (processes, payload bytes, IOPS, cache hit,
shards), but the ceiling each station reports is the recorded one.

This is the same trick as the k8s simulator: `LoadModels` is data, the
controller is thin, and htmx replays a small part of the page instead of the
whole course. If you can run kubectl-accurate numbers from a README, you can
FEEL why the first million RPS is a hardware problem.
