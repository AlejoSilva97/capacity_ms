package com.example.capacity.domain.spi;

import com.example.capacity.domain.model.Capacity;
import com.example.capacity.domain.model.PaginationParams;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CapacityPersistencePort {
    Mono<Capacity> save(Capacity capacity);
    Mono<Boolean> existByName(String name);
    Flux<Capacity> findAll(PaginationParams params);
    Mono<Long> countByIds(List<Long> ids);
    Flux<Capacity> findAllByIds(List<Long> ids);
}
