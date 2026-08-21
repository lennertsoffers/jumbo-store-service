package be.lennertsoffers.domain.model;

import be.lennertsoffers.domain.exception.InvalidAddressException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AddressTest {

    @Test
    @DisplayName("Should create an address when all fields are valid")
    void constructor_shouldCreateAddress_whenAllFieldsAreValid() {
        Address address = new Address("Antwerp", "2000", "Meir", "1");

        assertThat(address.city()).isEqualTo("Antwerp");
        assertThat(address.postalCode()).isEqualTo("2000");
        assertThat(address.street()).isEqualTo("Meir");
        assertThat(address.houseNumber()).isEqualTo("1");
    }

    @ParameterizedTest
    @ValueSource(strings = {" Antwerp", "Antwerp ", " Antwerp "})
    @DisplayName("Should strip surrounding whitespace from every field")
    void constructor_shouldStripWhitespace_whenFieldsHaveSurroundingSpaces(String city) {
        Address address = new Address(city, " 2000 ", " Meir ", " 1 ");

        assertThat(address.city()).isEqualTo("Antwerp");
        assertThat(address.postalCode()).isEqualTo("2000");
        assertThat(address.street()).isEqualTo("Meir");
        assertThat(address.houseNumber()).isEqualTo("1");
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Should throw InvalidAddressException when city is empty or blank")
    void constructor_shouldThrowInvalidAddressException_whenCityIsBlank(String city) {
        assertThatThrownBy(() -> new Address(city, "2000", "Meir", "1"))
            .isInstanceOf(InvalidAddressException.class)
            .hasMessage("city must not be blank");
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Should throw InvalidAddressException when postalCode is empty or blank")
    void constructor_shouldThrowInvalidAddressException_whenPostalCodeIsBlank(String postalCode) {
        assertThatThrownBy(() -> new Address("Antwerp", postalCode, "Meir", "1"))
            .isInstanceOf(InvalidAddressException.class)
            .hasMessage("postalCode must not be blank");
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Should throw InvalidAddressException when street is empty or blank")
    void constructor_shouldThrowInvalidAddressException_whenStreetIsBlank(String street) {
        assertThatThrownBy(() -> new Address("Antwerp", "2000", street, "1"))
            .isInstanceOf(InvalidAddressException.class)
            .hasMessage("street must not be blank");
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Should normalise a blank houseNumber to null since it is optional")
    void constructor_shouldNormaliseHouseNumberToNull_whenHouseNumberIsBlank(String houseNumber) {
        Address address = new Address("Antwerp", "2000", "Meir", houseNumber);

        assertThat(address.houseNumber()).isNull();
    }

    @Test
    @DisplayName("Should throw NullPointerException with descriptive message when city is null")
    void constructor_shouldThrowNullPointerException_whenCityIsNull() {
        assertThatThrownBy(() -> new Address(null, "2000", "Meir", "1"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("city must not be null");
    }

    @Test
    @DisplayName("Should throw NullPointerException with descriptive message when postalCode is null")
    void constructor_shouldThrowNullPointerException_whenPostalCodeIsNull() {
        assertThatThrownBy(() -> new Address("Antwerp", null, "Meir", "1"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("postalCode must not be null");
    }

    @Test
    @DisplayName("Should throw NullPointerException with descriptive message when street is null")
    void constructor_shouldThrowNullPointerException_whenStreetIsNull() {
        assertThatThrownBy(() -> new Address("Antwerp", "2000", null, "1"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("street must not be null");
    }

    @Test
    @DisplayName("Should normalise houseNumber to null when it is null since it is optional")
    void constructor_shouldNormaliseHouseNumberToNull_whenHouseNumberIsNull() {
        Address address = new Address("Antwerp", "2000", "Meir", null);

        assertThat(address.houseNumber()).isNull();
    }

    @Test
    @DisplayName("Should be equal when all fields match")
    void equals_shouldReturnTrue_whenAllFieldsAreEqual() {
        Address first = new Address("Antwerp", "2000", "Meir", "1");
        Address second = new Address("Antwerp", "2000", "Meir", "1");

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @ParameterizedTest
    @CsvSource({
        "Ghent, 2000, Meir, 1",
        "Antwerp, 9000, Meir, 1",
        "Antwerp, 2000, Veldstraat, 1",
        "Antwerp, 2000, Meir, 2"
    })
    @DisplayName("Should not be equal when any field differs")
    void equals_shouldReturnFalse_whenAnyFieldDiffers(String city, String postalCode, String street, String houseNumber) {
        Address original = new Address("Antwerp", "2000", "Meir", "1");
        Address other = new Address(city, postalCode, street, houseNumber);

        assertThat(original).isNotEqualTo(other);
    }

}
