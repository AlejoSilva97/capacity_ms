package com.example.capacity.infrastructure.entrypoints.handler;

import com.example.capacity.domain.api.CapacityServicePort;
import com.example.capacity.domain.enums.TechnicalMessage;
import com.example.capacity.infrastructure.entrypoints.dto.CapacityDTO;
import com.example.capacity.infrastructure.entrypoints.dto.CapacityResponseDTO;
import com.example.capacity.infrastructure.entrypoints.mapper.CapacityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class CapacityHandlerImpl {

    private final CapacityServicePort capacityServicePort;
    private final CapacityMapper capacityMapper;

    public Mono<ServerResponse> createCapacity(ServerRequest request) {
        return request.bodyToMono(CapacityDTO.class)
                .flatMap(capacity -> capacityServicePort.registerCapacity(capacityMapper.capacityDTOToCapacity(capacity))
                        .doOnSuccess(savedCapacity -> log.info("Capacity created successfully"))
                )
                .flatMap(savedCapacity -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .bodyValue(TechnicalMessage.CAPACITY_CREATED.getMessage()));
    }

    public Mono<ServerResponse> getAllCapacities(ServerRequest request) {
        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(10);
        String sortBy = request.queryParam("sortBy").orElse("name");
        String direction = request.queryParam("direction").orElse("asc");

        return capacityServicePort.getAllCapacities(page, size, sortBy, direction)
                .map(capacityMapper::capacityToCapacityResponseDTO)
                .collectList()
                .flatMap(list -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(list));
    }
}
