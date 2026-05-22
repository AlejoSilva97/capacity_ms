package com.example.capacity.infrastructure.entrypoints.mapper;

import com.example.capacity.domain.model.Capacity;
import com.example.capacity.domain.model.Technology;
import com.example.capacity.infrastructure.entrypoints.dto.CapacityDTO;
import com.example.capacity.infrastructure.entrypoints.dto.CapacityResponseDTO;
import com.example.capacity.infrastructure.entrypoints.dto.TechnologyResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CapacityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "technologiesIds", target = "technologies", qualifiedByName = "idToTechnology")
    Capacity capacityDTOToCapacity(CapacityDTO capacityDTO);

    @Named("idToTechnology")
    default Technology mapIdToTechnology(Long id) {
        if (id == null) return null;
        return new Technology(id, null);
    }

    CapacityResponseDTO capacityToCapacityResponseDTO(Capacity capacity);

    TechnologyResponseDTO technologyToTechnologyResponseDTO(Technology technology);
}
