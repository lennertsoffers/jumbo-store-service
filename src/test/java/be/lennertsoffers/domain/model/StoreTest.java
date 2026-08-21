package be.lennertsoffers.domain.model;

import be.lennertsoffers.domain.exception.InvalidStoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoreTest {

    private static final Address ADDRESS = new Address("Antwerp", "2000", "Meir", "1");
    private static final Coordinates COORDINATES = new Coordinates(51.2194, 4.4025);
    private static final OpeningHours OPENING_HOURS = new OpeningHours(LocalTime.of(9, 0), LocalTime.of(18, 0));
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Brussels");

    @Test
    @DisplayName("Should create store and expose all provided values through getters")
    void constructor_shouldCreateStore_whenAllFieldsAreValid() {
        Store store = new Store("1", "Jumbo Meir", ADDRESS, COORDINATES, OPENING_HOURS, ZONE_ID);

        assertThat(store.getId()).isEqualTo("1");
        assertThat(store.getName()).isEqualTo("Jumbo Meir");
        assertThat(store.getAddress()).isEqualTo(ADDRESS);
        assertThat(store.getCoordinates()).isEqualTo(COORDINATES);
        assertThat(store.getOpeningHours()).contains(OPENING_HOURS);
        assertThat(store.getZoneId()).isEqualTo(ZONE_ID);
    }

    @Test
    @DisplayName("Should create store when openingHours is null")
    void constructor_shouldCreateStore_whenOpeningHoursIsNull() {
        Store store = new Store("1", "Jumbo Meir", ADDRESS, COORDINATES, null, ZONE_ID);

        assertThat(store.getOpeningHours()).isEqualTo(Optional.empty());
    }

    @Test
    @DisplayName("Should strip surrounding whitespace from the name")
    void constructor_shouldStripWhitespace_whenNameHasSurroundingSpaces() {
        Store store = new Store("1", " Jumbo Meir ", ADDRESS, COORDINATES, OPENING_HOURS, ZONE_ID);

        assertThat(store.getName()).isEqualTo("Jumbo Meir");
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "\t"})
    @DisplayName("Should throw InvalidStoreException when id is empty or blank")
    void constructor_shouldThrowInvalidStoreException_whenIdIsBlank(String id) {
        assertThatThrownBy(() -> new Store(id, "Jumbo Meir", ADDRESS, COORDINATES, OPENING_HOURS, ZONE_ID))
            .isInstanceOf(InvalidStoreException.class)
            .hasMessage("id must not be blank");
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "\t"})
    @DisplayName("Should throw InvalidStoreException when name is empty or blank")
    void constructor_shouldThrowInvalidStoreException_whenNameIsBlank(String name) {
        assertThatThrownBy(() -> new Store("1", name, ADDRESS, COORDINATES, OPENING_HOURS, ZONE_ID))
            .isInstanceOf(InvalidStoreException.class)
            .hasMessage("name must not be blank");
    }

    @Test
    @DisplayName("Should throw NullPointerException with descriptive message when id is null")
    void constructor_shouldThrowNullPointerException_whenIdIsNull() {
        assertThatThrownBy(() -> new Store(null, "Jumbo Meir", ADDRESS, COORDINATES, OPENING_HOURS, ZONE_ID))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("id must not be null");
    }

    @Test
    @DisplayName("Should throw NullPointerException with descriptive message when name is null")
    void constructor_shouldThrowNullPointerException_whenNameIsNull() {
        assertThatThrownBy(() -> new Store("1", null, ADDRESS, COORDINATES, OPENING_HOURS, ZONE_ID))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("name must not be null");
    }

    @Test
    @DisplayName("Should throw NullPointerException with descriptive message when address is null")
    void constructor_shouldThrowNullPointerException_whenAddressIsNull() {
        assertThatThrownBy(() -> new Store("1", "Jumbo Meir", null, COORDINATES, OPENING_HOURS, ZONE_ID))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("address must not be null");
    }

    @Test
    @DisplayName("Should throw NullPointerException with descriptive message when coordinates is null")
    void constructor_shouldThrowNullPointerException_whenCoordinatesIsNull() {
        assertThatThrownBy(() -> new Store("1", "Jumbo Meir", ADDRESS, null, OPENING_HOURS, ZONE_ID))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("coordinates must not be null");
    }

    @Test
    @DisplayName("Should throw NullPointerException with descriptive message when zoneId is null")
    void constructor_shouldThrowNullPointerException_whenZoneIdIsNull() {
        assertThatThrownBy(() -> new Store("1", "Jumbo Meir", ADDRESS, COORDINATES, OPENING_HOURS, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("zoneId must not be null");
    }

    @Test
    @DisplayName("Should return false when checking if open and openingHours is absent")
    void isOpenAt_shouldReturnFalse_whenOpeningHoursIsNull() {
        Store store = new Store("1", "Jumbo Meir", ADDRESS, COORDINATES, null, ZONE_ID);

        boolean isOpen = store.isOpenAt(Instant.parse("2024-01-01T10:00:00Z"));

        assertThat(isOpen).isFalse();
    }

    @Test
    @DisplayName("Should return true when the instant falls within opening hours in the store's own time zone")
    void isOpenAt_shouldReturnTrue_whenInstantIsWithinOpeningHoursInStoreZone() {
        Store store = new Store("1", "Jumbo Meir", ADDRESS, COORDINATES, OPENING_HOURS, ZONE_ID);

        // 10:00 in Europe/Brussels (UTC+1 in January) corresponds to 09:00 UTC
        boolean isOpen = store.isOpenAt(Instant.parse("2024-01-01T09:00:00Z"));

        assertThat(isOpen).isTrue();
    }

    @Test
    @DisplayName("Should return false when the instant falls outside opening hours in the store's own time zone")
    void isOpenAt_shouldReturnFalse_whenInstantIsOutsideOpeningHoursInStoreZone() {
        Store store = new Store("1", "Jumbo Meir", ADDRESS, COORDINATES, OPENING_HOURS, ZONE_ID);

        // 07:00 in Europe/Brussels (UTC+1 in January) corresponds to 06:00 UTC, before opening
        boolean isOpen = store.isOpenAt(Instant.parse("2024-01-01T06:00:00Z"));

        assertThat(isOpen).isFalse();
    }

    @Test
    @DisplayName("Should use the store's own time zone rather than another zone's local time")
    void isOpenAt_shouldUseStoreZoneId_whenConvertingInstantToLocalTime() {
        // 09:30 UTC is 10:30 in Europe/Brussels (open) but only 04:30 in America/New_York (closed)
        Store brusselsStore = new Store("1", "Jumbo Meir", ADDRESS, COORDINATES, OPENING_HOURS, ZoneId.of("Europe/Brussels"));
        Store newYorkStore = new Store("2", "Jumbo NYC", ADDRESS, COORDINATES, OPENING_HOURS, ZoneId.of("America/New_York"));
        Instant instant = Instant.parse("2024-01-01T09:30:00Z");

        assertThat(brusselsStore.isOpenAt(instant)).isTrue();
        assertThat(newYorkStore.isOpenAt(instant)).isFalse();
    }

    @Test
    @DisplayName("Should be equal when ids match, regardless of other fields")
    void equals_shouldReturnTrue_whenIdsAreEqual() {
        Store first = new Store("1", "Jumbo Meir", ADDRESS, COORDINATES, OPENING_HOURS, ZONE_ID);
        Store second = new Store("1", "Different Name", new Address("Ghent", "9000", "Veldstraat", "2"), new Coordinates(0, 0), null, ZoneId.of("UTC"));

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when ids differ, even if other fields match")
    void equals_shouldReturnFalse_whenIdsDiffer() {
        Store first = new Store("1", "Jumbo Meir", ADDRESS, COORDINATES, OPENING_HOURS, ZONE_ID);
        Store second = new Store("2", "Jumbo Meir", ADDRESS, COORDINATES, OPENING_HOURS, ZONE_ID);

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("Should not be equal when compared to a non-Store object")
    void equals_shouldReturnFalse_whenComparedToDifferentType() {
        Store store = new Store("1", "Jumbo Meir", ADDRESS, COORDINATES, OPENING_HOURS, ZONE_ID);

        assertThat(store).isNotEqualTo("1");
    }

}
