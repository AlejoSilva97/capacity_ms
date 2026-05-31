package com.example.capacity.infrastructure.entrypoints;

import com.example.capacity.infrastructure.entrypoints.dto.CapacityDTO;
import com.example.capacity.infrastructure.entrypoints.dto.CapacityResponseDTO;
import com.example.capacity.infrastructure.entrypoints.handler.CapacityHandlerImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
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
            ),
            @RouterOperation(
                    path = "/capacities",
                    method = RequestMethod.GET,
                    beanClass = CapacityHandlerImpl.class,
                    beanMethod = "getAllCapacities",
                    operation = @Operation(
                            summary = "Listar capacidades con paginación y ordenamiento dinámico",
                            description = "Retorna un flujo paginado de capacidades enriquecidas con sus tecnologías. Permite ordenar alfabéticamente por 'name' o por la cantidad de tecnologías usando 'technologies'.",
                            operationId = "getAllCapacities",
                            parameters = {
                                    @Parameter(name = "page", in = ParameterIn.QUERY, description = "Número de la página a consultar (basado en índice 0)", schema = @Schema(type = "integer", defaultValue = "0")),
                                    @Parameter(name = "size", in = ParameterIn.QUERY, description = "Cantidad máxima de registros por página", schema = @Schema(type = "integer", defaultValue = "10")),
                                    @Parameter(name = "sortBy", in = ParameterIn.QUERY, description = "Campo de ordenamiento ('name' o 'technologies')", schema = @Schema(type = "string", defaultValue = "name")),
                                    @Parameter(name = "direction", in = ParameterIn.QUERY, description = "Sentido del ordenamiento ('ASC' o 'DESC')", schema = @Schema(type = "string", defaultValue = "DESC"))
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Lista de capacidades paginada y ordenada obtenida con éxito",
                                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CapacityResponseDTO.class)))
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Parámetros de consulta inválidos o con formato incorrecto")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/capacities/exists",
                    method = RequestMethod.GET,
                    beanClass = CapacityHandlerImpl.class,
                    beanMethod = "validateExistence",
                    operation = @Operation(
                            summary = "Verificar la existencia de múltiples capacidades por ID",
                            description = "Valida si un conjunto de identificadores de capacidad pasados por parámetros existen en su totalidad dentro del sistema.",
                            operationId = "validateExistence",
                            parameters = {
                                    @Parameter(name = "ids", in = ParameterIn.QUERY, description = "Lista de IDs separados por comas (ej. 1,2,3)", required = true, schema = @Schema(type = "string"))
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Validación ejecutada con éxito (retorna true si todas existen, false en caso contrario)",
                                            content = @Content(schema = @Schema(implementation = Boolean.class))
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Parámetro 'ids' ausente o con formato inválido")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/capacities/bulk",
                    method = RequestMethod.GET,
                    beanClass = CapacityHandlerImpl.class,
                    beanMethod = "getCapacitiesByIds",
                    operation = @Operation(
                            summary = "Obtener múltiples capacidades por lote de IDs",
                            description = "Retorna una lista de capacidades enriquecidas con sus tecnologías a partir de una lista de IDs. Permite un máximo de 4 IDs por consulta.",
                            operationId = "getCapacitiesByIds",
                            parameters = {
                                    @Parameter(name = "ids", in = ParameterIn.QUERY, description = "Lista de IDs separados por comas (ej. 1,2,3)", required = true, schema = @Schema(type = "string"))
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Lista de capacidades obtenida con éxito",
                                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CapacityResponseDTO.class)))
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Parámetro 'ids' ausente, con formato incorrecto o si excede el límite de 4 elementos"),
                                    @ApiResponse(responseCode = "404", description = "No se encontraron coincidencias para ninguna de las capacidades solicitadas")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/capacities",
                    method = RequestMethod.DELETE,
                    beanClass = CapacityHandlerImpl.class,
                    beanMethod = "deleteCapacities",
                    operation = @Operation(
                            summary = "Eliminar capacidades por lote y sus tecnologías huérfanas",
                            description = "Elimina las capacidades especificadas y limpia de forma transaccional las tecnologías asociadas si no pertenecen a ningún otro bootcamp o capacidad.",
                            operationId = "deleteCapacities",
                            parameters = {
                                    @Parameter(name = "ids", in = ParameterIn.QUERY, description = "Lista de IDs de capacidades separados por comas (ej. 1,2,3)", required = true, schema = @Schema(type = "string"))
                            },
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "Capacidades y tecnologías huérfanas eliminadas con éxito"),
                                    @ApiResponse(responseCode = "400", description = "Parámetro 'ids' ausente o con formato inválido"),
                                    @ApiResponse(responseCode = "404", description = "No se encontraron capacidades coincidentes con los IDs provistos")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(CapacityHandlerImpl capacityHandler) {
        return route(POST("/capacities"), capacityHandler::createCapacity)
                .andRoute(GET("/capacities"), capacityHandler::getAllCapacities)
                .andRoute(GET("/capacities/exists"), capacityHandler::validateExistence)
                .andRoute(GET("/capacities/bulk"), capacityHandler::getCapacitiesByIds)
                .andRoute(DELETE("/capacities"), capacityHandler::deleteCapacities);
    }
}