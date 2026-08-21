package be.lennertsoffers.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class CoordinatesTest {

    @ParameterizedTest
    @CsvSource({
        "0.0, 0.0",
        "-90.0, -180.0",
        "90.0, 180.0",
        "51.2194, 4.4025"
    })
    @DisplayName("Should create coordinates when latitude and longitude are within their valid ranges")
    void constructor_shouldCreateCoordinates_whenLatitudeAndLongitudeAreValid(double latitude, double longitude) {
        Coordinates coordinates = new Coordinates(latitude, longitude);

        assertThat(coordinates.latitude()).isEqualTo(latitude);
        assertThat(coordinates.longitude()).isEqualTo(longitude);
    }

    @ParameterizedTest
    @CsvSource({
        // identical coordinates should yield zero distance
        "51.2194, 4.4025, 51.2194, 4.4025, 0.0",
        // North Pole to South Pole is half the earth's circumference (pi * R)
        "90.0, 0.0, -90.0, 0.0, 20015.086796020572",
        // a quarter turn along the equator is a quarter of the earth's circumference ((pi / 2) * R)
        "0.0, 0.0, 0.0, 90.0, 10007.543398010284",
        // traveling 180 degrees of longitude on the equator is antipodal, so also half the circumference
        "0.0, 0.0, 0.0, 180.0, 20015.086796020572",
        // a quarter turn along a meridian (equator to pole) is also a quarter of the circumference
        "0.0, 45.0, 90.0, 45.0, 10007.543398010284",
        // two points just two degrees apart, straddling the antimeridian (179 to -179)
        "0.0, 179.0, 0.0, -179.0, 222.38985328911653",
        // +180 and -180 longitude represent the same meridian, so the distance should be effectively zero
        "0.0, -180.0, 0.0, 180.0, 0.0",
        // real-world short distance between two nearby cities (Antwerp and Brussels)
        "51.2194, 4.4025, 50.8503, 4.3517, 41.19547722407601",
        // real-world long distance spanning both hemispheres (Sydney and New York)
        "-33.8688, 151.2093, 40.7128, -74.0060, 15988.755507039632"
    })
    @DisplayName("Should return the expected haversine distance for well-known coordinate pairs")
    void distanceTo_shouldReturnExpectedDistance_whenCalculatingKnownCoordinatePairs(
        double lat1, double lon1, double lat2, double lon2, double expectedDistanceInKm
    ) {
        Coordinates origin = new Coordinates(lat1, lon1);
        Coordinates target = new Coordinates(lat2, lon2);

        double distance = origin.distanceTo(target);

        assertThat(distance).isCloseTo(expectedDistanceInKm, within(0.000001));
    }

    @Test
    @DisplayName("Should return the same distance regardless of calculation direction")
    void distanceTo_shouldBeSymmetric_whenCalculatedInBothDirections() {
        Coordinates first = new Coordinates(51.2194, 4.4025);
        Coordinates second = new Coordinates(50.8503, 4.3517);

        double distanceForward = first.distanceTo(second);
        double distanceBackward = second.distanceTo(first);

        assertThat(distanceForward).isCloseTo(distanceBackward, within(0.0001));
    }

    @Test
    @DisplayName("Should throw NullPointerException with descriptive message when target is null")
    void distanceTo_shouldThrowNullPointerException_whenTargetIsNull() {
        Coordinates origin = new Coordinates(51.2194, 4.4025);

        assertThatThrownBy(() -> origin.distanceTo(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("target coordinates must not be null");
    }

    @Test
    @DisplayName("Should be equal when latitude and longitude match")
    void equals_shouldReturnTrue_whenLatitudeAndLongitudeAreEqual() {
        Coordinates first = new Coordinates(51.2194, 4.4025);
        Coordinates second = new Coordinates(51.2194, 4.4025);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @ParameterizedTest
    @CsvSource({
        "50.8503, 4.4025",
        "51.2194, 4.3517"
    })
    @DisplayName("Should not be equal when latitude or longitude differs")
    void equals_shouldReturnFalse_whenLatitudeOrLongitudeDiffers(double latitude, double longitude) {
        Coordinates original = new Coordinates(51.2194, 4.4025);
        Coordinates other = new Coordinates(latitude, longitude);

        assertThat(original).isNotEqualTo(other);
    }

}
