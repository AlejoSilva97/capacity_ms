package com.example.capacity.infrastructure.adapters.persistenceadapter.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "capacity_technology")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CapacityTechnologyEntity {
    @Id
    private Long id;
    @Column("id_capacity")
    private Long idCapacity;
    @Column("id_technology")
    private Long idTechnology;
}
