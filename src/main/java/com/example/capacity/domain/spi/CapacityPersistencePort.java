package com.example.capacity.domain.spi;

import com.example.capacity.domain.model.Capacity;
import reactor.core.publisher.Mono;

public interface CapacityPersistencePort {
    Mono<Capacity> save(Capacity user);
    Mono<Boolean> existByName(String name);
}
