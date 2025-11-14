package com.example.api_gateway.circuitbreaker;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CircuitBreakerFilter extends OncePerRequestFilter {

    Map<String, CircuitBreakerState> responseTracker = new ConcurrentHashMap<>();

    @Autowired
    CircuitBreakerService circuitBreakerService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String serviceName = (String) request.getAttribute("SERVICE_NAME");
        String backendUrl = (String) request.getAttribute("TARGET_BACKEND_URL");

        if (!circuitBreakerService.allowRequest(serviceName, backendUrl)) {
            response.setStatus(503);
            response.getWriter().write("Circuit open for " + serviceName);
            return;
        }

        try {
            filterChain.doFilter(request, response);
            int status = response.getStatus();
            if (status >= 500) {
                circuitBreakerService.recordFailure(serviceName, backendUrl);
            } else {
                circuitBreakerService.recordSuccess(serviceName, backendUrl);
            }
        } catch (Exception e) {
            circuitBreakerService.recordFailure(serviceName, backendUrl);
            response.setStatus(500);
            response.getWriter().write("Backend error for " + serviceName);
        }
    }
}
