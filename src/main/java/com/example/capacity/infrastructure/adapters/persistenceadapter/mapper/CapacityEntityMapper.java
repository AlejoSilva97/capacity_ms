package com.example.capacity.infrastructure.adapters.persistenceadapter.mapper;

import com.example.capacity.domain.model.Capacity;
import com.example.capacity.infrastructure.adapters.persistenceadapter.entity.CapacityEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CapacityEntityMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    Capacity toModel(CapacityEntity entity);
    CapacityEntity toEntity(Capacity capacity);
}
