package com.example.capacity.domain.spi;

import reactor.core.publisher.Mono;

import java.util.List;

public interface TechnologyExternalService {
    Mono<Void> verifyTechnologiesById(List<Long> ids);
}
