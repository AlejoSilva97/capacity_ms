package com.example.capacity.infrastructure.adapters.persistenceadapter;

import com.example.capacity.domain.model.Capacity;
import com.example.capacity.domain.model.Technology;
import com.example.capacity.domain.spi.CapacityPersistencePort;
import com.example.capacity.infrastructure.adapters.persistenceadapter.entity.CapacityTechnologyEntity;
import com.example.capacity.infrastructure.adapters.persistenceadapter.mapper.CapacityEntityMapper;
import com.example.capacity.infrastructure.adapters.persistenceadapter.repository.CapacityRepository;
import com.example.capacity.infrastructure.adapters.persistenceadapter.repository.CapacityTechnologyRepository;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@AllArgsConstructor
public class CapacityPersistenceAdapter implements CapacityPersistencePort {
    private final CapacityRepository capacityRepository;
    private final CapacityTechnologyRepository capacityTechnologyRepository;
    private final CapacityEntityMapper capacityEntityMapper;

    @Override
    public Mono<Capacity> save(Capacity capacity) {
        return capacityRepository.save(capacityEntityMapper.toEntity(capacity))
                .flatMap(savedEntity -> {
                    List<CapacityTechnologyEntity> capacityTechnologies = capacity.technologies().stream()
                            .map(techs -> new CapacityTechnologyEntity(null, savedEntity.getId(), techs.id()))
                            .toList();
                    return capacityTechnologyRepository.saveAll(capacityTechnologies)
                            .then(Mono.just(capacityEntityMapper.toModel(savedEntity)));
                });
    }

    @Override
    public Mono<Boolean> existByName(String name) {
        return capacityRepository.findByName(name)
                .map(capacityEntityMapper::toModel)
                .map(cap -> true)
                .defaultIfEmpty(false);
    }
}
