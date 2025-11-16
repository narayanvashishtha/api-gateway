package com.example.api_gateway.auth.controller;


import com.example.api_gateway.ratelimiter.service.MarketDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/market")
public class MarketDataController {

    private final MarketDataService marketDataService;

    public MarketDataController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<?> getLtp(@PathVariable String symbol) {
        Optional<Double> ltp = marketDataService.getLtp(symbol);
        return ltp.map(value -> ResponseEntity.ok(Map.of("symbol", symbol.toUpperCase(), "ltp", value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

