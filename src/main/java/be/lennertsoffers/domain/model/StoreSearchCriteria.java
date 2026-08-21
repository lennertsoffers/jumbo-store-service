package be.lennertsoffers.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Describes a store search: the origin to measure from and any filters to apply.
 *
 * @param origin     the position to measure distances from; must not be null
 * @param openOnly   when {@code true}, only stores open at {@code searchTime} are considered
 * @param searchTime the instant used to evaluate {@code openOnly}; must not be null
 */
public record StoreSearchCriteria(
    Coordinates origin,
    boolean openOnly,
    Instant searchTime
) {

    public StoreSearchCriteria {
        Objects.requireNonNull(origin, "origin must not be null");
        Objects.requireNonNull(searchTime, "searchTime must not be null");
    }

}
