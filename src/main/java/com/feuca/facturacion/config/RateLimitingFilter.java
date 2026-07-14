package com.feuca.facturacion.config;

import com.feuca.facturacion.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.regex.Pattern;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Pattern EMPRESA_EMISION_PATH =
            Pattern.compile("^/api/v1/empresas/[^/]+/facturas/[^/]+/enviar$");
    private static final Pattern LEGACY_EMISION_PATH =
            Pattern.compile("^/api/v1/facturacion/factura/[^/]+/enviar$");

    private final RateLimitService rateLimitService;
    private final int loginMaxAttempts;
    private final long loginWindowSeconds;
    private final int emissionMaxAttempts;
    private final long emissionWindowSeconds;

    public RateLimitingFilter(
            RateLimitService rateLimitService,
            @Value("${security.rate-limit.login.max-attempts:5}") int loginMaxAttempts,
            @Value("${security.rate-limit.login.window-seconds:300}") long loginWindowSeconds,
            @Value("${security.rate-limit.emission.max-attempts:10}") int emissionMaxAttempts,
            @Value("${security.rate-limit.emission.window-seconds:60}") long emissionWindowSeconds
    ) {
        this.rateLimitService = rateLimitService;
        this.loginMaxAttempts = loginMaxAttempts;
        this.loginWindowSeconds = loginWindowSeconds;
        this.emissionMaxAttempts = emissionMaxAttempts;
        this.emissionWindowSeconds = emissionWindowSeconds;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        String ip = clientIp(request);

        if ("/api/v1/auth/login".equals(path)
                && !rateLimitService.allow("login:" + ip, loginMaxAttempts, Duration.ofSeconds(loginWindowSeconds))) {
            reject(response, "Demasiados intentos de login. Intente nuevamente mas tarde.");
            return;
        }

        if ((EMPRESA_EMISION_PATH.matcher(path).matches() || LEGACY_EMISION_PATH.matcher(path).matches())
                && !rateLimitService.allow("emision:" + ip + ":" + path, emissionMaxAttempts,
                        Duration.ofSeconds(emissionWindowSeconds))) {
            reject(response, "Demasiados intentos de emision. Intente nuevamente mas tarde.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void reject(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
