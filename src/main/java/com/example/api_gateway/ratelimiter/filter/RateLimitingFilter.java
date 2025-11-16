package com.example.api_gateway.ratelimiter.filter;

import com.example.api_gateway.ratelimiter.service.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class RateLimitingFilter extends OncePerRequestFilter {

    RateLimiterService rateLimiterService;

    public RateLimitingFilter(RateLimiterService rateLimiterService){
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // ✅ Skip rate limiting for auth endpoints
        if (path.equals("/login") || path.equals("/register") || path.equals("/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr();
        if (!rateLimiterService.isAllowed(clientIp)) {
            response.setStatus(429);
            response.getWriter().write("Too many requests. Try again later.");
            response.setHeader("Retry After", "60");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

