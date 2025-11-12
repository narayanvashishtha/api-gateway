package com.example.api_gateway.ratelimiter.filter;

import com.example.api_gateway.ratelimiter.service.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class RateLimitingFilter extends OncePerRequestFilter {

    RateLimiterService rateLimiterService;
    public RateLimitingFilter(RateLimiterService rateLimiterService){
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();
        if(!rateLimiterService.isAllowed(clientIp)){
            response.setStatus(429);
            response.getWriter().write("Too many requests. Try again later.");
            response.setHeader("Retry After", "60");
            return;
        }
        filterChain.doFilter(request,response);
    }
}
