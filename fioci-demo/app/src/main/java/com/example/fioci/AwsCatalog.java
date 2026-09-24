package com.example.fioci;

import java.util.List;

public final class AwsCatalog {
	public static final List<AwsConcept> CONCEPTS = List.of(
		new AwsConcept("s3", "Object Storage", "Amazon S3", "minio:9000",
			"store and fetch byte blobs through an S3-compatible API",
			"minio live at minio:9000 — compose service 'minio'",
			"GET /api/s3 \u2192 live-compose when MinIO answers"),
		new AwsConcept("rds", "Relational SQL", "Amazon RDS", "postgres:5432",
			"ACID rows with joins — the durable system of record",
			"postgres 16 on 5432 — compose service 'postgres'",
			"GET /api/rds \u2192 live-compose when Postgres answers"),
		new AwsConcept("elasticache", "In-Memory Cache", "Amazon ElastiCache", "redis:6379",
			"sub-millisecond reads parked in front of the database",
			"redis 7 on 6379 — compose service 'redis'",
			"GET /api/elasticache \u2192 live-compose when Redis answers"),
		new AwsConcept("dynamodb", "NoSQL Store", "Amazon DynamoDB", "dynamodb-local:8000",
			"single-digit-ms access on auto-sharded partition keys",
			"dynamodb-local on 8000 — compose service 'dynamodb'",
			"GET /api/dynamodb \u2192 live-compose when DynamoDB Local answers"),
		new AwsConcept("sqs", "Message Queue", "Amazon SQS", "localstack:4566",
			"decouple producers from consumers with a durable queue",
			"localstack on 4566 — compose service 'localstack'",
			"GET /api/sqs \u2192 live-compose when LocalStack answers"),
		new AwsConcept("lambda", "Serverless", "AWS Lambda", "localstack:4566",
			"run code without provisioning servers",
			"localstack on 4566 — compose service 'localstack'",
			"GET /api/lambda \u2192 live-compose when LocalStack answers"),
		new AwsConcept("cloudfront", "CDN", "Amazon CloudFront", "minio:9000",
			"edge cache in front of the origin",
			"minio on 9000 — compose service 'minio'",
			"GET /api/cloudfront \u2192 live-compose when MinIO answers"),
		new AwsConcept("iam", "Identity & Access", "AWS IAM", "dynamodb-local:8000",
			"policies scoping who may call what",
			"dynamodb-local on 8000 — compose service 'dynamodb'",
			"GET /api/iam \u2192 live-compose when DynamoDB Local answers"));

	private AwsCatalog() {}
}
