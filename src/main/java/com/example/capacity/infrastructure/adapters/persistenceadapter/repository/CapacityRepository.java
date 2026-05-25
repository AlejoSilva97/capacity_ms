package com.example.capacity.infrastructure.adapters.persistenceadapter.repository;

import com.example.capacity.infrastructure.adapters.persistenceadapter.entity.CapacityEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Repository
public interface CapacityRepository extends ReactiveCrudRepository<CapacityEntity, Long> {
    Mono<CapacityEntity> findByName(String name);
    Flux<CapacityEntity> findAllBy(Pageable pageable);

    @Query("SELECT c.* FROM capacities c " +
            "LEFT JOIN capacity_technology ct ON c.id = ct.id_capacity " +
            "GROUP BY c.id, c.name, c.description " +
            "ORDER BY COUNT(ct.id_technology) ASC " +
            "LIMIT :limit OFFSET :offset")
    Flux<CapacityEntity> findAllSortedByTechCountAsc(int limit, long offset);

    @Query("SELECT c.* FROM capacities c " +
            "LEFT JOIN capacity_technology ct ON c.id = ct.id_capacity " +
            "GROUP BY c.id, c.name, c.description " +
            "ORDER BY COUNT(ct.id_technology) DESC " +
            "LIMIT :limit OFFSET :offset")
    Flux<CapacityEntity> findAllSortedByTechCountDesc(int limit, long offset);
}
