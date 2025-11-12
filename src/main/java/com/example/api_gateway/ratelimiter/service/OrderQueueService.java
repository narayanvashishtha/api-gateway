package com.example.api_gateway.ratelimiter.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface OrderQueueService {
    /**
     * Enqueue the order payload. Returns a generated orderId/queueId.
     */
    String enqueue(JsonNode orderPayload, String userId);
}
