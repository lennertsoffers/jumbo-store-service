package be.lennertsoffers.domain.model;

import be.lennertsoffers.domain.exception.InvalidAddressException;

import java.util.Objects;

/**
 * A postal address for a store.
 *
 * <p>{@code city}, {@code postalCode} and {@code street} are mandatory. {@code houseNumber} is optional
 * because the source feed does not always provide it; when it is missing or blank it is normalised to
 * {@code null} so the store is still retained for nearest-store search.
 */
public record Address(
    String city,
    String postalCode,
    String street,
    String houseNumber
) {

    public Address {
        city = requireNonBlank(city, "city").strip();
        postalCode = requireNonBlank(postalCode, "postalCode").strip();
        street = requireNonBlank(street, "street").strip();
        houseNumber = (houseNumber == null || houseNumber.isBlank()) ? null : houseNumber.strip();
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");

        if (value.isBlank()) {
            throw new InvalidAddressException(fieldName + " must not be blank");
        }

        return value;
    }

}
