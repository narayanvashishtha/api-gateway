package com.example.api_gateway.loadbalancer.registry;

import com.example.api_gateway.loadbalancer.model.ServiceInstance;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ServiceRegistry {

    private final Map<String, List<ServiceInstance>> registry = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        // Hard-coded instances for dev/test. Replace with dynamic discovery later.
        registry.put("TRADE_SERVICE", new ArrayList<>(List.of(
                new ServiceInstance("TRADE_SERVICE","http://localhost:8081"),
                new ServiceInstance("TRADE_SERVICE","http://localhost:8082"),
                new ServiceInstance("TRADE_SERVICE","http://localhost:8083")
        )));

        registry.put("USER_SERVICE", new ArrayList<>(List.of(
                new ServiceInstance("USER_SERVICE","http://localhost:8090"),
                new ServiceInstance("USER_SERVICE","http://localhost:8091")
        )));
    }

    public List<ServiceInstance> getInstances(String serviceName) {
        List<ServiceInstance> list = registry.get(serviceName);
        return list == null ? Collections.emptyList() : Collections.unmodifiableList(list);
    }

    public void registerInstance(String serviceName, ServiceInstance instance){
        registry.computeIfAbsent(serviceName, k -> new ArrayList<>()).add(instance);
    }
}
