package com.example.capacity.infrastructure.entrypoints.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {
    private String code;
    private String message;
}

