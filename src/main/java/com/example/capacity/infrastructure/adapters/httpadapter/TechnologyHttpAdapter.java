package com.example.capacity.infrastructure.adapters.httpadapter;

import com.example.capacity.domain.constants.Constants;
import com.example.capacity.domain.exceptions.TechnologyNotFoundException;
import com.example.capacity.domain.model.Technology;
import com.example.capacity.domain.spi.TechnologyExternalService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class TechnologyHttpAdapter implements TechnologyExternalService {

    private final WebClient technologyWebClient;

    public TechnologyHttpAdapter(WebClient technologyWebClient) {
        this.technologyWebClient = technologyWebClient;
    }


    @Override
    public Mono<Void> verifyTechnologiesById(List<Long> ids) {
        return Flux.fromIterable(ids)
                .flatMap(id -> technologyWebClient.get()
                        .uri("/technologies/{id}", id)
                        .retrieve()
                        .bodyToMono(Object.class)
                        .onErrorResume(e -> Mono.error(
                                new TechnologyNotFoundException(String.format(Constants.TECHNOLOGY_NOT_FOUND, id))
                            ))
                )
                .then();
    }

    @Override
    public Flux<Technology> getTechnologiesByIds(List<Long> ids) {
        return Flux.fromIterable(ids)
                .flatMap(id -> technologyWebClient.get()
                        .uri("/technologies/{id}", id)
                        .retrieve()
                        .bodyToMono(Technology.class)
                        .onErrorResume(e -> Mono.empty())
                );
    }
}
