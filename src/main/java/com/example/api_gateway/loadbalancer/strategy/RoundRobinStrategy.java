package com.example.api_gateway.loadbalancer.strategy;

import com.example.api_gateway.loadbalancer.model.ServiceInstance;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinStrategy {

    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>(); ;

    public ServiceInstance select(String serviceName, List<ServiceInstance> instances){
        if (instances == null || instances.isEmpty()) return null;
        AtomicInteger count = counters.computeIfAbsent(serviceName, k -> new AtomicInteger(0));
        int current = count.getAndIncrement();
        int index = (current & Integer.MAX_VALUE) % instances.size();


        return instances.get(index);
    }
}
