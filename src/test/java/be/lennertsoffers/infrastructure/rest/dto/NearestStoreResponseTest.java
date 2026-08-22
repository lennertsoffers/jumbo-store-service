package be.lennertsoffers.infrastructure.rest.dto;

import be.lennertsoffers.domain.model.Address;
import be.lennertsoffers.domain.model.Coordinates;
import be.lennertsoffers.domain.model.OpeningHours;
import be.lennertsoffers.domain.model.Store;
import be.lennertsoffers.domain.model.StoreWithDistance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class NearestStoreResponseTest {

    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Brussels");

    private static Store store(OpeningHours openingHours) {
        return new Store(
            "uuid-1",
            "Jumbo Meir",
            new Address("Antwerp", "2000", "Meir", "1"),
            new Coordinates(51.2194, 4.4025),
            openingHours,
            ZONE_ID
        );
    }

    @Test
    @DisplayName("Should map all store and distance fields to the response when the store has opening hours")
    void fromDomain_shouldMapAllFields_whenStoreHasOpeningHours() {
        Store store = store(new OpeningHours(LocalTime.of(9, 0), LocalTime.of(18, 0)));
        StoreWithDistance storeWithDistance = new StoreWithDistance(store, 3.75);

        NearestStoreResponse response = NearestStoreResponse.fromDomain(storeWithDistance);

        assertThat(response.id()).isEqualTo("uuid-1");
        assertThat(response.name()).isEqualTo("Jumbo Meir");
        assertThat(response.city()).isEqualTo("Antwerp");
        assertThat(response.postalCode()).isEqualTo("2000");
        assertThat(response.street()).isEqualTo("Meir");
        assertThat(response.houseNumber()).isEqualTo("1");
        assertThat(response.latitude()).isEqualTo(51.2194);
        assertThat(response.longitude()).isEqualTo(4.4025);
        assertThat(response.todayOpen()).isEqualTo("09:00");
        assertThat(response.todayClose()).isEqualTo("18:00");
        assertThat(response.distanceInKm()).isEqualTo(3.75);
    }

    @Test
    @DisplayName("Should format single-digit hours and minutes with leading zeros")
    void fromDomain_shouldFormatTimesWithLeadingZeros_whenHoursOrMinutesAreSingleDigit() {
        Store store = store(new OpeningHours(LocalTime.of(9, 5), LocalTime.of(9, 30)));
        StoreWithDistance storeWithDistance = new StoreWithDistance(store, 1.0);

        NearestStoreResponse response = NearestStoreResponse.fromDomain(storeWithDistance);

        assertThat(response.todayOpen()).isEqualTo("09:05");
        assertThat(response.todayClose()).isEqualTo("09:30");
    }

    @Test
    @DisplayName("Should map todayOpen and todayClose to null when the store has no opening hours")
    void fromDomain_shouldMapToNull_whenStoreHasNoOpeningHours() {
        Store store = store(null);
        StoreWithDistance storeWithDistance = new StoreWithDistance(store, 5.0);

        NearestStoreResponse response = NearestStoreResponse.fromDomain(storeWithDistance);

        assertThat(response.todayOpen()).isNull();
        assertThat(response.todayClose()).isNull();
    }

    @Test
    @DisplayName("Should round the distance to metre precision (three decimals)")
    void fromDomain_shouldRoundDistanceToThreeDecimals() {
        Store store = store(new OpeningHours(LocalTime.of(9, 0), LocalTime.of(18, 0)));
        StoreWithDistance storeWithDistance = new StoreWithDistance(store, 1.2345678);

        NearestStoreResponse response = NearestStoreResponse.fromDomain(storeWithDistance);

        assertThat(response.distanceInKm()).isEqualTo(1.235);
    }

    @Test
    @DisplayName("Should carry over the exact distance provided by the domain object")
    void fromDomain_shouldMapDistance_whenDistanceIsZero() {
        Store store = store(new OpeningHours(LocalTime.of(9, 0), LocalTime.of(18, 0)));
        StoreWithDistance storeWithDistance = new StoreWithDistance(store, 0.0);

        NearestStoreResponse response = NearestStoreResponse.fromDomain(storeWithDistance);

        assertThat(response.distanceInKm()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should consider two responses with identical field values equal")
    void equals_shouldReturnTrue_whenAllFieldsAreIdentical() {
        NearestStoreResponse response1 = new NearestStoreResponse(
            "uuid-1", "Jumbo Meir", "Antwerp", "2000", "Meir", "1", 51.2194, 4.4025, "09:00", "18:00", 3.75
        );
        NearestStoreResponse response2 = new NearestStoreResponse(
            "uuid-1", "Jumbo Meir", "Antwerp", "2000", "Meir", "1", 51.2194, 4.4025, "09:00", "18:00", 3.75
        );

        assertThat(response1).isEqualTo(response2);
        assertThat(response1).hasSameHashCodeAs(response2);
    }

}
