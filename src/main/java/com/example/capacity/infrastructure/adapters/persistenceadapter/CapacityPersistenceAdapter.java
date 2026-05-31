package com.example.capacity.infrastructure.adapters.persistenceadapter;

import com.example.capacity.domain.model.Capacity;
import com.example.capacity.domain.model.PaginationParams;
import com.example.capacity.domain.model.Technology;
import com.example.capacity.domain.spi.CapacityPersistencePort;
import com.example.capacity.infrastructure.adapters.persistenceadapter.constants.DatabaseConstants;
import com.example.capacity.infrastructure.adapters.persistenceadapter.entity.CapacityEntity;
import com.example.capacity.infrastructure.adapters.persistenceadapter.entity.CapacityTechnologyEntity;
import com.example.capacity.infrastructure.adapters.persistenceadapter.mapper.CapacityEntityMapper;
import com.example.capacity.infrastructure.adapters.persistenceadapter.repository.CapacityRepository;
import com.example.capacity.infrastructure.adapters.persistenceadapter.repository.CapacityTechnologyRepository;
import lombok.AllArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@AllArgsConstructor
public class CapacityPersistenceAdapter implements CapacityPersistencePort {
    private final CapacityRepository capacityRepository;
    private final CapacityTechnologyRepository capacityTechnologyRepository;
    private final CapacityEntityMapper capacityEntityMapper;
    private final DatabaseClient databaseClient;

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
    public Flux<Capacity> findAll(PaginationParams params) {
        final String query = buildQuery(params);

        return databaseClient.sql(query)
                .bind(DatabaseConstants.SIZE, params.size())
                .bind(DatabaseConstants.OFFSET, (long) params.page() * params.size())
                .map(capacityEntityMapper::rowToEntity)
                .all()
                .collectList()
                .filter(entities -> !entities.isEmpty())
                .flatMapMany(this::enrichEntitiesWithRelations)
                .switchIfEmpty(Flux.empty());
    }

    private static String buildQuery(PaginationParams params) {
        String sortColumn = DatabaseConstants.SORT_BY_TECHNOLOGIES.equalsIgnoreCase(params.sortBy())
                ? DatabaseConstants.SORT_BY_COUNT
                : DatabaseConstants.SORT_BY_NAME;

        String sortOrder = DatabaseConstants.ASC.equalsIgnoreCase(params.direction())
                ? DatabaseConstants.ASC
                : DatabaseConstants.DESC;

        return String.format(DatabaseConstants.QUERY, sortColumn, sortOrder);
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

    @Override
    public Flux<Long> findOrphanTechnologyIds(List<Long> ids) {
        return capacityTechnologyRepository.findOrphanTechnologyIds(ids);
    }

    @Override
    @Transactional
    public Mono<Void> deleteAllByIds(List<Long> ids) {
        return capacityTechnologyRepository.deleteByIdCapacityIn(ids)
                .then(capacityRepository.deleteAllById(ids));
    }
}
