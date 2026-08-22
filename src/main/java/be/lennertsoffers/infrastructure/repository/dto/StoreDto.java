package be.lennertsoffers.infrastructure.repository.dto;

import be.lennertsoffers.domain.exception.InvalidCoordinatesException;
import be.lennertsoffers.domain.model.Address;
import be.lennertsoffers.domain.model.Coordinates;
import be.lennertsoffers.domain.exception.InvalidOpeningHoursException;
import be.lennertsoffers.domain.model.OpeningHours;
import be.lennertsoffers.domain.model.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Transport model mirroring a single store record in the seed JSON.
 *
 * <p>Fields are captured as raw strings exactly as the feed provides them (coordinates and times are text) and
 * normalized on construction. {@link #toDomain(ZoneId)} translates the record into the {@link Store} domain
 * model.
 */
public record StoreDto(
    String uuid,
    String addressName,
    String city,
    String postalCode,
    String street,
    String street2,
    String street3,
    String longitude,
    String latitude,
    String todayOpen,
    String todayClose,
    String complexNumber,
    String sapStoreID,
    String locationType,
    boolean showWarningMessage,
    Boolean collectionPoint
) {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("[HH:mm][H:mm][HH:mm:ss][H:mm:ss]");
    private static final String TIME_CLOSED = "gesloten";

    private static final Logger log = LoggerFactory.getLogger(StoreDto.class);

    public StoreDto {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("Store UUID must not be null or blank");
        }

        addressName = sanitize(addressName);
        city = sanitize(city);
        postalCode = sanitize(postalCode);
        street = sanitize(street);
        street2 = sanitize(street2);
        street3 = sanitize(street3);
        longitude = sanitize(longitude);
        latitude = sanitize(latitude);
        todayOpen = sanitize(todayOpen);
        todayClose = sanitize(todayClose);
        complexNumber = sanitize(complexNumber);
    }

    /**
     * Maps this record to a {@link Store} in the given store timezone.
     *
     * @param zoneId the timezone the store's opening hours are expressed in; must not be null
     * @return the mapped domain store
     * @throws InvalidCoordinatesException if the coordinates are out of range
     * @throws IllegalArgumentException    if a required field is missing or malformed
     */
    public Store toDomain(ZoneId zoneId) {
        return new Store(
            uuid,
            addressName,
            new Address(city, postalCode, street, street2),
            new Coordinates(
                parseCoordinate(latitude, "latitude"),
                parseCoordinate(longitude, "longitude")
            ),
            createOpeningHours(),
            zoneId
        );
    }

    /**
     * Maps the raw {@code todayOpen}/{@code todayClose} fields to {@link OpeningHours}. Opening hours are optional
     * enrichment: they must never cause an otherwise valid store to be dropped. When the
     * hours are missing, blank, marked {@code "gesloten"}, or cannot be parsed, {@code null} is returned (the store
     * is kept but reported as having no opening hours today).
     */
    private OpeningHours createOpeningHours() {
        if (todayOpen == null || todayOpen.isBlank() || todayClose == null || todayClose.isBlank()) {
            log.debug("Store [uuid={}] has no opening hours today (open='{}', close='{}')", uuid, todayOpen, todayClose);
            return null;
        }

        if (TIME_CLOSED.equalsIgnoreCase(todayOpen) || TIME_CLOSED.equalsIgnoreCase(todayClose)) {
            return null;
        }

        LocalTime opensAt = parseTime(todayOpen, "todayOpen");
        LocalTime closesAt = parseTime(todayClose, "todayClose");
        if (opensAt == null || closesAt == null) {
            return null;
        }

        try {
            return new OpeningHours(opensAt, closesAt);
        } catch (InvalidOpeningHoursException e) {
            log.warn("Store [uuid={}] has inconsistent opening hours, treating as no hours today: {}", uuid, e.getMessage());
            return null;
        }
    }

    private LocalTime parseTime(String value, String fieldName) {
        try {
            return LocalTime.parse(value, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("Store [uuid={}] has an unparseable {} '{}', treating as no hours today", uuid, fieldName, value);
            return null;
        }
    }

    private double parseCoordinate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                String.format("Failed to map store JSON [uuid=%s]: %s is missing or blank", this.uuid, fieldName)
            );
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                String.format("Failed to map store JSON [uuid=%s]: Invalid %s format '%s'", this.uuid, fieldName, value), e
            );
        }
    }

    private static String sanitize(String value) {
        return value != null ? value.strip() : null;
    }

}
