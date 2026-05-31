package com.example.capacity.infrastructure.adapters.persistenceadapter.repository;

import com.example.capacity.infrastructure.adapters.persistenceadapter.entity.CapacityTechnologyEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;

public interface CapacityTechnologyRepository extends ReactiveCrudRepository<CapacityTechnologyEntity, Long> {
    Flux<CapacityTechnologyEntity> findByIdCapacityIn(List<Long> capacityIds);

    Mono<Void> deleteByIdCapacityIn(List<Long> capacityIds);

    @Query("SELECT id_technology FROM capacity_technology " +
            "WHERE id_capacity IN (:ids) " +
            "AND id_technology NOT IN (" +
            "    SELECT id_technology FROM capacity_technology WHERE id_capacity NOT IN (:ids)" +
            ")")
    Flux<Long> findOrphanTechnologyIds(List<Long> ids);
}