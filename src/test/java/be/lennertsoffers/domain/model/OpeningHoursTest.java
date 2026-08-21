package be.lennertsoffers.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpeningHoursTest {

    @Test
    @DisplayName("Should create opening hours when opensAt is strictly before closesAt")
    void constructor_shouldCreateOpeningHours_whenOpensAtIsBeforeClosesAt() {
        OpeningHours openingHours = new OpeningHours(LocalTime.of(9, 0), LocalTime.of(18, 0));

        assertThat(openingHours.opensAt()).isEqualTo(LocalTime.of(9, 0));
        assertThat(openingHours.closesAt()).isEqualTo(LocalTime.of(18, 0));
    }

    @Test
    @DisplayName("Should throw NullPointerException with descriptive message when opensAt is null")
    void constructor_shouldThrowNullPointerException_whenOpensAtIsNull() {
        assertThatThrownBy(() -> new OpeningHours(null, LocalTime.of(18, 0)))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("opensAt must not be null");
    }

    @Test
    @DisplayName("Should throw NullPointerException with descriptive message when closesAt is null")
    void constructor_shouldThrowNullPointerException_whenClosesAtIsNull() {
        assertThatThrownBy(() -> new OpeningHours(LocalTime.of(9, 0), null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("closesAt must not be null");
    }

    @ParameterizedTest
    @CsvSource({
        // exactly at opening time is considered open (inclusive lower bound)
        "09:00, true",
        // strictly between opening and closing time is open
        "12:00, true",
        // one minute before closing time is still open
        "17:59, true",
        // exactly at closing time is considered closed (exclusive upper bound)
        "18:00, false",
        // any time after closing time is closed
        "18:01, false",
        // any time before opening time is closed
        "08:59, false"
    })
    @DisplayName("Should correctly determine whether the store is open at a given time")
    void isOpenAt_shouldReturnExpectedResult_whenCheckingAgainstOpeningHours(LocalTime time, boolean expectedIsOpen) {
        OpeningHours openingHours = new OpeningHours(LocalTime.of(9, 0), LocalTime.of(18, 0));

        boolean isOpen = openingHours.isOpenAt(time);

        assertThat(isOpen).isEqualTo(expectedIsOpen);
    }

    @Test
    @DisplayName("Should throw NullPointerException with descriptive message when checked time is null")
    void isOpenAt_shouldThrowNullPointerException_whenTimeIsNull() {
        OpeningHours openingHours = new OpeningHours(LocalTime.of(9, 0), LocalTime.of(18, 0));

        assertThatThrownBy(() -> openingHours.isOpenAt(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("time must not be null");
    }

    @Test
    @DisplayName("Should be equal when opensAt and closesAt match")
    void equals_shouldReturnTrue_whenOpensAtAndClosesAtAreEqual() {
        OpeningHours first = new OpeningHours(LocalTime.of(9, 0), LocalTime.of(18, 0));
        OpeningHours second = new OpeningHours(LocalTime.of(9, 0), LocalTime.of(18, 0));

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @ParameterizedTest
    @CsvSource({
        "10:00, 18:00",
        "09:00, 19:00"
    })
    @DisplayName("Should not be equal when opensAt or closesAt differs")
    void equals_shouldReturnFalse_whenOpensAtOrClosesAtDiffers(LocalTime opensAt, LocalTime closesAt) {
        OpeningHours original = new OpeningHours(LocalTime.of(9, 0), LocalTime.of(18, 0));
        OpeningHours other = new OpeningHours(opensAt, closesAt);

        assertThat(original).isNotEqualTo(other);
    }

}
