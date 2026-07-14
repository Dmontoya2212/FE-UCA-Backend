package com.feuca.facturacion.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void usesRequestCorrelationIdInResponseAndMdcDuringRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/probe");
        MockHttpServletResponse response = new MockHttpServletResponse();
        String traceId = "trace-test-123";
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, traceId);
        AtomicReference<String> mdcDuringRequest = new AtomicReference<>();

        FilterChain chain = (servletRequest, servletResponse) -> {
            mdcDuringRequest.set(MDC.get(CorrelationIdFilter.TRACE_ID_MDC_KEY));
            assertEquals(traceId, servletRequest.getAttribute(CorrelationIdFilter.TRACE_ID_REQUEST_ATTRIBUTE));
        };

        filter.doFilter(request, response, chain);

        assertEquals(traceId, mdcDuringRequest.get());
        assertEquals(traceId, response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER));
        assertNull(MDC.get(CorrelationIdFilter.TRACE_ID_MDC_KEY));
    }
}
