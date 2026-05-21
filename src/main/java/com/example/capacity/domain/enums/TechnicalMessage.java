package com.example.capacity.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TechnicalMessage {

    INTERNAL_ERROR("500","Something went wrong, please try again", ""),
    INVALID_REQUEST("400", "Bad Request, please verify data", ""),
    INVALID_PARAMETERS(INVALID_REQUEST.getCode(), "Bad Parameters, please verify data", ""),
    CAPACITY_CREATED("201", "Capacity created successfully", ""),
    CAPACITY_ALREADY_EXISTS("400","La capacidad ya esta registrada" ,"" ),
    TECHNOLOGY_NOT_EXISTS("400","La tecnologia con el id %s no se encuentra registrada" ,"" ),
    INVALID_TECHNOLOGY_IDS("400","Error en la lista de tecnologias" ,"" );

    private final String code;
    private final String message;
    private final String param;
}