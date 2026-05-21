package com.example.capacity.domain.api;

import com.example.capacity.domain.model.Capacity;
import reactor.core.publisher.Mono;

public interface CapacityServicePort {
    Mono<Capacity> registerCapacity(Capacity capacity);
}
