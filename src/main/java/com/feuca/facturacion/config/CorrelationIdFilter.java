package com.feuca.facturacion.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String TRACE_ID_MDC_KEY = "traceId";
    public static final String TRACE_ID_REQUEST_ATTRIBUTE = "traceId";
    private static final int MAX_CORRELATION_ID_LENGTH = 120;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = resolveTraceId(request.getHeader(CORRELATION_ID_HEADER));
        MDC.put(TRACE_ID_MDC_KEY, traceId);
        request.setAttribute(TRACE_ID_REQUEST_ATTRIBUTE, traceId);
        response.setHeader(CORRELATION_ID_HEADER, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_MDC_KEY);
        }
    }

    private String resolveTraceId(String receivedTraceId) {
        if (receivedTraceId == null || receivedTraceId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        String candidate = receivedTraceId.trim();
        if (candidate.length() > MAX_CORRELATION_ID_LENGTH) {
            return UUID.randomUUID().toString();
        }
        if (!candidate.matches("[A-Za-z0-9._:-]+")) {
            return UUID.randomUUID().toString();
        }
        return candidate;
    }
}
