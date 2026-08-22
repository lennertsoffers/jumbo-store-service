package be.lennertsoffers.infrastructure.rest;

import be.lennertsoffers.application.StoreServiceApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.hamcrest.Matchers;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = StoreServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(StoreControllerIntegrationTest.FixedClockConfiguration.class)
@TestPropertySource(properties = "store-service.store.dataset-location=classpath:stores-test.json")
class StoreControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class FixedClockConfiguration {

        @Bean
        @Primary
        Clock fixedClock() {
            // Europe/Amsterdam is UTC+2 in summer, so this resolves to 12:00 local time,
            // which falls within every mock store's opening hours except the closed one.
            return Clock.fixed(Instant.parse("2024-06-10T10:00:00Z"), ZoneOffset.UTC);
        }

    }

    @Test
    @DisplayName("Should return the five nearest stores ordered by ascending distance by default")
    void findNearestStores_shouldReturnFiveClosestStoresOrderedByDistance_whenNoLimitGiven() throws Exception {
        mockMvc.perform(get("/api/v1/stores")
                .param("latitude", "52.3791")
                .param("longitude", "4.9003"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(5))
            .andExpect(jsonPath("$[0].id").value("store-1-dam"))
            .andExpect(jsonPath("$[1].id").value("store-2-zuid"))
            .andExpect(jsonPath("$[2].id").value("store-3-utrecht"))
            .andExpect(jsonPath("$[3].id").value("store-4-rotterdam"))
            .andExpect(jsonPath("$[4].id").value("store-5-groningen"))
            .andExpect(jsonPath("$[0].distanceInKm").value(Matchers.lessThan(1.0)));
    }

    @Test
    @DisplayName("Should limit the number of returned stores when a limit param is given")
    void findNearestStores_shouldReturnLimitedResults_whenLimitParamGiven() throws Exception {
        mockMvc.perform(get("/api/v1/stores")
                .param("latitude", "52.3791")
                .param("longitude", "4.9003")
                .param("limit", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value("store-1-dam"))
            .andExpect(jsonPath("$[1].id").value("store-2-zuid"));
    }

    @Test
    @DisplayName("Should exclude closed stores when the open filter is enabled")
    void findNearestStores_shouldExcludeClosedStores_whenOpenFilterEnabled() throws Exception {
        mockMvc.perform(get("/api/v1/stores")
                .param("latitude", "52.3791")
                .param("longitude", "4.9003")
                .param("limit", "10")
                .param("open", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(4))
            .andExpect(jsonPath("$[*].id", Matchers.not(Matchers.hasItem("store-4-rotterdam"))));
    }

    @Test
    @DisplayName("Should return 400 with a problem detail body when latitude is out of range")
    void findNearestStores_shouldReturnBadRequest_whenLatitudeIsOutOfRange() throws Exception {
        mockMvc.perform(get("/api/v1/stores")
                .param("latitude", "1000")
                .param("longitude", "4.9003"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.errors.latitude").exists());
    }

    @Test
    @DisplayName("Should return 400 with a problem detail body when latitude is missing")
    void findNearestStores_shouldReturnBadRequest_whenLatitudeIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/stores")
                .param("longitude", "4.9003"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.errors.latitude").exists());
    }

}
