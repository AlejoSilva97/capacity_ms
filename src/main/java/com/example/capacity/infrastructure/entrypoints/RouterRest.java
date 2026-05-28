package com.example.capacity.infrastructure.entrypoints;

import com.example.capacity.infrastructure.entrypoints.dto.CapacityDTO;
import com.example.capacity.infrastructure.entrypoints.handler.CapacityHandlerImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/capacities",
                    method = RequestMethod.POST,
                    beanClass = CapacityHandlerImpl.class,
                    beanMethod = "createCapacity",
                    operation = @Operation(
                            summary = "Registrar una nueva capacidad y sus tecnologias asociadas",
                            description = "Valida y almacena una capacidad si no existe previamente.",
                            operationId = "createCapacity",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = CapacityDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "201",
                                            description = "Capacity created successfully",
                                            content = @Content(schema = @Schema(implementation = String.class))
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Validation or business error")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(CapacityHandlerImpl capacityHandler) {
        return route(POST("/capacities"), capacityHandler::createCapacity)
                .andRoute(GET("/capacities"), capacityHandler::getAllCapacities)
                .andRoute(GET("/capacities/validate-existence"), capacityHandler::validateExistence)
                .andRoute(GET("/capacities/batch"), capacityHandler::getCapacitiesByIds);
    }
}