package be.lennertsoffers.infrastructure.rest.dto;

import be.lennertsoffers.domain.model.OpeningHours;
import be.lennertsoffers.domain.model.Store;
import be.lennertsoffers.domain.model.StoreWithDistance;

import java.time.format.DateTimeFormatter;

/**
 * API response for a single nearby store.
 *
 * <p>{@code todayOpen}/{@code todayClose} are formatted as {@code HH:mm}, or {@code null} when the store has no
 * opening hours today. {@code distanceInKm} is the great-circle distance rounded to metre precision.
 */
public record NearestStoreResponse(
    String id,
    String name,
    String city,
    String postalCode,
    String street,
    String houseNumber,
    double latitude,
    double longitude,
    String todayOpen,
    String todayClose,
    double distanceInKm
) {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Maps a domain {@link StoreWithDistance} to its API representation.
     *
     * @param storeWithDistance the ranked store; must not be null
     * @return the response DTO
     */
    public static NearestStoreResponse fromDomain(StoreWithDistance storeWithDistance) {
        Store store = storeWithDistance.store();

        String todayOpen = store.getOpeningHours()
            .map(OpeningHours::opensAt)
            .map(TIME_FORMATTER::format)
            .orElse(null);

        String todayClose = store.getOpeningHours()
            .map(OpeningHours::closesAt)
            .map(TIME_FORMATTER::format)
            .orElse(null);

        return new NearestStoreResponse(
            store.getId(),
            store.getName(),
            store.getAddress().city(),
            store.getAddress().postalCode(),
            store.getAddress().street(),
            store.getAddress().houseNumber(),
            store.getCoordinates().latitude(),
            store.getCoordinates().longitude(),
            todayOpen,
            todayClose,
            roundToMeters(storeWithDistance.distanceInKm())
        );
    }

    private static double roundToMeters(double distanceInKm) {
        return Math.round(distanceInKm * 1_000.0) / 1_000.0;
    }

}
