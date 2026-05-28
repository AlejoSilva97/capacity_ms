package com.example.capacity.infrastructure.adapters.persistenceadapter.mapper;

import com.example.capacity.domain.model.Capacity;
import com.example.capacity.domain.model.Technology;
import com.example.capacity.infrastructure.adapters.persistenceadapter.entity.CapacityEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import io.r2dbc.spi.Readable;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CapacityEntityMapper {

    @Mapping(source = "id", target = "id")
    CapacityEntity toEntity(Capacity capacity);

    @Mapping(source = "entity.id", target = "id")
    @Mapping(source = "entity.name", target = "name")
    @Mapping(source = "entity.description", target = "description")
    @Mapping(source = "technologies", target = "technologies")
    Capacity toModel(CapacityEntity entity, List<Technology> technologies);

    default CapacityEntity rowToEntity(Readable readable) {
        return new CapacityEntity(
                readable.get("id", Long.class),
                readable.get("name", String.class),
                readable.get("description", String.class)
        );
    }
}
