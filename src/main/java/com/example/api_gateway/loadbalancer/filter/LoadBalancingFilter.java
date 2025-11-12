package com.example.api_gateway.loadbalancer.filter;

import com.example.api_gateway.loadbalancer.service.LoadBalancerService;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * Simple synchronous load-balancing forwarder.
 * For demo: forwards requests whose path starts with /proxy/{logical}/... to a backend chosen by LoadBalancerService.

 * Note: This is intentionally synchronous and simple so it's easy to reason about and compile.
 * Production: add timeouts, circuit-breaker, streaming bodies, preserve all headers correctly, health checks etc.
 */
@Component
@Order(300)
public class LoadBalancingFilter extends OncePerRequestFilter {

    private final LoadBalancerService loadBalancerService;
    private final RestTemplate restTemplate;

    public LoadBalancingFilter(LoadBalancerService loadBalancerService, RestTemplate restTemplate) {
        this.loadBalancerService = loadBalancerService;
        this.restTemplate = restTemplate;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Only handle proxy paths. Adjust to your routing scheme.
        return !request.getRequestURI().startsWith("/proxy/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Example path: /proxy/trade/api/orders
        String path = request.getRequestURI(); // e.g. /proxy/trade/api/orders
        String[] parts = path.split("/");
        if (parts.length < 3) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid proxy path");
            return;
        }

        String logical = parts[2]; // e.g. "trade"
        String serviceName = mapLogicalToServiceName(logical); // e.g. "TRADE_SERVICE"

        String backendBase = loadBalancerService.getNextUrl(serviceName);
        if (backendBase == null) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().write("No backend instances available for " + serviceName);
            return;
        }

        // derive the path to forward (strip /proxy/{logical})
        String forwardPath = path.substring(("/proxy/" + logical).length());
        if (forwardPath.isEmpty()) forwardPath = "/";

        // Wrap request to safely read body without consuming it for downstream (though we respond directly here)
        ContentCachingRequestWrapper cached = new ContentCachingRequestWrapper(request);
        String body = StreamUtils.copyToString(cached.getInputStream(), StandardCharsets.UTF_8);

        // Build target URL
        String targetUrl = backendBase + forwardPath + (request.getQueryString() == null ? "" : "?" + request.getQueryString());

        // Build headers for forwarding
        HttpHeaders headers = new HttpHeaders();
        // copy headers except Host / Content-Length (let RestTemplate manage)
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                if (name == null) continue;
                if (name.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH) || name.equalsIgnoreCase(HttpHeaders.HOST)) continue;
                Enumeration<String> values = request.getHeaders(name);
                while (values.hasMoreElements()) {
                    headers.add(name, values.nextElement());
                }
            }
        }
        // Ensure content-type if body present
        if (body != null && !body.isBlank()) {
            String ct = request.getContentType();
            if (ct != null) headers.set(HttpHeaders.CONTENT_TYPE, ct);
            else headers.setContentType(MediaType.APPLICATION_JSON);
        }

        HttpMethod method;
        try {
            method = HttpMethod.valueOf(request.getMethod());
        } catch (IllegalArgumentException ex) {
            method = HttpMethod.POST; // fallback if unknown
        }

        HttpEntity<String> forwardEntity = new HttpEntity<>(body, headers);

        ResponseEntity<byte[]> backendResponse;
        try {
            backendResponse = restTemplate.exchange(targetUrl, method, forwardEntity, byte[].class);
        } catch (RestClientException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.getWriter().write("Error forwarding to backend: " + ex.getMessage());
            return;
        }

        // copy status
        response.setStatus(backendResponse.getStatusCodeValue());

        // copy headers from backend response (simplified)
        HttpHeaders respHeaders = backendResponse.getHeaders();
        respHeaders.forEach((name, values) -> {
            // avoid overriding certain headers managed by container
            if (name.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING)) return;
            for (String val : values) {
                response.addHeader(name, val);
            }
        });

        // copy body
        byte[] respBody = backendResponse.getBody();
        if (respBody != null && respBody.length > 0) {
            // set content-type if present in response headers
            String respCt = respHeaders.getFirst(HttpHeaders.CONTENT_TYPE);
            if (respCt != null) response.setContentType(respCt);
            response.getOutputStream().write(respBody);
        }
    }

    private String mapLogicalToServiceName(String logical) {
        return switch (logical.toLowerCase()) {
            case "trade" -> "TRADE_SERVICE";
            case "user" -> "USER_SERVICE";
            default -> logical.toUpperCase() + "_SERVICE";
        };
    }
}
