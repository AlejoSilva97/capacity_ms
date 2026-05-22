package com.example.capacity.infrastructure.entrypoints.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class CapacityResponseDTO {
    private Long id;
    private String name;
    private String description;
    private List<TechnologyResponseDTO> technologies;
}
