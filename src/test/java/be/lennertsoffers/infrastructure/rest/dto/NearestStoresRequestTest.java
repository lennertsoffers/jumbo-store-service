package be.lennertsoffers.infrastructure.rest.dto;

import be.lennertsoffers.domain.model.StoreSearchCriteria;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class NearestStoresRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    private static NearestStoresRequest request(Double latitude, Double longitude, Integer limit, Boolean open) {
        return new NearestStoresRequest(latitude, longitude, limit, open);
    }

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        validatorFactory.close();
    }

    @Test
    @DisplayName("Should keep the given latitude, longitude, limit and open values when all are provided")
    void constructor_shouldKeepGivenValues_whenAllFieldsAreProvided() {
        NearestStoresRequest request = request(51.2194, 4.4025, 10, true);

        assertThat(request.latitude()).isEqualTo(51.2194);
        assertThat(request.longitude()).isEqualTo(4.4025);
        assertThat(request.limit()).isEqualTo(10);
        assertThat(request.open()).isTrue();
    }

    @Test
    @DisplayName("Should default limit to 5 when limit is not provided")
    void constructor_shouldDefaultLimitToFive_whenLimitIsNull() {
        NearestStoresRequest request = request(51.2194, 4.4025, null, true);

        assertThat(request.limit()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should default open to false when open is not provided")
    void constructor_shouldDefaultOpenToFalse_whenOpenIsNull() {
        NearestStoresRequest request = request(51.2194, 4.4025, 10, null);

        assertThat(request.open()).isFalse();
    }

    @Test
    @DisplayName("Should default both limit and open when neither is provided")
    void constructor_shouldDefaultBothLimitAndOpen_whenBothAreNull() {
        NearestStoresRequest request = request(51.2194, 4.4025, null, null);

        assertThat(request.limit()).isEqualTo(5);
        assertThat(request.open()).isFalse();
    }

    @Test
    @DisplayName("Should build a StoreSearchCriteria with the request's coordinates, open flag and the clock's instant")
    void toCriteria_shouldBuildCriteria_whenRequestIsValid() {
        Instant fixedInstant = Instant.parse("2024-01-01T10:00:00Z");
        Clock clock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
        NearestStoresRequest request = request(51.2194, 4.4025, 10, true);

        StoreSearchCriteria criteria = request.toCriteria(clock);

        assertThat(criteria.origin().latitude()).isEqualTo(51.2194);
        assertThat(criteria.origin().longitude()).isEqualTo(4.4025);
        assertThat(criteria.openOnly()).isTrue();
        assertThat(criteria.searchTime()).isEqualTo(fixedInstant);
    }

    @Test
    @DisplayName("Should use the clock's current instant as searchTime")
    void toCriteria_shouldUseClockInstant_asSearchTime() {
        Instant fixedInstant = Instant.parse("2025-06-15T08:30:00Z");
        Clock clock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
        NearestStoresRequest request = request(51.2194, 4.4025, null, null);

        StoreSearchCriteria criteria = request.toCriteria(clock);

        assertThat(criteria.searchTime()).isEqualTo(fixedInstant);
    }

    @Test
    @DisplayName("Should not report any violations when the request is fully valid")
    void validate_shouldReportNoViolations_whenRequestIsValid() {
        NearestStoresRequest request = request(51.2194, 4.4025, 10, true);

        Set<ConstraintViolation<NearestStoresRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(doubles = {-90.1, 90.1, -1000.0, 1000.0})
    @DisplayName("Should report a violation when latitude is null or out of the [-90, 90] range")
    void validate_shouldReportViolation_whenLatitudeIsNullOrOutOfRange(Double latitude) {
        NearestStoresRequest request = request(latitude, 4.4025, 10, true);

        Set<ConstraintViolation<NearestStoresRequest>> violations = validator.validate(request);

        assertThat(violations)
            .extracting(ConstraintViolation::getPropertyPath)
            .extracting(Object::toString)
            .contains("latitude");
    }

    @ParameterizedTest
    @ValueSource(doubles = {-90.0, 0.0, 90.0})
    @DisplayName("Should not report a violation when latitude is at or within the [-90, 90] boundary")
    void validate_shouldReportNoViolation_whenLatitudeIsWithinRange(double latitude) {
        NearestStoresRequest request = request(latitude, 4.4025, 10, true);

        Set<ConstraintViolation<NearestStoresRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(doubles = {-180.1, 180.1, -1000.0, 1000.0})
    @DisplayName("Should report a violation when longitude is null or out of the [-180, 180] range")
    void validate_shouldReportViolation_whenLongitudeIsNullOrOutOfRange(Double longitude) {
        NearestStoresRequest request = request(51.2194, longitude, 10, true);

        Set<ConstraintViolation<NearestStoresRequest>> violations = validator.validate(request);

        assertThat(violations)
            .extracting(ConstraintViolation::getPropertyPath)
            .extracting(Object::toString)
            .contains("longitude");
    }

    @ParameterizedTest
    @ValueSource(doubles = {-180.0, 0.0, 180.0})
    @DisplayName("Should not report a violation when longitude is at or within the [-180, 180] boundary")
    void validate_shouldReportNoViolation_whenLongitudeIsWithinRange(double longitude) {
        NearestStoresRequest request = request(51.2194, longitude, 10, true);

        Set<ConstraintViolation<NearestStoresRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    @DisplayName("Should report a violation when limit is not a positive number")
    void validate_shouldReportViolation_whenLimitIsNotPositive(int limit) {
        NearestStoresRequest request = request(51.2194, 4.4025, limit, true);

        Set<ConstraintViolation<NearestStoresRequest>> violations = validator.validate(request);

        assertThat(violations)
            .extracting(ConstraintViolation::getPropertyPath)
            .extracting(Object::toString)
            .contains("limit");
    }

    @ParameterizedTest
    @ValueSource(ints = {51, 100})
    @DisplayName("Should report a violation when limit exceeds the maximum of 50")
    void validate_shouldReportViolation_whenLimitExceedsMaximum(int limit) {
        NearestStoresRequest request = request(51.2194, 4.4025, limit, true);

        Set<ConstraintViolation<NearestStoresRequest>> violations = validator.validate(request);

        assertThat(violations)
            .extracting(ConstraintViolation::getPropertyPath)
            .extracting(Object::toString)
            .contains("limit");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 50})
    @DisplayName("Should not report a violation when limit is within the [1, 50] boundary")
    void validate_shouldReportNoViolation_whenLimitIsWithinRange(int limit) {
        NearestStoresRequest request = request(51.2194, 4.4025, limit, true);

        Set<ConstraintViolation<NearestStoresRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "-90.5, 4.4025",
        "51.2194, -180.5"
    })
    @DisplayName("Should report multiple violations when both latitude and limit are invalid")
    void validate_shouldReportViolation_whenLatitudeInvalidAndLimitNonPositive(double latitude, double longitude) {
        NearestStoresRequest request = request(latitude, longitude, 0, true);

        Set<ConstraintViolation<NearestStoresRequest>> violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(2);
    }

}
