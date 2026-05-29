package com.example.capacity.domain.usecase;

import com.example.capacity.domain.constants.Constants;
import com.example.capacity.domain.enums.TechnicalMessage;
import com.example.capacity.domain.exceptions.BusinessException;
import com.example.capacity.domain.exceptions.CapacityAlreadyExistsException;
import com.example.capacity.domain.exceptions.InvalidFieldException;
import com.example.capacity.domain.model.Capacity;
import com.example.capacity.domain.model.PaginationParams;
import com.example.capacity.domain.model.Technology;
import com.example.capacity.domain.spi.CapacityPersistencePort;
import com.example.capacity.domain.api.CapacityServicePort;
import com.example.capacity.domain.spi.TechnologyExternalService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CapacityUseCase implements CapacityServicePort {

    private final CapacityPersistencePort capacityPersistencePort;
    private final TechnologyExternalService technologyExternalService;

    public CapacityUseCase(CapacityPersistencePort capacityPersistencePort, TechnologyExternalService technologyExternalService) {
        this.capacityPersistencePort = capacityPersistencePort;
        this.technologyExternalService = technologyExternalService;
    }

    @Override
    public Mono<Capacity> registerCapacity(Capacity capacity) {
        List<Long> ids = capacity.technologies().stream()
                .map(Technology::id)
                .toList();
        if (!validateIds(ids)) {
            return Mono.error(new InvalidFieldException(Constants.DUPLICATE_TECHNOLOGIES_NOT_ALLOWED));
        }
        return capacityPersistencePort.existByName(capacity.name())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new CapacityAlreadyExistsException(String.format(Constants.CAPACITY_ALREADY_EXISTS, capacity.name())));
                    }

                    return technologyExternalService.verifyTechnologiesById(ids)
                            .then(Mono.defer(() -> capacityPersistencePort.save(capacity)));
                });
    }

    private static Boolean validateIds(List<Long> ids) {
        Set<Long> uniqueIds = new HashSet<>(ids);
        return uniqueIds.size() == ids.size();
    }

    @Override
    public Flux<Capacity> getAllCapacities(PaginationParams params) {
        return capacityPersistencePort.findAll(params)
                .collectList()
                .filter(capacities -> !capacities.isEmpty())
                .flatMapMany(this::enrichCapacitiesWithTechnologies)
                .switchIfEmpty(Flux.empty());
    }

    private Flux<Capacity> enrichCapacitiesWithTechnologies(List<Capacity> capacities) {
        List<Long> techIds = getTechIds(capacities);

        if (techIds.isEmpty()) {
            return Flux.fromIterable(capacities);
        }

        return technologyExternalService.getTechnologiesByIds(techIds)
                .collectMap(Technology::id, tech -> tech)
                .flatMapMany(techMap -> enrichCapacityList(capacities, techMap));
    }

    private Flux<Capacity> enrichCapacityList(List<Capacity> capacities, Map<Long, Technology> techMap) {
        return Flux.fromIterable(capacities)
                .map(capacity -> {
                    List<Technology> enrichedTechs = capacity.technologies().stream()
                            .map(t -> techMap.getOrDefault(t.id(), new Technology(t.id(), "Unknown")))
                            .toList();

                    return new Capacity(capacity.id(), capacity.name(), capacity.description(), enrichedTechs);
                });
    }

    private List<Long> getTechIds(List<Capacity> capacities) {
        return capacities.stream()
                .flatMap(c -> c.technologies().stream())
                .map(Technology::id)
                .distinct()
                .toList();
    }

    @Override
    public Mono<Boolean> validateCapacitiesExist(List<Long> ids) {
        if (org.springframework.util.CollectionUtils.isEmpty(ids)) {
            return Mono.just(false);
        }

        List<Long> uniqueIds = ids.stream().distinct().toList();

        return validateSize(uniqueIds)
                .then(capacityPersistencePort.countByIds(uniqueIds))
                .map(count -> count == uniqueIds.size());
    }

    private Mono<Void> validateSize(List<Long> uniqueIds) {
        return uniqueIds.size() > 4
                ? Mono.error(new BusinessException(TechnicalMessage.INVALID_PARAMETERS))
                : Mono.empty();
    }

    @Override
    public Flux<Capacity> getCapacitiesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty() || ids.size() > 4) {
            return Flux.error(new BusinessException(TechnicalMessage.INVALID_PARAMETERS));
        }
        List<Long> uniqueIds = ids.stream().distinct().toList();
//        return capacityPersistencePort.findAllByIds(uniqueIds)
//                .collectList()
//                .filter(capacities -> !capacities.isEmpty())
//                .flatMapMany(this::enrichCapacitiesWithTechnologies)
//                .switchIfEmpty(Flux.empty());
        return Flux.empty();
    }
}
