package be.lennertsoffers.domain.model;

import java.util.Objects;

/**
 * A store paired with its distance to a search origin.
 *
 * @param store        the store; must not be null
 * @param distanceInKm the great-circle distance to the search origin in kilometres; must be finite and non-negative
 */
public record StoreWithDistance(
    Store store,
    double distanceInKm
) {

    public StoreWithDistance {
        Objects.requireNonNull(store, "store must not be null");

        // TODO: throw InvalidDistanceException when distanceInKm is not finite or negative
    }

}
