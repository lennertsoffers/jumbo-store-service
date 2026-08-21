package be.lennertsoffers.domain.model;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;

/**
 * A store.
 *
 * <p>Identity is defined solely by {@link #getId() id}: two {@code Store} instances with the same id are
 * considered equal regardless of their other attributes. Opening hours are optional and absent when the
 * store has no published hours for the current day.
 */
public final class Store {

    private final String id;
    private final String name;
    private final Address address;
    private final Coordinates coordinates;
    private final OpeningHours openingHours;
    private final ZoneId zoneId;

    public Store(String id, String name, Address address, Coordinates coordinates, OpeningHours openingHours, ZoneId zoneId) {
        this.id = requireNonBlank(id, "id");
        this.name = requireNonBlank(name, "name").strip();
        this.address = Objects.requireNonNull(address, "address must not be null");
        this.coordinates = Objects.requireNonNull(coordinates, "coordinates must not be null");
        this.zoneId = Objects.requireNonNull(zoneId, "zoneId must not be null");

        this.openingHours = openingHours;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public Optional<OpeningHours> getOpeningHours() {
        return Optional.ofNullable(openingHours);
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    /**
     * Checks whether the store is open at the given instant.
     *
     * @param instant the instant to check, must not be null
     * @return {@code true} if the store has opening hours and is open at that instant,
     * {@code false} if it is closed or has no opening hours (e.g. closed for the day)
     */
    public boolean isOpenAt(Instant instant) {
        if (openingHours == null) {
            return false;
        }

        LocalTime localTime = LocalTime.ofInstant(instant, zoneId);

        return openingHours.isOpenAt(localTime);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Store store)) return false;

        return getId().equals(store.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "Store{" +
            "id='" + getId() + '\'' +
            ", name='" + getName() + '\'' +
            ", address=" + getAddress() +
            ", coordinates=" + getCoordinates() +
            ", openingHours=" + getOpeningHours() +
            ", zoneId=" + getZoneId() +
            '}';
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");

        // TODO: throw InvalidStoreException when value is blank

        return value;
    }

}
