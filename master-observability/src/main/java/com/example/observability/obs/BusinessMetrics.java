package com.example.observability.obs;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The metrics that matter. The videos' point: a CPU spike is boring, a failed checkout is
 * not. So the shop instruments its <em>business</em> flow — checkouts — not just JVM health.
 * <p>
 * Every counter is also exported to Prometheus via /actuator/prometheus, where the alert
 * rules in docker/prometheus/alert-rules.yml watch the failure ratio.
 */
@Component
public class BusinessMetrics {

    private final MeterRegistry registry;

    private final Counter successes;
    private final Counter failures;                 // checkout_failure_total{reason}
    private final ConcurrentMap<String, Counter> failureByReason = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> samplingByReason = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicInteger> stock = new ConcurrentHashMap<>();

    private final Timer checkoutTimer;

    public BusinessMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.checkoutTimer = Timer.builder("checkout_duration")
                .description("Time to run one checkout end to end")
                .publishPercentiles(0.50, 0.95)
                .register(registry);
        this.successes = Counter.builder("checkout_total")
                .description("Completed checkouts")
                .tag("outcome", "success")
                .register(registry);
        this.failures = Counter.builder("checkout_failure_total")
                .description("Failed checkouts")
                .register(registry);
    }

    public Timer.Sample startCheckout() {
        return Timer.start(registry);
    }

    public void recordSuccess(Timer.Sample sample) {
        sample.stop(checkoutTimer);
        successes.increment();
    }

    public void recordFailure(Timer.Sample sample, String reason) {
        sample.stop(checkoutTimer);
        failures.increment();
        failureByReason.computeIfAbsent(reason,
                r -> Counter.builder("checkout_failure_total").tag("reason", r).register(registry)).increment();
    }

    public void recordSamplingDecision(String reason) {
        samplingByReason.computeIfAbsent(reason,
                r -> Counter.builder("trace_sampling_decision").tag("reason", r).register(registry)).increment();
    }

    public void setStock(int productId, String name, int value) {
        AtomicInteger gauge = stock.computeIfAbsent("p" + productId, k -> {
            AtomicInteger ai = new AtomicInteger(value);
            registry.gauge("stock_level", Tags.of("product", name), ai, AtomicInteger::get);
            return ai;
        });
        gauge.set(value);
    }

    public java.util.List<String> stockKeys() {
        return java.util.List.copyOf(stock.keySet());
    }
}