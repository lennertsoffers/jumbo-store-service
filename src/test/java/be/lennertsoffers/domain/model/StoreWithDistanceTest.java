package be.lennertsoffers.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoreWithDistanceTest {

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
    @DisplayName("Should create store with distance when distance is a positive finite number")
    void constructor_shouldCreateStoreWithDistance_whenDistanceIsPositive() {
        Store store = store("1");

        StoreWithDistance storeWithDistance = new StoreWithDistance(store, 12.5);

        assertThat(storeWithDistance.store()).isEqualTo(store);
        assertThat(storeWithDistance.distanceInKm()).isEqualTo(12.5);
    }

    @Test
    @DisplayName("Should create store with distance when distance is exactly zero")
    void constructor_shouldCreateStoreWithDistance_whenDistanceIsZero() {
        StoreWithDistance storeWithDistance = new StoreWithDistance(store("1"), 0.0);

        assertThat(storeWithDistance.distanceInKm()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should throw NullPointerException with descriptive message when store is null")
    void constructor_shouldThrowNullPointerException_whenStoreIsNull() {
        assertThatThrownBy(() -> new StoreWithDistance(null, 12.5))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("store must not be null");
    }

    @Test
    @DisplayName("Should be equal when store and distance both match")
    void equals_shouldReturnTrue_whenStoreAndDistanceAreEqual() {
        StoreWithDistance first = new StoreWithDistance(store("1"), 12.5);
        StoreWithDistance second = new StoreWithDistance(store("1"), 12.5);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when the underlying store differs")
    void equals_shouldReturnFalse_whenStoreDiffers() {
        StoreWithDistance original = new StoreWithDistance(store("1"), 12.5);
        StoreWithDistance other = new StoreWithDistance(store("2"), 12.5);

        assertThat(original).isNotEqualTo(other);
    }

    @Test
    @DisplayName("Should not be equal when the distance differs")
    void equals_shouldReturnFalse_whenDistanceDiffers() {
        StoreWithDistance original = new StoreWithDistance(store("1"), 12.5);
        StoreWithDistance other = new StoreWithDistance(store("1"), 20.0);

        assertThat(original).isNotEqualTo(other);
    }

}
