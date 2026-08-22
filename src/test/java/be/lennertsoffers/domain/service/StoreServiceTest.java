package be.lennertsoffers.domain.service;

import be.lennertsoffers.domain.model.Address;
import be.lennertsoffers.domain.model.Coordinates;
import be.lennertsoffers.domain.model.OpeningHours;
import be.lennertsoffers.domain.model.Store;
import be.lennertsoffers.domain.model.StoreSearchCriteria;
import be.lennertsoffers.domain.model.StoreWithDistance;
import be.lennertsoffers.domain.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    private static final StoreSearchCriteria CRITERIA = new StoreSearchCriteria(
        new Coordinates(51.2194, 4.4025), false, Instant.parse("2024-01-01T10:00:00Z")
    );

    private static Store store(String id) {
        return new Store(
            id,
            "Jumbo " + id,
            new Address("Antwerp", "2000", "Meir", "1"),
            new Coordinates(51.2194, 4.4025),
            new OpeningHours(LocalTime.of(9, 0), LocalTime.of(18, 0)),
            ZoneId.of("Europe/Brussels")
        );
    }

    @Test
    @DisplayName("Should throw NullPointerException when the storeRepository is null")
    void constructor_shouldThrowNullPointerException_whenStoreRepositoryIsNull() {
        assertThatThrownBy(() -> new StoreService(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("storeRepository must not be null");
    }

    @Test
    @DisplayName("Should return the stores provided by the repository, in the given order")
    void findNearestStores_shouldReturnRepositoryResult_whenCriteriaAndLimitAreValid() {
        List<StoreWithDistance> expected = List.of(
            new StoreWithDistance(store("1"), 1.5),
            new StoreWithDistance(store("2"), 3.0)
        );
        when(storeRepository.findNearest(CRITERIA, 5)).thenReturn(expected);
        StoreService storeService = new StoreService(storeRepository);

        List<StoreWithDistance> result = storeService.findNearestStores(CRITERIA, 5);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should delegate to the repository with the exact criteria and limit received")
    void findNearestStores_shouldDelegateToRepository_withGivenCriteriaAndLimit() {
        when(storeRepository.findNearest(any(), anyInt())).thenReturn(List.of());
        StoreService storeService = new StoreService(storeRepository);

        storeService.findNearestStores(CRITERIA, 5);

        verify(storeRepository).findNearest(CRITERIA, 5);
    }

    @Test
    @DisplayName("Should return an empty list when the repository has no matching stores")
    void findNearestStores_shouldReturnEmptyList_whenRepositoryReturnsNoStores() {
        when(storeRepository.findNearest(CRITERIA, 5)).thenReturn(List.of());
        StoreService storeService = new StoreService(storeRepository);

        List<StoreWithDistance> result = storeService.findNearestStores(CRITERIA, 5);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should throw NullPointerException and never call the repository when criteria is null")
    void findNearestStores_shouldThrowNullPointerException_whenCriteriaIsNull() {
        StoreService storeService = new StoreService(storeRepository);

        assertThatThrownBy(() -> storeService.findNearestStores(null, 5))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("criteria must not be null");

        verify(storeRepository, never()).findNearest(any(), anyInt());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    @DisplayName("Should throw IllegalArgumentException and never call the repository when limit is not positive")
    void findNearestStores_shouldThrowIllegalArgumentException_whenLimitIsNotPositive(int limit) {
        StoreService storeService = new StoreService(storeRepository);

        assertThatThrownBy(() -> storeService.findNearestStores(CRITERIA, limit))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("limit must be positive but was " + limit);

        verify(storeRepository, never()).findNearest(any(), anyInt());
    }

}
