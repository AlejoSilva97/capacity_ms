package com.example.capacity.infrastructure.adapters.persistenceadapter.repository;

import com.example.capacity.infrastructure.adapters.persistenceadapter.entity.CapacityTechnologyEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CapacityTechnologyRepository extends ReactiveCrudRepository<CapacityTechnologyEntity, Long> {
}
