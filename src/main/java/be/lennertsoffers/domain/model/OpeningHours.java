package be.lennertsoffers.domain.model;

import java.time.LocalTime;
import java.util.Objects;

/**
 * The opening hours of a store for a single day, as a half-open interval {@code [opensAt, closesAt)}.
 *
 * @param opensAt  the time the store opens, inclusive; must not be null
 * @param closesAt the time the store closes, exclusive; must be strictly after {@code opensAt}
 */
public record OpeningHours(
    LocalTime opensAt,
    LocalTime closesAt
) {

    public OpeningHours {
        Objects.requireNonNull(opensAt, "opensAt must not be null");
        Objects.requireNonNull(closesAt, "closesAt must not be null");

        // TODO: throw InvalidOpeningHoursException when opensAt is not strictly before closesAt
    }

    /**
     * Checks whether the given time falls within these opening hours.
     *
     * @param time the time to check, must not be null
     * @return {@code true} if {@code time} is on or after {@link #opensAt()} and strictly before {@link #closesAt()}
     */
    public boolean isOpenAt(LocalTime time) {
        Objects.requireNonNull(time, "time must not be null");

        return !time.isBefore(opensAt) && time.isBefore(closesAt);
    }

}
