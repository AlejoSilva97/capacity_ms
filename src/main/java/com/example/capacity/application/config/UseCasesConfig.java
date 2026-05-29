package com.example.capacity.application.config;

import com.example.capacity.domain.spi.CapacityPersistencePort;
import com.example.capacity.domain.spi.TechnologyExternalService;
import com.example.capacity.domain.usecase.CapacityUseCase;
import com.example.capacity.domain.api.CapacityServicePort;
import com.example.capacity.infrastructure.adapters.persistenceadapter.CapacityPersistenceAdapter;
import com.example.capacity.infrastructure.adapters.persistenceadapter.mapper.CapacityEntityMapper;
import com.example.capacity.infrastructure.adapters.persistenceadapter.repository.CapacityRepository;
import com.example.capacity.infrastructure.adapters.persistenceadapter.repository.CapacityTechnologyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;

@Configuration
@RequiredArgsConstructor
public class UseCasesConfig {
        private final CapacityRepository capacityRepository;
        private final CapacityTechnologyRepository capacityTechnologyRepository;
        private final CapacityEntityMapper capacityEntityMapper;
        private final DatabaseClient databaseClient;

        @Bean
        public CapacityPersistencePort capacitiesPersistencePort() {
                return new CapacityPersistenceAdapter(capacityRepository, capacityTechnologyRepository, capacityEntityMapper, databaseClient);
        }

        @Bean
        public CapacityServicePort capacitiesServicePort(CapacityPersistencePort capacityyPersistencePort, TechnologyExternalService technologyExternalService){
                return new CapacityUseCase(capacityyPersistencePort, technologyExternalService);
        }
}
