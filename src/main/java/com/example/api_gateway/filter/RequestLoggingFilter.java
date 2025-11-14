package com.example.api_gateway.filter;

import com.example.api_gateway.logging.GatewayRequestLog;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        long startTime = System.currentTimeMillis();

        try{
            filterChain.doFilter(request,response);
        }finally {
            long duration = System.currentTimeMillis() - startTime;

            GatewayRequestLog gatewayRequestLog = GatewayRequestLog.builder().requestId(requestId)
                    .method(request.getMethod()).path(request.getRequestURI())
                    .clientIp(request.getRemoteAddr()).status(response.getStatus())
                    .query(request.getQueryString()).user(request.getRemoteUser())
                    .timestamp(startTime).processingTimeMs(duration).build();

            log.info(gatewayRequestLog.toString());
            MDC.clear();
        }
    }
}
