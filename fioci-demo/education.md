# fioci-demo — education notes

## The five primitives and their one-line pitch
- S3 — dump any bytes, fetch them from anywhere.
- RDS — the rows that must never disagree.
- ElastiCache — the hot copies in front of those rows.
- DynamoDB — the scale-out key/attribute store.
- SQS — the buffer that lets two sides never wait for each other.

## Container-backed (real) vs simulated
The API returns `mode: live-compose` only when the port probe succeeds.
Otherwise `mode: simulated`. Both shapes have identical fields, so the front
end teaches the concept regardless of whether Docker is running.
