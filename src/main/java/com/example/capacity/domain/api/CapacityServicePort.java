package com.example.capacity.domain.api;

import com.example.capacity.domain.model.Capacity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CapacityServicePort {
    Mono<Capacity> registerCapacity(Capacity capacity);
    Flux<Capacity> getAllCapacities(int page, int size, String sortBy, String direction);
    Mono <Boolean> validateCapacitiesExist(List<Long> ids);
    Flux<Capacity> getCapacitiesByIds(List<Long> ids);
}
