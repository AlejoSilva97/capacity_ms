package com.example.capacity.infrastructure.entrypoints.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class TechnologyResponseDTO {
    private Long id;
    private String name;
}
