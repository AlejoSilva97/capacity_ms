package com.example.capacity.domain.usecase;

import com.example.capacity.domain.enums.TechnicalMessage;
import com.example.capacity.domain.exceptions.BusinessException;
import com.example.capacity.domain.model.Capacity;
import com.example.capacity.domain.model.Technology;
import com.example.capacity.domain.spi.CapacityPersistencePort;
import com.example.capacity.domain.spi.TechnologyExternalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CapacityUseCaseTest {

    @Mock
    private CapacityPersistencePort capacityPersistencePort;

    @Mock
    private TechnologyExternalService technologyExternalService;

    @InjectMocks
    private CapacityUseCase capacityUseCase;

    @Nested
    @DisplayName("Tests for registerCapacity")
    class RegisterCapacityTests {

        @Test
        @DisplayName("Should throw BusinessException when technology IDs contain duplicates")
        void should_ThrowBusinessException_When_IdsAreDuplicated() {
            List<Technology> duplicatedTechs = List.of(
                    new Technology(1L, null),
                    new Technology(1L, null)
            );
            Capacity capacity = new Capacity(null, "Java Backend", "Description", duplicatedTechs);

            Mono<Capacity> result = capacityUseCase.registerCapacity(capacity);

            StepVerifier.create(result)
                    .expectErrorMatches(throwable -> throwable instanceof BusinessException
                            && ((BusinessException) throwable).getTechnicalMessage() == TechnicalMessage.INVALID_TECHNOLOGY_IDS) // Ajusta según el método getter de tu BusinessException
                    .verify();

            verifyNoInteractions(capacityPersistencePort);
            verifyNoInteractions(technologyExternalService);
        }

        @Test
        @DisplayName("Should throw BusinessException when technology IDs exceed the maximum limit of 20")
        void should_ThrowBusinessException_When_IdsExceedLimit() {
            List<Technology> limitExceededTechs = new ArrayList<>();
            for (long i = 1; i <= 21; i++) {
                limitExceededTechs.add(new Technology(i, null));
            }
            Capacity capacity = new Capacity(null, "Huge Capacity", "Description", limitExceededTechs);

            Mono<Capacity> result = capacityUseCase.registerCapacity(capacity);

            StepVerifier.create(result)
                    .expectError(BusinessException.class)
                    .verify();

            verifyNoInteractions(capacityPersistencePort);
        }

        @Test
        @DisplayName("Should throw BusinessException when capacity name already exists")
        void should_ThrowBusinessException_When_CapacityNameAlreadyExists() {
            List<Technology> validTechs = List.of(new Technology(1L, null));
            Capacity capacity = new Capacity(null, "Existing Capacity", "Description", validTechs);

            when(capacityPersistencePort.existByName(capacity.name())).thenReturn(Mono.just(true));

            Mono<Capacity> result = capacityUseCase.registerCapacity(capacity);

            StepVerifier.create(result)
                    .expectErrorMatches(t -> t instanceof BusinessException
                            && ((BusinessException) t).getTechnicalMessage() == TechnicalMessage.CAPACITY_ALREADY_EXISTS)
                    .verify();

            verify(capacityPersistencePort, times(1)).existByName(capacity.name());
            verify(capacityPersistencePort, never()).save(any());
            verifyNoInteractions(technologyExternalService);
        }

        @Test
        @DisplayName("Should register capacity successfully when all validations pass")
        void should_RegisterCapacitySuccessfully_When_ValidationsPass() {
            List<Long> expectedIds = List.of(1L, 2L);
            List<Technology> inputTechs = List.of(new Technology(1L, null), new Technology(2L, null));
            Capacity inputCapacity = new Capacity(null, "Cloud Architecture", "Description", inputTechs);
            Capacity savedCapacity = new Capacity(100L, "Cloud Architecture", "Description", inputTechs);

            when(capacityPersistencePort.existByName(inputCapacity.name())).thenReturn(Mono.just(false));
            when(technologyExternalService.verifyTechnologiesById(expectedIds)).thenReturn(Mono.empty());
            when(capacityPersistencePort.save(inputCapacity)).thenReturn(Mono.just(savedCapacity));

            Mono<Capacity> result = capacityUseCase.registerCapacity(inputCapacity);

            StepVerifier.create(result)
                    .expectNext(savedCapacity)
                    .verifyComplete();

            verify(capacityPersistencePort).existByName(inputCapacity.name());
            verify(technologyExternalService).verifyTechnologiesById(expectedIds);
            verify(capacityPersistencePort).save(inputCapacity);
        }
    }

    @Nested
    @DisplayName("Tests for getAllCapacities")
    class GetAllCapacitiesTests {

        @Test
        @DisplayName("Should return empty Flux when no capacities are found in persistence")
        void should_ReturnEmptyFlux_When_NoCapacitiesExist() {
            int page = 0;
            int size = 10;
            String sortBy = "name";
            String direction = "asc";

            when(capacityPersistencePort.findAll(page, size, sortBy, direction)).thenReturn(Flux.empty());

            Flux<Capacity> result = capacityUseCase.getAllCapacities(page, size, sortBy, direction);

            StepVerifier.create(result)
                    .expectNextCount(0)
                    .verifyComplete();

            verify(capacityPersistencePort).findAll(page, size, sortBy, direction);
            verifyNoInteractions(technologyExternalService);
        }

        @Test
        @DisplayName("Should return enriched capacities when records exist and microservice answers")
        void should_ReturnEnrichedCapacities_When_CapacitiesExist() {
            int page = 0;
            int size = 10;
            String sortBy = "name";
            String direction = "asc";

            List<Technology> rawTechsCap1 = List.of(new Technology(1L, null));
            List<Technology> rawTechsCap2 = List.of(new Technology(2L, null), new Technology(3L, null));

            Capacity cap1 = new Capacity(1L, "Cap 1", "Desc 1", rawTechsCap1);
            Capacity cap2 = new Capacity(2L, "Cap 2", "Desc 2", rawTechsCap2);

            Technology tech1 = new Technology(1L, "Java");
            Technology tech2 = new Technology(2L, "Spring Boot");
            Technology tech3 = new Technology(3L, "Docker");

            when(capacityPersistencePort.findAll(page, size, sortBy, direction))
                    .thenReturn(Flux.just(cap1, cap2));

            when(technologyExternalService.getTechnologiesByIds(List.of(1L, 2L, 3L)))
                    .thenReturn(Flux.just(tech1, tech2, tech3));

            Flux<Capacity> result = capacityUseCase.getAllCapacities(page, size, sortBy, direction);

            StepVerifier.create(result)
                    .assertNext(enrichedCap1 -> {
                        assertEquals(1L, enrichedCap1.id());
                        assertEquals("Java", enrichedCap1.technologies().get(0).name());
                    })
                    .assertNext(enrichedCap2 -> {
                        assertEquals(2L, enrichedCap2.id());
                        assertEquals("Spring Boot", enrichedCap2.technologies().get(0).name());
                        assertEquals("Docker", enrichedCap2.technologies().get(1).name());
                    })
                    .verifyComplete();

            verify(capacityPersistencePort).findAll(page, size, sortBy, direction);
            verify(technologyExternalService).getTechnologiesByIds(anyList());
        }

        @Test
        @DisplayName("Should fallback to 'Unknown' name when external service doesn't return a technology")
        void should_FallbackToUnknown_When_ExternalServiceIsMissingATechnology() {
            int page = 0;
            int size = 5;
            Capacity cap = new Capacity(1L, "Cap", "Desc", List.of(new Technology(99L, null)));

            when(capacityPersistencePort.findAll(page, size, "name", "asc")).thenReturn(Flux.just(cap));
            when(technologyExternalService.getTechnologiesByIds(List.of(99L))).thenReturn(Flux.empty());

            Flux<Capacity> result = capacityUseCase.getAllCapacities(page, size, "name", "asc");

            StepVerifier.create(result)
                    .assertNext(enrichedCap -> {
                        assertEquals(1L, enrichedCap.id());
                        assertEquals(1, enrichedCap.technologies().size());
                        assertEquals("Unknown", enrichedCap.technologies().get(0).name());
                    })
                    .verifyComplete();
        }
    }
}