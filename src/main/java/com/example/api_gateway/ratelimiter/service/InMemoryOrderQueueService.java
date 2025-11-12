package com.example.api_gateway.ratelimiter.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

@Component
class InMemoryOrderQueueService implements OrderQueueService {
    private final BlockingQueue<JsonNode> queue = new LinkedBlockingQueue<>();
    private final AtomicLong idGen = new AtomicLong(1000);

    @Override
    public String enqueue(JsonNode orderPayload, String userId) {
        String id = "Q-" + idGen.incrementAndGet();
        if (orderPayload.isObject()) {
            ((com.fasterxml.jackson.databind.node.ObjectNode) orderPayload).put("_queuedBy", userId);
            ((com.fasterxml.jackson.databind.node.ObjectNode) orderPayload).put("_queueId", id);
        }
// Best-effort enqueue (non-blocking)
        queue.offer(orderPayload);
        return id;
    }

    // dev helper
    public int size() { return queue.size(); }
}

