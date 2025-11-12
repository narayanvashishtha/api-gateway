package com.example.api_gateway.ratelimiter.service;

import java.util.Optional;

public interface MarketDataService {
    /**
     * Return LTP for symbol if available.
     */
    Optional<Double> getLtp(String symbol);

    /**
     * Update LTP (convenience for tests/dev)
     */
    void updateLtp(String symbol, double ltp);
}
