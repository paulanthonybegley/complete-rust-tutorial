package com.example.postgresstack.queue;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class QueueWorker implements CommandLineRunner {

    public static final int WORKERS = 3;

    private final QueueService queue;
    private final ScheduledExecutorService pool;
    private final AtomicLong processedOk = new AtomicLong();
    private final AtomicLong processedFailed = new AtomicLong();
    private final AtomicLong totalProcessedMs = new AtomicLong();
    private final long startedAt = System.currentTimeMillis();

    public QueueWorker(QueueService queue) {
        this.queue = queue;
        this.pool = Executors.newScheduledThreadPool(WORKERS);
    }

    @Override
    public void run(String... args) {
        for (int i = 0; i < WORKERS; i++) {
            pool.scheduleWithFixedDelay(this::tick, 0, 150, TimeUnit.MILLISECONDS);
        }
    }

    private void tick() {
        queue.claim().ifPresent(this::runJob);
    }

    private void runJob(QueueService.Job job) {
        long start = System.currentTimeMillis();
        try {
            sleep(job.kind());
            if (queue.wantsToFail(job.payload())) {
                throw new IllegalStateException("simulated failure (payload said fail)");
            }
            queue.complete(job.id(), "processed " + job.kind());
            processedOk.incrementAndGet();
        } catch (Exception e) {
            processedFailed.incrementAndGet();
            queue.fail(job.id(), job.attempts(), job.maxAttempts(), e.getMessage());
        } finally {
            totalProcessedMs.addAndGet(System.currentTimeMillis() - start);
        }
    }

    private void sleep(String kind) {
        long millis = switch (kind) {
            case "email" -> 1200 + (long) (Math.random() * 1800);
            case "report" -> 2000 + (long) (Math.random() * 2500);
            case "thumbnail" -> 500 + (long) (Math.random() * 1000);
            default -> 800 + (long) (Math.random() * 1500);
        };
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public long doneCount() {
        return processedOk.get();
    }

    public long failedCount() {
        return processedFailed.get();
    }

    public double processedPerMinute() {
        long elapsedSec = Math.max(1, (System.currentTimeMillis() - startedAt) / 1000);
        return Math.round(processedOk.get() * 6000.0 / elapsedSec) / 100.0;
    }

    public long avgProcessedMs() {
        long total = processedOk.get() + processedFailed.get();
        return total == 0 ? 0 : totalProcessedMs.get() / total;
    }

    public long uptimeSeconds() {
        return (System.currentTimeMillis() - startedAt) / 1000;
    }

    @PreDestroy
    public void shutdown() {
        pool.shutdownNow();
    }
}