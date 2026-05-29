package com.example.capacity.infrastructure.adapters.persistenceadapter.repository;

import com.example.capacity.infrastructure.adapters.persistenceadapter.entity.CapacityEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;


@Repository
public interface CapacityRepository extends ReactiveCrudRepository<CapacityEntity, Long> {
    Mono<Boolean> existsByName(String name);
    Mono<Long> countByIdIn(Collection<Long> ids);
}
