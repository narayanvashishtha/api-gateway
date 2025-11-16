package com.example.api_gateway.auth.controller;

import com.example.api_gateway.ratelimiter.service.InMemoryOrderQueueService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final InMemoryOrderQueueService orderQueueService;
    private final ObjectMapper mapper = new ObjectMapper();

    public OrderController(InMemoryOrderQueueService orderQueueService) {
        this.orderQueueService = orderQueueService;
    }

    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(@RequestBody Map<String, Object> payload, @RequestHeader(value = "Authorization", required = false) String auth) {
        // minimal auth awareness: if Authorization absent, still enqueue (gateway filters handle auth)
        JsonNode node = mapper.convertValue(payload, JsonNode.class);
        String userId = "anonymous";
        if (payload.containsKey("userId")) userId = String.valueOf(payload.get("userId"));
        String queueId = orderQueueService.enqueue(node, userId);
        return ResponseEntity.ok(Map.of("status", "queued", "queueId", queueId));
    }

    @GetMapping("/queue-size")
    public ResponseEntity<?> queueSize() {
        int size = orderQueueService.size();
        return ResponseEntity.ok(Map.of("queueSize", size));
    }
}

