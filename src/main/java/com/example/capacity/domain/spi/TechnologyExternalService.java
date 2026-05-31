package com.example.capacity.domain.spi;

import com.example.capacity.domain.model.Technology;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TechnologyExternalService {
    Mono<Void> verifyTechnologiesById(List<Long> ids);
    Flux<Technology> getTechnologiesByIds(List<Long> ids);
    Mono<Void> deleteTechnologiesByIds(List<Long> ids);
}
