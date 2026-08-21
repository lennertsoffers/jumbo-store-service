package be.lennertsoffers.domain.model;

import be.lennertsoffers.domain.exception.InvalidCoordinatesException;

import java.util.Objects;

/**
 * Represents a geographical coordinate pair on Earth defined by latitude and longitude.
 *
 * @param latitude  the latitude in degrees, must be between -90.0 and 90.0 inclusive
 * @param longitude the longitude in degrees, must be between -180.0 and 180.0 inclusive
 */
public record Coordinates(
    double latitude,
    double longitude
) {

    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;
    private static final double EARTH_RADIUS_KM = 6371.0;

    public Coordinates {
        if (Double.isNaN(latitude) || latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
            throw new InvalidCoordinatesException("Latitude must be between " + MIN_LATITUDE + " and " + MAX_LATITUDE + ". Got: " + latitude);
        }

        if (Double.isNaN(longitude) || longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
            throw new InvalidCoordinatesException("Longitude must be between " + MIN_LONGITUDE + " and " + MAX_LONGITUDE + ". Got: " + longitude);
        }
    }

    /**
     * Calculates the great-circle distance between these coordinates and a target position
     * using the Haversine formula.
     *
     * @param target the target coordinates, must not be null
     * @return the calculated distance in kilometers
     * @throws NullPointerException if target is null
     */
    public double distanceTo(Coordinates target) {
        Objects.requireNonNull(target, "target coordinates must not be null");

        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(target.latitude());

        double dLatHalfSin = Math.sin(Math.toRadians(target.latitude() - this.latitude) / 2.0);
        double dLonHalfSin = Math.sin(Math.toRadians(target.longitude() - this.longitude) / 2.0);

        double a = dLatHalfSin * dLatHalfSin
            + Math.cos(lat1Rad) * Math.cos(lat2Rad) * dLonHalfSin * dLonHalfSin;

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        return EARTH_RADIUS_KM * c;
    }

}
