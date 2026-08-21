package be.lennertsoffers.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoreSearchCriteriaTest {

    private static final Coordinates ORIGIN = new Coordinates(51.2194, 4.4025);
    private static final Instant SEARCH_TIME = Instant.parse("2024-01-01T10:00:00Z");

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Should create search criteria when origin and searchTime are provided")
    void constructor_shouldCreateSearchCriteria_whenOriginAndSearchTimeAreProvided(boolean openOnly) {
        StoreSearchCriteria criteria = new StoreSearchCriteria(ORIGIN, openOnly, SEARCH_TIME);

        assertThat(criteria.origin()).isEqualTo(ORIGIN);
        assertThat(criteria.openOnly()).isEqualTo(openOnly);
        assertThat(criteria.searchTime()).isEqualTo(SEARCH_TIME);
    }

    @Test
    @DisplayName("Should throw NullPointerException with descriptive message when origin is null")
    void constructor_shouldThrowNullPointerException_whenOriginIsNull() {
        assertThatThrownBy(() -> new StoreSearchCriteria(null, true, SEARCH_TIME))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("origin must not be null");
    }

    @Test
    @DisplayName("Should throw NullPointerException with descriptive message when searchTime is null")
    void constructor_shouldThrowNullPointerException_whenSearchTimeIsNull() {
        assertThatThrownBy(() -> new StoreSearchCriteria(ORIGIN, true, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("searchTime must not be null");
    }

    @Test
    @DisplayName("Should be equal when origin, openOnly and searchTime all match")
    void equals_shouldReturnTrue_whenAllFieldsAreEqual() {
        StoreSearchCriteria first = new StoreSearchCriteria(ORIGIN, true, SEARCH_TIME);
        StoreSearchCriteria second = new StoreSearchCriteria(new Coordinates(51.2194, 4.4025), true, Instant.parse("2024-01-01T10:00:00Z"));

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when origin differs")
    void equals_shouldReturnFalse_whenOriginDiffers() {
        StoreSearchCriteria original = new StoreSearchCriteria(ORIGIN, true, SEARCH_TIME);
        StoreSearchCriteria other = new StoreSearchCriteria(new Coordinates(50.8503, 4.3517), true, SEARCH_TIME);

        assertThat(original).isNotEqualTo(other);
    }

    @Test
    @DisplayName("Should not be equal when openOnly differs")
    void equals_shouldReturnFalse_whenOpenOnlyDiffers() {
        StoreSearchCriteria original = new StoreSearchCriteria(ORIGIN, true, SEARCH_TIME);
        StoreSearchCriteria other = new StoreSearchCriteria(ORIGIN, false, SEARCH_TIME);

        assertThat(original).isNotEqualTo(other);
    }

    @Test
    @DisplayName("Should not be equal when searchTime differs")
    void equals_shouldReturnFalse_whenSearchTimeDiffers() {
        StoreSearchCriteria original = new StoreSearchCriteria(ORIGIN, true, SEARCH_TIME);
        StoreSearchCriteria other = new StoreSearchCriteria(ORIGIN, true, SEARCH_TIME.plusSeconds(1));

        assertThat(original).isNotEqualTo(other);
    }

}
