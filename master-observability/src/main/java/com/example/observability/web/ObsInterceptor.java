package com.example.observability.web;

import com.example.observability.obs.TraceCtx;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Pushes request context into the SLF4J MDC so every service-level log line carries
 * tenant/user context (the JSON encoder emits the whole MDC; promtail lifts trace_id
 * to a Loki label). Also stamps the response with <code>X-Trace-Id</code> so a learner
 * running <code>curl -i</code> can see that every single HTTP request has a trace.
 */
@Component
public class ObsInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        MDC.put("tenant", "shop");
        String userId = request.getParameter("user");
        if (userId == null) {
            userId = request.getParameter("customerId");
        }
        if (userId != null && !userId.isEmpty()) {
            MDC.put("userId", userId);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        MDC.remove("tenant");
        MDC.remove("userId");
        String traceId = TraceCtx.traceId();
        if (traceId != null && !traceId.isEmpty()) {
            response.setHeader("X-Trace-Id", traceId);
        }
    }
}