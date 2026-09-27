package com.example.observability.obs;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;

/**
 * Small helpers around the OpenTelemetry API. At runtime the OpenTelemetry Java agent
 * supplies the SDK; when the app runs without the agent these degrade to no-ops so the
 * lab still works (just without trace ids).
 */
public final class TraceCtx {

    private TraceCtx() {}

    private static OpenTelemetry otel() {
        return GlobalOpenTelemetry.get();
    }

    public static Tracer tracer(String instrumentationName) {
        return otel().getTracer(instrumentationName, "0.0.1");
    }

    public static String traceId() {
        SpanContext ctx = new SpanContext(Span.current().getSpanContext());
        return ctx.valid() ? ctx.traceId() : "";
    }

    public static String spanId() {
        SpanContext ctx = new SpanContext(Span.current().getSpanContext());
        return ctx.valid() ? ctx.spanId() : "";
    }

    public static Context root() {
        return Context.root();
    }

    /** immutable wrapper so the class is trivially testable without an SDK */
    public record SpanContext(boolean valid, String traceId, String spanId) {
        public SpanContext(io.opentelemetry.api.trace.SpanContext c) {
            this(c.isValid(), c.getTraceId(), c.getSpanId());
        }
    }
}