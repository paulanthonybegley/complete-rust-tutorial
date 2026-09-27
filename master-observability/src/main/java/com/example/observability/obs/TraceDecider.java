package com.example.observability.obs;

/**
 * App-side mirror of the OpenTelemetry collector's {@code tail_sampling} policies
 * (see docker/otel-collector/otel-collector-config.yml). The /sampling lab writes this
 * decision to PostgreSQL for every synthetic request so learners can see WHAT was kept,
 * WHY it was kept, and compare that with what actually showed up in Jaeger.
 */
public final class TraceDecider {

    private final long slowThresholdMs;
    private final double keepRatio;
    private final java.util.Random random = new java.util.Random();

    public TraceDecider(long slowThresholdMs, double keepRatio) {
        this.slowThresholdMs = slowThresholdMs;
        this.keepRatio = keepRatio;
    }

    public Decision decide(long durationMs, boolean failed, boolean vip) {
        if (failed) {
            return new Decision(true, "error");
        }
        if (durationMs >= slowThresholdMs) {
            return new Decision(true, "slow");
        }
        if (vip) {
            return new Decision(true, "vip");
        }
        if (random.nextDouble() < keepRatio) {
            return new Decision(true, "sampled");
        }
        return new Decision(false, "dropped");
    }

    public record Decision(boolean kept, String reason) {}
}