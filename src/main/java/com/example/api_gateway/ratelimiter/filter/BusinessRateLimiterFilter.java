package com.example.api_gateway.ratelimiter.filter;

import com.example.api_gateway.ratelimiter.service.MarketDataService;
import com.example.api_gateway.ratelimiter.service.OrderQueueService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

@Component
public class BusinessRateLimiterFilter extends OncePerRequestFilter {

    private final ObjectMapper mapper = new ObjectMapper();
    private final MarketDataService marketDataService;
    private final OrderQueueService orderQueueService;

    public BusinessRateLimiterFilter(MarketDataService m, OrderQueueService q) {
        this.marketDataService = m;
        this.orderQueueService = q;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1) only handle order endpoint
        if (!request.getMethod().equalsIgnoreCase("POST") || !request.getRequestURI().startsWith("/api/orders")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2) get authenticated userId
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            filterChain.doFilter(request, response); // let auth layer handle missing auth
            return;
        }
        String userId = auth.getName();

        // 3) wrap request so body can be read safely
        ContentCachingRequestWrapper cached = new ContentCachingRequestWrapper(request);

        // 4) read the body now (this populates the cached content)
        String body = StreamUtils.copyToString(cached.getInputStream(), StandardCharsets.UTF_8);
        if (body == null || body.isBlank()) {
            reject(response, SC_BAD_REQUEST, "empty body");
            return;
        }

        JsonNode orderJson;
        try {
            orderJson = mapper.readTree(body);
        } catch (Exception e) {
            reject(response, SC_BAD_REQUEST, "invalid json");
            return;
        }

        // 5) minimal safe field extraction
        String type = Optional.ofNullable(orderJson.get("type")).map(JsonNode::asText).orElse("");
        String side = Optional.ofNullable(orderJson.get("side")).map(JsonNode::asText).orElse("");
        String symbol = Optional.ofNullable(orderJson.get("symbol")).map(JsonNode::asText).orElse("");
        long qty = Optional.ofNullable(orderJson.get("quantity")).map(JsonNode::asLong).orElse(0L);
        double price = orderJson.has("price") ? orderJson.get("price").asDouble() : Double.NaN;

        // 6) price sanity check for LIMIT orders
        if ("LIMIT".equalsIgnoreCase(type)) {
            Optional<Double> ltpOpt = marketDataService.getLtp(symbol);
            if (ltpOpt.isEmpty()) {
                reject(response, SC_BAD_REQUEST, "LTP not available for symbol " + symbol);
                return;
            }
            double ltp = ltpOpt.get();
            if ("BUY".equalsIgnoreCase(side) && price < ltp) {
                reject(response, SC_BAD_REQUEST, "Limit BUY price cannot be less than LTP");
                return;
            }
            if ("SELL".equalsIgnoreCase(side) && price > ltp) {
                reject(response, SC_BAD_REQUEST, "Limit SELL price cannot be greater than LTP");
                return;
            }
        }

        // 7) large order check -> queue
        if (qty > 1000) {
            String queueId = orderQueueService.enqueue(orderJson, userId);
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":\"QUEUED\",\"orderId\":\"" + queueId + "\"}");
            response.getWriter().flush();
            return;
        }

        // 8) pass through to controller — IMPORTANT: pass cached wrapper not original
        filterChain.doFilter(cached, response);
    }

    private void reject(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"error\":\"" + message + "\"}");
        resp.getWriter().flush();
    }
}
