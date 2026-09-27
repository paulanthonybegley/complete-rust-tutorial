package com.example.observability.domain;

import com.example.observability.obs.BusinessMetrics;
import com.example.observability.obs.TraceCtx;
import com.example.observability.obs.TraceDecider;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;
import java.util.Random;

/**
 * The "shop" that exists so something can go wrong. Every method here is instrumented the
 * way the videos prescribe:
 * <ul>
 *   <li>structured JSON logs with the trace id in the MDC (Loki + correlation)</li>
 *   <li>business metrics raised through Micrometer (Prometheus + the alert rules)</li>
 *   <li>explicit parent/child spans (Jaeger + the collector's tail_sampling)</li>
 * </ul>
 */
@Service
public class ShopService {

    private static final Logger log = LoggerFactory.getLogger(ShopService.class);
    private static final Random RANDOM = new Random();

    private static final String[] FAIL_REASONS = {"card_declined", "insufficient_funds", "timeout", "fraud_check"};

    private final JdbcTemplate jdbc;
    private final BusinessMetrics metrics;
    private final TraceDecider decider;
    private final Tracer tracer = TraceCtx.tracer("observability-shop");

    public ShopService(JdbcTemplate jdbc, BusinessMetrics metrics, TraceDecider decider) {
        this.jdbc = jdbc;
        this.metrics = metrics;
        this.decider = decider;
    }

    /** Full checkout: charge -> stock -> order, with one child span per step. */
    public CheckoutResult checkout(long customerId, int productId, int qty, String flow,
                                   boolean forceFail, long slowMs) {
        var sample = metrics.startCheckout();
        String tier = loadTier(customerId);
        boolean vip = "vip".equals(tier);

        putMdc(customerId, tier, flow);
        try {
            Span root = tracer.spanBuilder("checkout." + flow).startSpan();
            root.setAttribute("flow", flow);
            root.setAttribute("user.id", customerId);
            root.setAttribute("user.tier", tier);
            long startNanos = System.nanoTime();

            try (Scope ignored = root.makeCurrent()) {
                boolean fail = forceFail || RANDOM.nextInt(100) < 20;
                String reason = FAIL_REASONS[RANDOM.nextInt(FAIL_REASONS.length)];

                charge(root, slowMs, fail, reason);

                long orderId;
                if (fail) {
                    root.setStatus(StatusCode.ERROR, reason);
                    root.recordException(new RuntimeException("checkout failed: " + reason));
                    metrics.recordFailure(sample, reason);
                    orderId = insertOrder(customerId, productId, qty, "failed", reason);
                    long durMs = elapsedMs(startNanos);
                    log.warn("checkout failed flow={} reason={} orderId={} durationMs={}",
                            flow, reason, orderId, durMs);
                    return new CheckoutResult(traceId(), false, reason, durMs, flow, vip, orderId);
                }

                childSpanAndUpdateStock(root, productId, qty);
                childSpanAndInsertOrder(root, customerId, productId, qty);
                metrics.recordSuccess(sample);
                long durMs = elapsedMs(startNanos);
                log.info("checkout completed flow={} customerId={} qty={} durationMs={} tier={}",
                        flow, customerId, qty, durMs, tier);
                return new CheckoutResult(traceId(), true, null, durMs, flow, vip, 0);
            } finally {
                root.end();
            }
        } finally {
            clearMdc();
        }
    }

    /** A cheap root-span request, used by the /sampling lab to mint lots of small traces. */
    public SamplingResult probe(String endpoint, long durationMs, boolean fail, boolean vip) {
        Span span = tracer.spanBuilder("probe." + endpoint)
                .setParent(Context.root())
                .startSpan();
        span.setAttribute("endpoint", endpoint);
        span.setAttribute("user.tier", vip ? "vip" : "standard");
        long start = System.nanoTime();
        try (Scope ignored = span.makeCurrent()) {
            if (durationMs > 0) {
                sleep(durationMs);
            }
            if (fail) {
                span.setStatus(StatusCode.ERROR, "probe failure");
                span.recordException(new RuntimeException("probe failed"));
            }
            long durMs = elapsedMs(start);
            TraceDecider.Decision decision = decider.decide(durMs, fail, vip);
            metrics.recordSamplingDecision(decision.reason());
            log.info("sampling probe endpoint={} durationMs={} kept={} reason={} failed={}",
                    endpoint, durMs, decision.kept(), decision.reason(), fail);
            return new SamplingResult(traceId(), endpoint, durMs, fail, vip, decision.kept(), decision.reason());
        } finally {
            span.end();
        }
    }

    // ---- internals ----------------------------------------------------------

    private void charge(Span parent, long slowMs, boolean fail, String reason) {
        Span charge = tracer.spanBuilder("payment.gateway.charge")
                .setParent(Context.current())
                .startSpan();
        charge.setAttribute("provider", "mock-gateway");
        try {
            sleep(slowMs > 0 ? slowMs : 20);
            if (fail) {
                charge.setAttribute("payment.error", reason);
            }
        } finally {
            charge.end();
        }
    }

    private void childSpanAndUpdateStock(Span parent, int productId, int qty) {
        Span span = tracer.spanBuilder("db.stock.update").setParent(Context.current()).startSpan();
        try {
            String productName = jdbc.queryForObject("SELECT name FROM products WHERE id=?", String.class, productId);
            int stock = jdbc.queryForObject("SELECT stock FROM products WHERE id=?", Integer.class, productId);
            jdbc.update("UPDATE products SET stock = GREATEST(0, stock - ?) WHERE id=?", qty, productId);
            metrics.setStock(productId, productName, stock - qty);
        } finally {
            span.end();
        }
    }

    private void childSpanAndInsertOrder(Span parent, long customerId, int productId, int qty) {
        Span span = tracer.spanBuilder("db.order.insert").setParent(Context.current()).startSpan();
        try {
            insertOrder(customerId, productId, qty, "completed", null);
        } finally {
            span.end();
        }
    }

    private long insertOrder(long customerId, int productId, int qty, String status, String errorCode) {
        BigDecimal price = jdbc.queryForObject("SELECT price FROM products WHERE id=?", BigDecimal.class, productId);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO orders (customer_id, product_id, qty, total, status, error_code, trace_id) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, customerId);
            ps.setInt(2, productId);
            ps.setInt(3, qty);
            ps.setBigDecimal(4, (price == null ? BigDecimal.ZERO : price).multiply(BigDecimal.valueOf(qty)));
            ps.setString(5, status);
            ps.setString(6, errorCode);
            ps.setString(7, traceId());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKeys() == null ? null : (Number) keyHolder.getKeys().get("id");
        return key == null ? 0 : key.longValue();
    }

    private String loadTier(long customerId) {
        try {
            Map<String, Object> row = jdbc.queryForMap("SELECT tier FROM customers WHERE id=?", customerId);
            return String.valueOf(row.get("tier"));
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return "standard";
        }
    }

    private static void putMdc(long customerId, String tier, String flow) {
        MDC.put("userId", String.valueOf(customerId));
        MDC.put("tier", tier);
        MDC.put("flow", flow);
        MDC.put("tenant", "shop");
    }

    private static void clearMdc() {
        MDC.remove("userId");
        MDC.remove("tier");
        MDC.remove("flow");
        MDC.remove("tenant");
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private static String traceId() {
        return TraceCtx.traceId();
    }

    public record CheckoutResult(String traceId, boolean ok, String reason, long durationMs,
                                 String flow, boolean vip, long orderId) {}

    public record SamplingResult(String traceId, String endpoint, long durationMs, boolean failed,
                                 boolean vip, boolean kept, String reason) {}
}