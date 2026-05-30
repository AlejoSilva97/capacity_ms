package com.example.capacity.infrastructure.entrypoints.handler;

import com.example.capacity.domain.api.CapacityServicePort;
import com.example.capacity.domain.constants.Constants;
import com.example.capacity.domain.exceptions.InvalidFieldException;
import com.example.capacity.domain.model.PaginationParams;
import com.example.capacity.infrastructure.entrypoints.dto.CapacityDTO;
import com.example.capacity.infrastructure.entrypoints.mapper.CapacityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CapacityHandlerImpl {

    private final CapacityServicePort capacityServicePort;
    private final CapacityMapper capacityMapper;

    public Mono<ServerResponse> createCapacity(ServerRequest request) {
        return request.bodyToMono(CapacityDTO.class)
                .flatMap(capacityDTO -> capacityServicePort.registerCapacity(capacityMapper.capacityDTOToCapacity(capacityDTO))
                        .doOnSuccess(savedCapacity -> log.info(Constants.CAPACITY_CREATED_SUCCESS))
                )
                .flatMap(savedCapacity -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .bodyValue(Constants.CAPACITY_CREATED));
    }

    public Mono<ServerResponse> getAllCapacities(ServerRequest request) {
        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(10);
        String sortBy = request.queryParam("sortBy").orElse("name");
        String direction = request.queryParam("direction").orElse("DESC");

        PaginationParams params = new PaginationParams(page,size, sortBy, direction);

        return capacityServicePort.getAllCapacities(params)
                .map(capacityMapper::capacityToCapacityResponseDTO)
                .collectList()
                .flatMap(list -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(list));
    }

    public Mono<ServerResponse> validateExistence(ServerRequest request) {
        return extractAndParseIds(request)
                .flatMap(capacityServicePort::validateCapacitiesExist)
                .flatMap(exists -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(exists));
    }

    private Mono<List<Long>> extractAndParseIds(ServerRequest request) {
        return Mono.justOrEmpty(request.queryParams().get("ids"))
                .filter(idsParam -> !idsParam.isEmpty() && !idsParam.get(0).isBlank())
                .switchIfEmpty(Mono.error(new InvalidFieldException(Constants.IDS_PARAMS_REQUIRED)))
                .map(idsParam -> idsParam.stream()
                        .flatMap(s -> java.util.Arrays.stream(s.split(",")))
                        .map(String::trim)
                        .map(Long::valueOf)
                        .toList())
                .onErrorMap(NumberFormatException.class, e ->
                        new InvalidFieldException(Constants.IDS_FORMAT_INVALID));
    }

    public Mono<ServerResponse> getCapacitiesByIds(ServerRequest request) {
        return extractAndParseIds(request)
                .flatMapMany(capacityServicePort::getCapacitiesByIds)
                .map(capacityMapper::capacityToCapacityResponseDTO)
                .collectList()
                .flatMap(list -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(list));
    }
}
