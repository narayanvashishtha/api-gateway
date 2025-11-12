package com.example.api_gateway.ratelimiter.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
class InMemoryMarketDataService implements MarketDataService {
    private final Map<String, Double> ltp = new ConcurrentHashMap<>();

    public InMemoryMarketDataService() {
// some realistic defaults for local testing
        ltp.put("RELIANCE", 2500.0);
        ltp.put("TCS", 3600.0);
        ltp.put("HDFC", 1600.0);
        ltp.put("INFY", 1400.0);
    }

    @Override
    public Optional<Double> getLtp(String symbol) {
        if (symbol == null) return Optional.empty();
        return Optional.ofNullable(ltp.get(symbol.toUpperCase()));
    }

    @Override
    public void updateLtp(String symbol, double ltpVal) {
        if (symbol != null) ltp.put(symbol.toUpperCase(), ltpVal);
    }
}