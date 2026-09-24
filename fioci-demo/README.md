# fioci-demo — AWS concepts, one docker-compose at a time

A small educational Spring Boot 4 API that walks you through the **five most
reached-for AWS building blocks** (S3, RDS, ElastiCache, DynamoDB, SQS/Lambda),
each one mapped to a real local container behind one `docker-compose.yml`.

## Why

Cloud providers are intimidating until you see the *same primitive* running on
your own laptop:
- **S3**         -> MinIO          (object storage)
- **RDS**        -> Postgres       (relational)
- **ElastiCache**-> Redis          (in-memory cache)
- **DynamoDB**   -> DynamoDB Local (NoSQL)
- **SQS/Lambda** -> LocalStack     (queue + serverless)

The app is just a thin Thymeleaf+htmx front end over a JSON API. It TALKS to the
containers when they are up; when they are not it answers `"mode":"simulated"`
so the API never breaks your demo.

## Run

    make start-fioci-demo          # app on http://localhost:8095
    make stop-fioci-demo
    docker compose -f fioci-demo/docker-compose.yml up -d   # real backing services

## API

    GET /                         stations grid + progress meter
    GET /station/{slug}           one concept page
    GET /api                      all concepts
    GET /api/{slug}               one concept (+ live backing probe)
