package com.example.capacity.infrastructure.adapters.persistenceadapter;

import com.example.capacity.domain.model.Capacity;
import com.example.capacity.domain.model.Technology;
import com.example.capacity.domain.spi.CapacityPersistencePort;
import com.example.capacity.infrastructure.adapters.persistenceadapter.entity.CapacityEntity;
import com.example.capacity.infrastructure.adapters.persistenceadapter.entity.CapacityTechnologyEntity;
import com.example.capacity.infrastructure.adapters.persistenceadapter.mapper.CapacityEntityMapper;
import com.example.capacity.infrastructure.adapters.persistenceadapter.repository.CapacityRepository;
import com.example.capacity.infrastructure.adapters.persistenceadapter.repository.CapacityTechnologyRepository;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
                            .then(Mono.just(capacityEntityMapper.toModel(savedEntity, capacity.technologies())));
                });
    }

    @Override
    public Mono<Boolean> existByName(String name) {
        return capacityRepository.existsByName(name);
    }

    @Override
    public Flux<Capacity> findAll(int page, int size, String sortBy, String direction) {
        return resolveEntityFlux(page, size, sortBy, direction)
                .collectList()
                .filter(entities -> !entities.isEmpty())
                .flatMapMany(this::enrichEntitiesWithRelations)
                .switchIfEmpty(Flux.empty());
    }

    private Flux<CapacityEntity> resolveEntityFlux(int page, int size, String sortBy, String direction) {
        if ("technologies".equalsIgnoreCase(sortBy)) {
            int offset = page * size;
            return "desc".equalsIgnoreCase(direction)
                    ? capacityRepository.findAllSortedByTechCountDesc(size, offset)
                    : capacityRepository.findAllSortedByTechCountAsc(size, offset);
        }

        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, "name"));
        return capacityRepository.findAllBy(pageable);
    }

    private Flux<Capacity> enrichEntitiesWithRelations(List<CapacityEntity> entities) {
        List<Long> capacityIds = entities.stream().map(CapacityEntity::getId).toList();

        return capacityTechnologyRepository.findByIdCapacityIn(capacityIds)
                .collectList()
                .flatMapMany(relations -> mapEntitiesToDomain(entities, relations));
    }

    private Flux<Capacity> mapEntitiesToDomain(List<CapacityEntity> entities, List<CapacityTechnologyEntity> relations) {
        return Flux.fromIterable(entities)
                .map(entity -> {
                    List<Technology> techs = relations.stream()
                            .filter(r -> r.getIdCapacity().equals(entity.getId()))
                            .map(r -> new Technology(r.getIdTechnology(), null))
                            .toList();

                    return new Capacity(entity.getId(), entity.getName(), entity.getDescription(), techs);
                });
    }

    @Override
    public Mono<Long> countByIds(List<Long> ids) {
        return capacityRepository.countByIdIn(ids);
    }

    @Override
    public Flux<Capacity> findAllByIds(List<Long> ids) {
        return capacityRepository.findAllById(ids)
                .collectList()
                .filter(entities -> !entities.isEmpty())
                .flatMapMany(this::enrichEntitiesWithRelations)
                .switchIfEmpty(Flux.empty());
    }
}
