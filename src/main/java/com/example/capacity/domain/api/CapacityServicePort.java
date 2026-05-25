package com.example.capacity.domain.api;

import com.example.capacity.domain.model.Capacity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CapacityServicePort {
    Mono<Capacity> registerCapacity(Capacity capacity);
    Flux<Capacity> getAllCapacities(int page, int size, String sortBy, String direction);
}
