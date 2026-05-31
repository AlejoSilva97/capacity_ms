package com.example.capacity.domain.api;

import com.example.capacity.domain.model.Capacity;
import com.example.capacity.domain.model.PaginationParams;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CapacityServicePort {
    Mono<Capacity> registerCapacity(Capacity capacity);
    Flux<Capacity> getAllCapacities(PaginationParams params);
    Mono <Boolean> validateCapacitiesExist(List<Long> ids);
    Flux<Capacity> getCapacitiesByIds(List<Long> ids);
    Mono<Void> deleteById(List<Long> ids);
}
