package com.example.capacity.infrastructure.adapters.persistenceadapter.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "capacities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CapacityEntity {
    @Id
    private Long id;
    private String name;
    private String description;
}
