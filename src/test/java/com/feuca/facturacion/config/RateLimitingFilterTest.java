package com.feuca.facturacion.config;

import com.feuca.facturacion.service.RateLimitService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RateLimitingFilterTest {

    @Test
    void ignoresClientSuppliedForwardedForHeader() throws Exception {
        RateLimitService rateLimitService = mock(RateLimitService.class);
        when(rateLimitService.allow(startsWith("login:"), org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.any())).thenReturn(true);
        RateLimitingFilter filter = new RateLimitingFilter(rateLimitService, 5, 300, 10, 60);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setRemoteAddr("10.0.0.7");
        request.addHeader("X-Forwarded-For", "203.0.113.25");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        verify(rateLimitService).allow(
                org.mockito.ArgumentMatchers.eq("login:10.0.0.7"),
                org.mockito.ArgumentMatchers.eq(5),
                org.mockito.ArgumentMatchers.any()
        );
    }
}
