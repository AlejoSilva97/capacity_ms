package com.example.capacity.domain.usecase;

import com.example.capacity.domain.enums.TechnicalMessage;
import com.example.capacity.domain.exceptions.BusinessException;
import com.example.capacity.domain.model.Capacity;
import com.example.capacity.domain.model.Technology;
import com.example.capacity.domain.spi.CapacityPersistencePort;
import com.example.capacity.domain.api.CapacityServicePort;
import com.example.capacity.domain.spi.TechnologyExternalService;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
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
            return Mono.error(new BusinessException(TechnicalMessage.INVALID_TECHNOLOGY_IDS));
        }
        return capacityPersistencePort.existByName(capacity.name())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new BusinessException(TechnicalMessage.CAPACITY_ALREADY_EXISTS));
                    }

                    return technologyExternalService.verifyTechnologiesById(ids)
                            .then(capacityPersistencePort.save(capacity));
                });
    }

    private static Boolean validateIds(List<Long> ids) {
        if (ids == null || ids.size() > 20) {
            return false;
        }
        Set<Long> uniqueIds = new HashSet<>(ids);
        return uniqueIds.size() == ids.size();
    }
}
