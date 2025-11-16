package com.example.api_gateway.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/mock")
public class MockDownstreamController {

    private final AtomicInteger counter = new AtomicInteger();

    // hit /mock/downstream?fail=true to force 500
    @GetMapping("/downstream")
    public ResponseEntity<?> downstream(@RequestParam(value = "fail", defaultValue = "false") boolean fail) {
        int c = counter.incrementAndGet();
        if (fail && (c % 1 == 0)) { // always fail when asked
            return ResponseEntity.status(500).body("simulated downstream failure");
        }
        return ResponseEntity.ok("ok-" + c);
    }

    // helper to reset internal counter (optional)
    @PostMapping("/reset-counter")
    public ResponseEntity<?> reset() {
        counter.set(0);
        return ResponseEntity.ok("reset");
    }
}

