package be.lennertsoffers.infrastructure.repository;

import be.lennertsoffers.domain.model.Store;
import be.lennertsoffers.infrastructure.repository.dto.StoreDto;
import be.lennertsoffers.infrastructure.repository.dto.StoreCollectionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * Loads the store seed data from the configured JSON resource once at startup and maps it to the domain model.
 *
 * <p>Records that cannot be made searchable (invalid coordinates) are skipped and logged; optional data such as
 * opening hours never causes a valid store to be dropped. The resulting list is immutable for the lifetime of
 * the application.
 */
@Component
public class JsonStoreDataLoader {

    private static final Logger log = LoggerFactory.getLogger(JsonStoreDataLoader.class);

    private final List<Store> stores;
    private final ZoneId zoneId;

    public JsonStoreDataLoader(
        @Value("${store-service.store.dataset-location}") Resource storesJsonResource,
        @Value("${store-service.store.timezone}") String zoneIdStr,
        ObjectMapper objectMapper,
        Clock clock
    ) {
        this.zoneId = ZoneId.of(zoneIdStr);

        if (!storesJsonResource.isReadable()) {
            throw new IllegalStateException("Store dataset is missing or unreadable at: "
                + storesJsonResource.getDescription());
        }

        Instant start = clock.instant();

        try (InputStream inputStream = storesJsonResource.getInputStream()) {
            StoreCollectionDto dataset = objectMapper.readValue(inputStream, StoreCollectionDto.class);

            this.stores = dataset.stores().stream()
                .map(this::tryMapToDomain)
                .flatMap(Optional::stream)
                .toList();

            Duration elapsed = Duration.between(start, clock.instant());
            log.info("Loaded store seed data recordCount={} skippedCount={} durationMs={}",
                stores.size(), dataset.stores().size() - stores.size(), elapsed.toMillis());
        } catch (IOException | JacksonException e) {
            throw new IllegalStateException("Failed to parse the store seed data", e);
        }
    }

    public List<Store> getStores() {
        return stores;
    }

    private Optional<Store> tryMapToDomain(StoreDto store) {
        try {
            return Optional.of(store.toDomain(zoneId));
        } catch (Exception e) {
            log.warn("Skipping invalid store record uuid={} reason={}", store.uuid(), e.getMessage());
            return Optional.empty();
        }
    }

}
