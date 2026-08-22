package be.lennertsoffers.infrastructure.repository.dto;

import be.lennertsoffers.domain.exception.InvalidCoordinatesException;
import be.lennertsoffers.domain.model.OpeningHours;
import be.lennertsoffers.domain.model.Store;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoreDtoTest {

    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Brussels");

    private static StoreDto dto(String uuid, String latitude, String longitude, String todayOpen, String todayClose) {
        return new StoreDto(
            uuid,
            "Jumbo Meir",
            "Antwerp",
            "2000",
            "Meir",
            "1",
            null,
            longitude,
            latitude,
            todayOpen,
            todayClose,
            "123",
            "SAP123",
            "supermarket",
            false,
            true
        );
    }

    private static StoreDto validDto() {
        return dto("uuid-1", "51.2194", "4.4025", "09:00", "18:00");
    }

    @Test
    @DisplayName("Should create the DTO and strip whitespace from optional string fields")
    void constructor_shouldStripWhitespace_whenOptionalFieldsHaveSurroundingSpaces() {
        StoreDto storeDto = new StoreDto(
            "uuid-1",
            " Jumbo Meir ",
            " Antwerp ",
            " 2000 ",
            " Meir ",
            " 1 ",
            null,
            " 4.4025 ",
            " 51.2194 ",
            " 09:00 ",
            " 18:00 ",
            " 123 ",
            "SAP123",
            "supermarket",
            false,
            true
        );

        assertThat(storeDto.addressName()).isEqualTo("Jumbo Meir");
        assertThat(storeDto.city()).isEqualTo("Antwerp");
        assertThat(storeDto.postalCode()).isEqualTo("2000");
        assertThat(storeDto.street()).isEqualTo("Meir");
        assertThat(storeDto.street2()).isEqualTo("1");
        assertThat(storeDto.street3()).isNull();
        assertThat(storeDto.longitude()).isEqualTo("4.4025");
        assertThat(storeDto.latitude()).isEqualTo("51.2194");
        assertThat(storeDto.todayOpen()).isEqualTo("09:00");
        assertThat(storeDto.todayClose()).isEqualTo("18:00");
        assertThat(storeDto.complexNumber()).isEqualTo("123");
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {" ", "\t"})
    @DisplayName("Should throw IllegalArgumentException when uuid is null, empty or blank")
    void constructor_shouldThrowIllegalArgumentException_whenUuidIsBlank(String uuid) {
        assertThatThrownBy(() -> dto(uuid, "51.2194", "4.4025", "09:00", "18:00"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Store UUID must not be null or blank");
    }

    @Test
    @DisplayName("Should map all fields to the corresponding domain Store")
    void toDomain_shouldMapAllFields_whenDtoIsValid() {
        Store store = validDto().toDomain(ZONE_ID);

        assertThat(store.getId()).isEqualTo("uuid-1");
        assertThat(store.getName()).isEqualTo("Jumbo Meir");
        assertThat(store.getAddress().city()).isEqualTo("Antwerp");
        assertThat(store.getAddress().postalCode()).isEqualTo("2000");
        assertThat(store.getAddress().street()).isEqualTo("Meir");
        assertThat(store.getAddress().houseNumber()).isEqualTo("1");
        assertThat(store.getCoordinates().latitude()).isEqualTo(51.2194);
        assertThat(store.getCoordinates().longitude()).isEqualTo(4.4025);
        assertThat(store.getOpeningHours()).contains(new OpeningHours(LocalTime.of(9, 0), LocalTime.of(18, 0)));
        assertThat(store.getZoneId()).isEqualTo(ZONE_ID);
    }

    @ParameterizedTest
    @CsvSource({
        "09:00, 18:00",
        "9:00, 18:00"
    })
    @DisplayName("Should parse opening and closing times in all supported time formats")
    void toDomain_shouldParseOpeningHours_whenTimesAreInSupportedFormats(String todayOpen, String todayClose) {
        Store store = dto("uuid-1", "51.2194", "4.4025", todayOpen, todayClose).toDomain(ZONE_ID);

        assertThat(store.getOpeningHours()).contains(new OpeningHours(LocalTime.of(9, 0), LocalTime.of(18, 0)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"gesloten", "Gesloten", "GESLOTEN"})
    @DisplayName("Should map to an absent OpeningHours when todayOpen indicates the store is closed")
    void toDomain_shouldReturnEmptyOpeningHours_whenTodayOpenIsClosed(String closedMarker) {
        Store store = dto("uuid-1", "51.2194", "4.4025", closedMarker, "18:00").toDomain(ZONE_ID);

        assertThat(store.getOpeningHours()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"gesloten", "Gesloten", "GESLOTEN"})
    @DisplayName("Should map to an absent OpeningHours when todayClose indicates the store is closed")
    void toDomain_shouldReturnEmptyOpeningHours_whenTodayCloseIsClosed(String closedMarker) {
        Store store = dto("uuid-1", "51.2194", "4.4025", "09:00", closedMarker).toDomain(ZONE_ID);

        assertThat(store.getOpeningHours()).isEmpty();
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {" "})
    @DisplayName("Should map to an absent OpeningHours when todayOpen is missing or blank while keeping the store")
    void toDomain_shouldReturnEmptyOpeningHours_whenTodayOpenIsMissing(String todayOpen) {
        Store store = dto("uuid-1", "51.2194", "4.4025", todayOpen, "18:00").toDomain(ZONE_ID);

        assertThat(store.getOpeningHours()).isEmpty();
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {" "})
    @DisplayName("Should map to an absent OpeningHours when todayClose is missing or blank while keeping the store")
    void toDomain_shouldReturnEmptyOpeningHours_whenTodayCloseIsMissing(String todayClose) {
        Store store = dto("uuid-1", "51.2194", "4.4025", "09:00", todayClose).toDomain(ZONE_ID);

        assertThat(store.getOpeningHours()).isEmpty();
    }

    @Test
    @DisplayName("Should map to an absent OpeningHours when todayOpen has an unparseable format while keeping the store")
    void toDomain_shouldReturnEmptyOpeningHours_whenTodayOpenIsUnparseable() {
        Store store = dto("uuid-1", "51.2194", "4.4025", "not-a-time", "18:00").toDomain(ZONE_ID);

        assertThat(store.getOpeningHours()).isEmpty();
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {" "})
    @DisplayName("Should throw IllegalArgumentException when latitude is missing or blank")
    void toDomain_shouldThrowIllegalArgumentException_whenLatitudeIsMissing(String latitude) {
        StoreDto storeDto = dto("uuid-1", latitude, "4.4025", "09:00", "18:00");

        assertThatThrownBy(() -> storeDto.toDomain(ZONE_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("latitude is missing or blank");
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {" "})
    @DisplayName("Should throw IllegalArgumentException when longitude is missing or blank")
    void toDomain_shouldThrowIllegalArgumentException_whenLongitudeIsMissing(String longitude) {
        StoreDto storeDto = dto("uuid-1", "51.2194", longitude, "09:00", "18:00");

        assertThatThrownBy(() -> storeDto.toDomain(ZONE_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("longitude is missing or blank");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when latitude is not a valid number")
    void toDomain_shouldThrowIllegalArgumentException_whenLatitudeIsNotANumber() {
        StoreDto storeDto = dto("uuid-1", "not-a-number", "4.4025", "09:00", "18:00");

        assertThatThrownBy(() -> storeDto.toDomain(ZONE_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid latitude format 'not-a-number'")
            .hasCauseInstanceOf(NumberFormatException.class);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when longitude is not a valid number")
    void toDomain_shouldThrowIllegalArgumentException_whenLongitudeIsNotANumber() {
        StoreDto storeDto = dto("uuid-1", "51.2194", "not-a-number", "09:00", "18:00");

        assertThatThrownBy(() -> storeDto.toDomain(ZONE_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid longitude format 'not-a-number'")
            .hasCauseInstanceOf(NumberFormatException.class);
    }

    @Test
    @DisplayName("Should throw InvalidCoordinatesException when latitude is out of range")
    void toDomain_shouldThrowInvalidCoordinatesException_whenLatitudeIsOutOfRange() {
        StoreDto storeDto = dto("uuid-1", "1000", "4.4025", "09:00", "18:00");

        assertThatThrownBy(() -> storeDto.toDomain(ZONE_ID))
            .isInstanceOf(InvalidCoordinatesException.class);
    }

}
