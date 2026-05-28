package com.example.capacity.infrastructure.entrypoints.handler;

import com.example.capacity.domain.constants.Constants;
import com.example.capacity.domain.enums.TechnicalMessage;
import com.example.capacity.domain.exceptions.*;
import com.example.capacity.infrastructure.entrypoints.dto.ErrorResponseDTO;
import com.example.capacity.infrastructure.entrypoints.util.APIResponse;
import com.example.capacity.infrastructure.entrypoints.util.ErrorDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Component
@Order(-2)
@Slf4j
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalExceptionHandler(ErrorAttributes errorAttributes,
                                  WebProperties webProperties,
                                  ApplicationContext applicationContext,
                                  ServerCodecConfigurer configurer) {
        super(errorAttributes, webProperties.getResources(), applicationContext);
        this.setMessageWriters(configurer.getWriters());
        this.setMessageReaders(configurer.getReaders());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable error = getError(request);

        if (error instanceof InvalidFieldException invalidFieldException) {
            log.warn("Business rule violation: {}", invalidFieldException.getMessage());
            return buildErrorDTO(HttpStatus.BAD_REQUEST, Constants.INVALID_FIELD_CODE, invalidFieldException.getMessage());
        }

        if (error instanceof CapacityAlreadyExistsException capacityExistsException) {
            log.warn("Business rule violation: {}", capacityExistsException.getMessage());
            return buildErrorDTO(HttpStatus.CONFLICT, Constants.CAPACITY_ALREADY_EXISTS_CODE, capacityExistsException.getMessage());
        }

        if (error instanceof TechnologyNotFoundException technologyNotFoundException) {
            log.warn("Business rule violation: {}", technologyNotFoundException.getMessage());
            return buildErrorDTO(HttpStatus.NOT_FOUND, Constants.TECHNOLOGY_NOT_FOUND_CODE, technologyNotFoundException.getMessage());
        }

        if (error instanceof BusinessException businessEx) {
            log.warn("Business rule violation: {}", businessEx.getMessage());
            return buildResponse(HttpStatus.BAD_REQUEST, TechnicalMessage.INVALID_PARAMETERS,
                    List.of(mapToErrorDTO(businessEx.getTechnicalMessage())));
        }

        if (error instanceof TechnicalException technicalEx) {
            log.error("Technical exception occurred: ", technicalEx);
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, TechnicalMessage.INTERNAL_ERROR,
                    List.of(mapToErrorDTO(technicalEx.getTechnicalMessage())));
        }

        log.error("Unexpected system error: ", error);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, TechnicalMessage.INTERNAL_ERROR,
                List.of(ErrorDTO.builder()
                        .code(TechnicalMessage.INTERNAL_ERROR.getCode())
                        .message(TechnicalMessage.INTERNAL_ERROR.getMessage())
                        .build()));
    }

    private ErrorDTO mapToErrorDTO(TechnicalMessage message) {
        return ErrorDTO.builder()
                .code(message.getCode())
                .message(message.getMessage())
                .param(message.getParam())
                .build();
    }

    private Mono<ServerResponse> buildResponse(HttpStatus httpStatus, TechnicalMessage error, List<ErrorDTO> errors) {
        APIResponse apiErrorResponse = APIResponse.builder()
                .code(error.getCode())
                .message(error.getMessage())
                .date(Instant.now().toString())
                .errors(errors)
                .build();

        return ServerResponse.status(httpStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(apiErrorResponse);
    }

    private Mono<ServerResponse> buildErrorDTO(HttpStatus status, String code, String message) {
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .code(code)
                .message(message)
                .build();

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(errorResponse);
    }
}