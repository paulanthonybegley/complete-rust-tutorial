package com.example.fioci;

import java.util.List;

public final class AwsCatalog {
	public static final List<AwsConcept> CONCEPTS = List.of(
		new AwsConcept("s3", "Object Storage", "Amazon S3", "minio:9000",
			"store and fetch byte blobs through an S3-compatible API"),
		new AwsConcept("rds", "Relational SQL", "Amazon RDS", "postgres:5432",
			"ACID rows with joins — the durable system of record"),
		new AwsConcept("cache", "In-Memory Cache", "Amazon ElastiCache", "redis:6379",
			"sub-millisecond reads parked in front of the database"),
		new AwsConcept("nosql", "NoSQL Store", "Amazon DynamoDB", "dynamodb-local:8000",
			"single-digit-ms access on auto-sharded partition keys"),
		new AwsConcept("queue", "Message Queue", "Amazon SQS", "localstack:4566",
			"decouple producers from consumers with a durable queue"));

	private AwsCatalog() {}
}
