package be.lennertsoffers.infrastructure.repository;

import be.lennertsoffers.domain.model.Store;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JsonStoreDataLoaderTest {

    private static final String ZONE_ID_STR = "Europe/Brussels";
    private static final ZoneId ZONE_ID = ZoneId.of(ZONE_ID_STR);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2024-01-01T10:00:00Z"), ZoneOffset.UTC);

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Mock
    private Resource unreadableResource;

    @Mock
    private Resource brokenResource;

    private static Resource jsonResource(String json) {
        return new ByteArrayResource(json.getBytes(StandardCharsets.UTF_8));
    }

    private static String storeJson(String uuid, String latitude, String longitude, String todayOpen, String todayClose) {
        return """
            {
                "stores": [
                    {
                        "uuid": "%s",
                        "addressName": "Jumbo Meir",
                        "city": "Antwerp",
                        "postalCode": "2000",
                        "street": "Meir",
                        "street2": "1",
                        "longitude": "%s",
                        "latitude": "%s",
                        "todayOpen": "%s",
                        "todayClose": "%s",
                        "complexNumber": "123",
                        "sapStoreID": "SAP123",
                        "locationType": "supermarket",
                        "showWarningMessage": false,
                        "collectionPoint": true
                    }
                ]
            }
            """.formatted(uuid, longitude, latitude, todayOpen, todayClose);
    }

    @Test
    @DisplayName("Should load and expose all valid stores from the JSON dataset")
    void constructor_shouldLoadStores_whenDatasetContainsValidRecords() {
        Resource resource = jsonResource(storeJson("uuid-1", "51.2194", "4.4025", "09:00", "18:00"));

        JsonStoreDataLoader loader = new JsonStoreDataLoader(resource, ZONE_ID_STR, objectMapper, CLOCK);

        assertThat(loader.getStores()).hasSize(1);
        Store store = loader.getStores().getFirst();
        assertThat(store.getId()).isEqualTo("uuid-1");
        assertThat(store.getName()).isEqualTo("Jumbo Meir");
        assertThat(store.getCoordinates().latitude()).isEqualTo(51.2194);
        assertThat(store.getCoordinates().longitude()).isEqualTo(4.4025);
        assertThat(store.getZoneId()).isEqualTo(ZONE_ID);
    }

    @Test
    @DisplayName("Should expose an empty store list when the dataset contains no stores")
    void constructor_shouldExposeEmptyStoreList_whenDatasetIsEmpty() {
        Resource resource = jsonResource("{ \"stores\": [] }");

        JsonStoreDataLoader loader = new JsonStoreDataLoader(resource, ZONE_ID_STR, objectMapper, CLOCK);

        assertThat(loader.getStores()).isEmpty();
    }

    @Test
    @DisplayName("Should skip records with invalid coordinates while keeping the valid ones")
    void constructor_shouldSkipInvalidRecords_whenSomeStoresHaveInvalidCoordinates() {
        String json = """
            {
                "stores": [
                    {
                        "uuid": "invalid-store",
                        "addressName": "Jumbo Invalid",
                        "city": "Antwerp",
                        "postalCode": "2000",
                        "street": "Meir",
                        "street2": "1",
                        "longitude": "4.4025",
                        "latitude": "1000",
                        "todayOpen": "09:00",
                        "todayClose": "18:00",
                        "complexNumber": "123",
                        "sapStoreID": "SAP123",
                        "locationType": "supermarket",
                        "showWarningMessage": false,
                        "collectionPoint": true
                    },
                    {
                        "uuid": "valid-store",
                        "addressName": "Jumbo Meir",
                        "city": "Antwerp",
                        "postalCode": "2000",
                        "street": "Meir",
                        "street2": "1",
                        "longitude": "4.4025",
                        "latitude": "51.2194",
                        "todayOpen": "09:00",
                        "todayClose": "18:00",
                        "complexNumber": "123",
                        "sapStoreID": "SAP123",
                        "locationType": "supermarket",
                        "showWarningMessage": false,
                        "collectionPoint": true
                    }
                ]
            }
            """;

        JsonStoreDataLoader loader = new JsonStoreDataLoader(jsonResource(json), ZONE_ID_STR, objectMapper, CLOCK);

        assertThat(loader.getStores())
            .hasSize(1)
            .extracting(Store::getId)
            .containsExactly("valid-store");
    }

    @Test
    @DisplayName("Should keep records with missing opening hours, exposing them without opening hours")
    void constructor_shouldKeepRecords_whenSomeStoresHaveMissingOpeningHours() {
        String json = """
            {
                "stores": [
                    {
                        "uuid": "no-hours-store",
                        "addressName": "Jumbo Invalid",
                        "city": "Antwerp",
                        "postalCode": "2000",
                        "street": "Meir",
                        "street2": "1",
                        "longitude": "4.4025",
                        "latitude": "51.2194",
                        "todayOpen": "",
                        "todayClose": "18:00",
                        "complexNumber": "123",
                        "sapStoreID": "SAP123",
                        "locationType": "supermarket",
                        "showWarningMessage": false,
                        "collectionPoint": true
                    },
                    {
                        "uuid": "valid-store",
                        "addressName": "Jumbo Meir",
                        "city": "Antwerp",
                        "postalCode": "2000",
                        "street": "Meir",
                        "street2": "1",
                        "longitude": "4.4025",
                        "latitude": "51.2194",
                        "todayOpen": "09:00",
                        "todayClose": "18:00",
                        "complexNumber": "123",
                        "sapStoreID": "SAP123",
                        "locationType": "supermarket",
                        "showWarningMessage": false,
                        "collectionPoint": true
                    }
                ]
            }
            """;

        JsonStoreDataLoader loader = new JsonStoreDataLoader(jsonResource(json), ZONE_ID_STR, objectMapper, CLOCK);

        assertThat(loader.getStores())
            .hasSize(2)
            .extracting(Store::getId)
            .containsExactly("no-hours-store", "valid-store");
        assertThat(loader.getStores().getFirst().getOpeningHours()).isEmpty();
    }

    @Test
    @DisplayName("Should throw IllegalStateException when the dataset resource is not readable")
    void constructor_shouldThrowIllegalStateException_whenResourceIsNotReadable() {
        when(unreadableResource.isReadable()).thenReturn(false);
        when(unreadableResource.getDescription()).thenReturn("classpath:missing.json");

        assertThatThrownBy(() -> new JsonStoreDataLoader(unreadableResource, ZONE_ID_STR, objectMapper, CLOCK))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Store dataset is missing or unreadable at: classpath:missing.json");
    }

    @Test
    @DisplayName("Should throw IllegalStateException wrapping the IOException when reading the resource fails")
    void constructor_shouldThrowIllegalStateException_whenResourceInputStreamFails() throws IOException {
        IOException ioException = new IOException("disk error");
        when(brokenResource.isReadable()).thenReturn(true);
        when(brokenResource.getInputStream()).thenThrow(ioException);

        assertThatThrownBy(() -> new JsonStoreDataLoader(brokenResource, ZONE_ID_STR, objectMapper, CLOCK))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Failed to parse the store seed data")
            .hasCause(ioException);
    }

    @Test
    @DisplayName("Should throw IllegalStateException wrapping the parse error when the JSON is malformed")
    void constructor_shouldThrowIllegalStateException_whenJsonIsMalformed() {
        Resource resource = jsonResource("{ this is not valid json ");

        assertThatThrownBy(() -> new JsonStoreDataLoader(resource, ZONE_ID_STR, objectMapper, CLOCK))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Failed to parse the store seed data")
            .hasCauseInstanceOf(tools.jackson.core.JacksonException.class);
    }

    @Test
    @DisplayName("Should throw an exception when the configured timezone identifier is invalid")
    void constructor_shouldThrowException_whenZoneIdIsInvalid() {
        Resource resource = jsonResource("{ \"stores\": [] }");

        assertThatThrownBy(() -> new JsonStoreDataLoader(resource, "Not/A_Zone", objectMapper, CLOCK))
            .isInstanceOf(java.time.DateTimeException.class);
    }

}
