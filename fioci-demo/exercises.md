# fioci-demo — exercises

1. `docker compose up -d` then reload `/api/s3` — notice mode flips.
2. Add a station: create the `AwsConcept`, add its `catalog` entry, and a
   template. The meter re-sizes itself from `CONCEPTS.size()`.
3. Point the S3 concept at MinIO's real endpoint and PUT a blob.
4. Add a health-probe Docker service of your own and wire `AwsConcept.backing`.
