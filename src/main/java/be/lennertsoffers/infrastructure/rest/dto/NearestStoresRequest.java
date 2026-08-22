package be.lennertsoffers.infrastructure.rest.dto;

import be.lennertsoffers.domain.model.Coordinates;
import be.lennertsoffers.domain.model.StoreSearchCriteria;
import jakarta.validation.constraints.*;

import java.time.Clock;
import java.time.Instant;

/**
 * Query parameters for the nearest-stores endpoint, validated at the transport boundary.
 *
 * <p>{@code limit} and {@code open} are optional and default to {@code 5} and {@code false} respectively.
 *
 * @param latitude  latitude of the search origin, {@code -90..90}
 * @param longitude longitude of the search origin, {@code -180..180}
 * @param limit     maximum number of stores to return, {@code 1..50}; defaults to {@code 5}
 * @param open      when {@code true}, only currently open stores are returned; defaults to {@code false}
 */
public record NearestStoresRequest(
    @NotNull(message = "latitude is required")
    @DecimalMin(value = "-90.0", message = "latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "latitude must be between -90 and 90")
    Double latitude,

    @NotNull(message = "longitude is required")
    @DecimalMin(value = "-180.0", message = "longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "longitude must be between -180 and 180")
    Double longitude,

    @Positive(message = "limit must be a positive number")
    @Max(value = 50, message = "limit must not exceed 50")
    Integer limit,

    Boolean open
) {

    private static final int DEFAULT_LIMIT = 5;

    public NearestStoresRequest {
        if (limit == null) {
            limit = DEFAULT_LIMIT;
        }

        if (open == null) {
            open = false;
        }
    }

    /**
     * Builds the domain {@link StoreSearchCriteria} for this request, using the given clock as the search time.
     *
     * @param clock the clock supplying "now" for the {@code open} filter; must not be null
     * @return the search criteria
     */
    public StoreSearchCriteria toCriteria(Clock clock) {
        Coordinates origin = new Coordinates(latitude, longitude);
        Instant now = clock.instant();

        return new StoreSearchCriteria(origin, open, now);
    }

}
