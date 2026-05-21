package com.example.capacity.infrastructure.adapters.persistenceadapter.repository;

import com.example.capacity.infrastructure.adapters.persistenceadapter.entity.CapacityEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;


@Repository
public interface CapacityRepository extends ReactiveCrudRepository<CapacityEntity, Long> {
    Mono<CapacityEntity> findByName(String name);
}
