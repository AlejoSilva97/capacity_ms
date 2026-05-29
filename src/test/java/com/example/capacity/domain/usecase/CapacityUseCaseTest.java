package com.example.capacity.domain.usecase;

import com.example.capacity.domain.constants.Constants;
import com.example.capacity.domain.exceptions.CapacityAlreadyExistsException;
import com.example.capacity.domain.exceptions.InvalidFieldException;
import com.example.capacity.domain.exceptions.TechnologyNotFoundException;
import com.example.capacity.domain.model.Capacity;
import com.example.capacity.domain.model.PaginationParams;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
        @DisplayName("Should register capacity successfully when fields and rules are valid")
        void registerCapacity_Success() {
            // Arrange
            List<Technology> technologies = List.of(
                    new Technology(1L, null),
                    new Technology(2L, null),
                    new Technology(3L, null)
            );
            Capacity inputCapacity = new Capacity(null, "Backend Developer", "Java, Spring and reactive apps", technologies);
            Capacity savedCapacity = new Capacity(1L, "Backend Developer", "Java, Spring and reactive apps", technologies);

            when(capacityPersistencePort.existByName(inputCapacity.name())).thenReturn(Mono.just(false));
            when(technologyExternalService.verifyTechnologiesById(any())).thenReturn(Mono.empty());
            when(capacityPersistencePort.save(inputCapacity)).thenReturn(Mono.just(savedCapacity));

            // Act & Assert
            StepVerifier.create(capacityUseCase.registerCapacity(inputCapacity))
                    .expectNext(savedCapacity)
                    .verifyComplete();

            verify(capacityPersistencePort).existByName(inputCapacity.name());
            verify(technologyExternalService).verifyTechnologiesById(any());
            verify(capacityPersistencePort).save(inputCapacity);
        }

        @Test
        @DisplayName("Should throw InvalidFieldException when technology ids are duplicated")
        void registerCapacity_ThrowsInvalidFieldException_WhenDuplicateTechnologies() {
            // Arrange
            List<Technology> duplicatedTechnologies = List.of(
                    new Technology(1L, null),
                    new Technology(1L, null),
                    new Technology(2L, null)
            );
            Capacity capacity = new Capacity(null, "Frontend Developer", "Web development", duplicatedTechnologies);

            // Act & Assert
            StepVerifier.create(capacityUseCase.registerCapacity(capacity))
                    .expectErrorMatches(throwable -> throwable instanceof InvalidFieldException
                            && throwable.getMessage().equals(Constants.DUPLICATE_TECHNOLOGIES_NOT_ALLOWED))
                    .verify();

            verifyNoInteractions(capacityPersistencePort);
            verifyNoInteractions(technologyExternalService);
        }

        @Test
        @DisplayName("Should throw CapacityAlreadyExistsException when capacity name already exists")
        void registerCapacity_ThrowsCapacityAlreadyExistsException_WhenNameExists() {
            // Arrange
            List<Technology> technologies = List.of(
                    new Technology(1L, null),
                    new Technology(2L, null),
                    new Technology(3L, null)
            );
            Capacity capacity = new Capacity(null, "DevOps", "Cloud infrastructure architecture", technologies);
            String expectedErrorMessage = String.format(Constants.CAPACITY_ALREADY_EXISTS, capacity.name());

            when(capacityPersistencePort.existByName(capacity.name())).thenReturn(Mono.just(true));

            // Act & Assert
            StepVerifier.create(capacityUseCase.registerCapacity(capacity))
                    .expectErrorMatches(throwable -> throwable instanceof CapacityAlreadyExistsException
                            && throwable.getMessage().equals(expectedErrorMessage))
                    .verify();

            verify(capacityPersistencePort).existByName(capacity.name());
            verifyNoMoreInteractions(capacityPersistencePort);
            verifyNoInteractions(technologyExternalService);
        }

    }

    @Nested
    @DisplayName("Tests for getAllCapacities")
    class GetAllCapacitiesTests {

        @Test
        @DisplayName("Should return enriched capacities flux when persistence port contains data")
        void getAllCapacities_Success_WithEnrichment() {
            // Arrange
            PaginationParams params = new PaginationParams(0, 10, "name", "ASC");

            List<Technology> initialTechs1 = List.of(new Technology(1L, null), new Technology(2L, null), new Technology(3L, null));
            List<Technology> initialTechs2 = List.of(new Technology(2L, null), new Technology(3L, null), new Technology(4L, null));

            Capacity capacity1 = new Capacity(1L, "Java Tech", "Description 1", initialTechs1);
            Capacity capacity2 = new Capacity(2L, "Web Tech", "Description 2", initialTechs2);

            Technology fullTech1 = new Technology(1L, "Java");
            Technology fullTech2 = new Technology(2L, "Spring");
            Technology fullTech3 = new Technology(3L, "Reactor");
            Technology fullTech4 = new Technology(4L, "Angular");

            when(capacityPersistencePort.findAll(params)).thenReturn(Flux.just(capacity1, capacity2));
            when(technologyExternalService.getTechnologiesByIds(List.of(1L, 2L, 3L, 4L)))
                    .thenReturn(Flux.just(fullTech1, fullTech2, fullTech3, fullTech4));

            // Act & Assert
            StepVerifier.create(capacityUseCase.getAllCapacities(params))
                    .assertNext(cap -> {
                        // Verifica la primera capacidad enriquecida
                        assert cap.id().equals(1L);
                        assert cap.technologies().get(0).name().equals("Java");
                        assert cap.technologies().get(1).name().equals("Spring");
                        assert cap.technologies().get(2).name().equals("Reactor");
                    })
                    .assertNext(cap -> {
                        // Verifica la segunda capacidad enriquecida
                        assert cap.id().equals(2L);
                        assert cap.technologies().get(0).name().equals("Spring");
                        assert cap.technologies().get(1).name().equals("Reactor");
                        assert cap.technologies().get(2).name().equals("Angular");
                    })
                    .verifyComplete();

            verify(capacityPersistencePort).findAll(params);
            verify(technologyExternalService).getTechnologiesByIds(anyList());
        }

        @Test
        @DisplayName("Should return empty flux when database returns no results")
        void getAllCapacities_ReturnsEmptyFlux_WhenNoResults() {
            // Arrange
            PaginationParams params = new PaginationParams(0, 10, "name", "ASC");
            when(capacityPersistencePort.findAll(params)).thenReturn(Flux.empty());

            // Act & Assert
            StepVerifier.create(capacityUseCase.getAllCapacities(params))
                    .verifyComplete(); // El switchIfEmpty(Flux.empty()) asegura un cierre limpio sin emitir nada

            verify(capacityPersistencePort).findAll(params);
            verifyNoInteractions(technologyExternalService);
        }
    }
}