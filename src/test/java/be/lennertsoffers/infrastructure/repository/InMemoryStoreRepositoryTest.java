package be.lennertsoffers.infrastructure.repository;

import be.lennertsoffers.domain.model.Address;
import be.lennertsoffers.domain.model.Coordinates;
import be.lennertsoffers.domain.model.OpeningHours;
import be.lennertsoffers.domain.model.Store;
import be.lennertsoffers.domain.model.StoreSearchCriteria;
import be.lennertsoffers.domain.model.StoreWithDistance;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InMemoryStoreRepositoryTest {

    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Brussels");
    private static final Coordinates ORIGIN = new Coordinates(51.2194, 4.4025);

    @Mock
    private JsonStoreDataLoader jsonStoreDataLoader;

    private static Store store(String id, Coordinates coordinates) {
        return store(id, coordinates, new OpeningHours(LocalTime.of(9, 0), LocalTime.of(18, 0)));
    }

    private static Store store(String id, Coordinates coordinates, OpeningHours openingHours) {
        return new Store(
            id,
            "Jumbo " + id,
            new Address("Antwerp", "2000", "Meir", "1"),
            coordinates,
            openingHours,
            ZONE_ID
        );
    }

    private static StoreSearchCriteria criteria(boolean openOnly, Instant searchTime) {
        return new StoreSearchCriteria(ORIGIN, openOnly, searchTime);
    }

    @Test
    @DisplayName("Should return an empty list when no stores were loaded")
    void findNearest_shouldReturnEmptyList_whenNoStoresAreLoaded() {
        when(jsonStoreDataLoader.getStores()).thenReturn(List.of());
        InMemoryStoreRepository repository = new InMemoryStoreRepository(jsonStoreDataLoader);

        List<StoreWithDistance> result = repository.findNearest(criteria(false, Instant.parse("2024-01-01T10:00:00Z")), 5);

        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    @DisplayName("Should return an empty list without inspecting stores when limit is not positive")
    void findNearest_shouldReturnEmptyList_whenLimitIsNotPositive(int limit) {
        when(jsonStoreDataLoader.getStores()).thenReturn(List.of(store("1", ORIGIN)));
        InMemoryStoreRepository repository = new InMemoryStoreRepository(jsonStoreDataLoader);

        List<StoreWithDistance> result = repository.findNearest(criteria(false, Instant.parse("2024-01-01T10:00:00Z")), limit);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return stores ordered from nearest to farthest from the origin")
    void findNearest_shouldReturnStoresSortedByDistance_whenStoresAreAvailable() {
        Store nearStore = store("near", new Coordinates(51.2200, 4.4030));
        Store midStore = store("mid", new Coordinates(51.3000, 4.5000));
        Store farStore = store("far", new Coordinates(52.0000, 5.0000));
        when(jsonStoreDataLoader.getStores()).thenReturn(List.of(farStore, nearStore, midStore));
        InMemoryStoreRepository repository = new InMemoryStoreRepository(jsonStoreDataLoader);

        List<StoreWithDistance> result = repository.findNearest(criteria(false, Instant.parse("2024-01-01T10:00:00Z")), 5);

        assertThat(result)
            .extracting(StoreWithDistance::store)
            .containsExactly(nearStore, midStore, farStore);
        assertThat(result).isSortedAccordingTo(java.util.Comparator.comparingDouble(StoreWithDistance::distanceInKm));
    }

    @Test
    @DisplayName("Should limit the number of returned stores to the given limit")
    void findNearest_shouldLimitResults_whenMoreStoresThanLimitAreAvailable() {
        Store store1 = store("1", new Coordinates(51.2200, 4.4030));
        Store store2 = store("2", new Coordinates(51.3000, 4.5000));
        Store store3 = store("3", new Coordinates(52.0000, 5.0000));
        when(jsonStoreDataLoader.getStores()).thenReturn(List.of(store1, store2, store3));
        InMemoryStoreRepository repository = new InMemoryStoreRepository(jsonStoreDataLoader);

        List<StoreWithDistance> result = repository.findNearest(criteria(false, Instant.parse("2024-01-01T10:00:00Z")), 2);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should include all stores regardless of opening hours when openOnly is false")
    void findNearest_shouldIncludeClosedStores_whenOpenOnlyIsFalse() {
        Instant midnight = Instant.parse("2024-01-01T23:30:00Z");
        Store openStore = store("open", ORIGIN, new OpeningHours(LocalTime.of(9, 0), LocalTime.of(18, 0)));
        Store closedStore = store("closed", ORIGIN, null);
        when(jsonStoreDataLoader.getStores()).thenReturn(List.of(openStore, closedStore));
        InMemoryStoreRepository repository = new InMemoryStoreRepository(jsonStoreDataLoader);

        List<StoreWithDistance> result = repository.findNearest(criteria(false, midnight), 5);

        assertThat(result)
            .extracting(StoreWithDistance::store)
            .containsExactlyInAnyOrder(openStore, closedStore);
    }

    @Test
    @DisplayName("Should exclude closed stores when openOnly is true")
    void findNearest_shouldExcludeClosedStores_whenOpenOnlyIsTrue() {
        Instant searchTime = Instant.parse("2024-01-01T09:30:00Z").atZone(ZONE_ID).toInstant();
        Store openStore = store("open", ORIGIN, new OpeningHours(LocalTime.of(9, 0), LocalTime.of(18, 0)));
        Store closedByHoursStore = store("closed-by-hours", ORIGIN, new OpeningHours(LocalTime.of(20, 0), LocalTime.of(23, 0)));
        Store noOpeningHoursStore = store("no-opening-hours", ORIGIN, null);
        when(jsonStoreDataLoader.getStores()).thenReturn(List.of(openStore, closedByHoursStore, noOpeningHoursStore));
        InMemoryStoreRepository repository = new InMemoryStoreRepository(jsonStoreDataLoader);

        List<StoreWithDistance> result = repository.findNearest(criteria(true, searchTime), 5);

        assertThat(result)
            .extracting(StoreWithDistance::store)
            .containsExactly(openStore);
    }

    @Test
    @DisplayName("Should compute the correct haversine distance for each returned store")
    void findNearest_shouldComputeCorrectDistance_whenStoreCoordinatesAreKnown() {
        Coordinates ghentCoordinates = new Coordinates(51.0543, 3.7174);
        Store store = store("ghent", ghentCoordinates);
        when(jsonStoreDataLoader.getStores()).thenReturn(List.of(store));
        InMemoryStoreRepository repository = new InMemoryStoreRepository(jsonStoreDataLoader);

        List<StoreWithDistance> result = repository.findNearest(criteria(false, Instant.parse("2024-01-01T10:00:00Z")), 5);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().distanceInKm()).isEqualTo(ORIGIN.distanceTo(ghentCoordinates));
    }

    @Test
    @DisplayName("Should read the store list from the loader only once during construction")
    void constructor_shouldReadStoresFromLoader_onlyOnce() {
        when(jsonStoreDataLoader.getStores()).thenReturn(List.of(store("1", ORIGIN)));

        new InMemoryStoreRepository(jsonStoreDataLoader);

        org.mockito.Mockito.verify(jsonStoreDataLoader, org.mockito.Mockito.times(1)).getStores();
    }

}
