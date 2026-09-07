# Lesson 4: Caching Deep Dive — Walkthrough

## Learning Goal
Understand caching strategies, patterns, and invalidation techniques.

---

## 1. Why Cache?

**Without cache:** Every request hits the database → slow, expensive
**With cache:** Frequent requests served from memory → fast, cheap

| Metric | Without Cache | With Cache |
|--------|---------------|------------|
| Latency | 100ms | 1ms |
| DB Load | High | Low |
| Cost | High | Lower |

---

## 2. Cache Patterns

### Cache-Aside (Lazy Loading)
```
App → Check Cache → Hit? Return / Miss? Query DB → Store in Cache → Return
```
**Use case:** Read-heavy workloads

### Write-Through
```
App → Write to Cache AND DB simultaneously
```
**Use case:** Strong consistency needed

### Write-Behind (Write-Back)
```
App → Write to Cache → Async write to DB later
```
**Use case:** Write performance critical
**Risk:** Data loss if cache fails before DB write

### Write-Around
```
App → Write directly to DB (skip cache)
```
**Use case:** Rarely read data

---

## 3. Cache Eviction Policies

| Policy | Description |
|--------|-------------|
| **LRU** | Least Recently Used |
| **LFU** | Least Frequently Used |
| **FIFO** | First In, First Out |
| **TTL** | Time To Live (expires after set time) |

---

## 4. Cache Invalidation

**The hard problem:** When source data changes, how do you update the cache?

### Strategies
1. **TTL-based**: Auto-expire after X seconds
2. **Event-driven**: Invalidate on data change events
3. **Version-based**: Include version in cache key

---

## 5. Redis vs Memcached

| Feature | Redis | Memcached |
|---------|-------|-----------|
| Data structures | Rich (strings, lists, sets, hashes) | Simple (key-value) |
| Persistence | Yes | No |
| Clustering | Yes | Limited |
| Memory efficiency | Moderate | High |

---

## 6. Activity: Cache Design

Design caching for a news website:
1. What content to cache? (articles, user sessions)
2. What eviction policy? (LRU for articles)
3. What TTL? (5 min for breaking news, 1 hour for regular)
4. How to invalidate? (Event-driven on article update)

---

## 7. Assessment

Explain the trade-off between write-through and write-behind caching. When would you choose each?
