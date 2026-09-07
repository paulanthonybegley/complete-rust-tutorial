# Lesson 5: Database Design — Walkthrough

## Learning Goal
Choose between SQL and NoSQL, understand sharding and replication strategies.

---

## 1. SQL vs NoSQL

| Feature | SQL | NoSQL |
|---------|-----|-------|
| **Schema** | Rigid (predefined) | Flexible (dynamic) |
| **Scaling** | Vertical | Horizontal |
| **Consistency** | Strong (ACID) | Eventual |
| **Query Language** | SQL | Varies |
| **Best For** | Complex queries, transactions | Large scale, simple queries |

### Examples
- **SQL:** PostgreSQL, MySQL, Oracle
- **NoSQL:** MongoDB, Cassandra, DynamoDB

---

## 2. Normalization vs Denormalization

### Normalized (3NF)
- Eliminate data redundancy
- Multiple tables with relationships
- More joins, less storage

### Denormalized
- Duplicate data for read performance
- Fewer joins, more storage
- Write complexity increases

---

## 3. Sharding (Partitioning)

**Purpose:** Split large database across multiple servers.

### Sharding Strategies
| Strategy | Description |
|----------|-------------|
| **Hash-based** | Hash shard key to determine placement |
| **Range-based** | Split by value ranges |
| **Geographic** | Split by user location |

### Shard Key Considerations
- Even distribution
- Query patterns
- Growth predictability

---

## 4. Replication

### Types
| Type | Description |
|------|-------------|
| **Master-Slave** | One write leader, multiple read replicas |
| **Master-Master** | Multiple write leaders |
| **Synchronous** | All replicas updated before commit |
| **Asynchronous** | Replicas updated after commit |

---

## 5. CAP Theorem

**Choose 2 of 3:**
- **Consistency**: All nodes see same data
- **Availability**: Every request gets response
- **Partition Tolerance**: System works despite network failures

| System | Prioritizes |
|--------|-------------|
| PostgreSQL | CP (Consistency + Partition tolerance) |
| Cassandra | AP (Availability + Partition tolerance) |

---

## 6. Activity: Database Selection

Choose SQL or NoSQL for:
1. Banking system → **SQL** (strong consistency)
2. Social media feed → **NoSQL** (scale, flexibility)
3. E-commerce inventory → **SQL** (transactions)
4. IoT sensor data → **NoSQL** (write throughput)

---

## 7. Assessment

Explain why you can't have all three: consistency, availability, and partition tolerance.
