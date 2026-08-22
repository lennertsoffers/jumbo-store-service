package be.lennertsoffers.infrastructure.rest;

import be.lennertsoffers.domain.model.StoreSearchCriteria;
import be.lennertsoffers.domain.service.StoreService;
import be.lennertsoffers.infrastructure.rest.dto.NearestStoreResponse;
import be.lennertsoffers.infrastructure.rest.dto.NearestStoresRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
public class StoreController {

    private static final Logger log = LoggerFactory.getLogger(StoreController.class);

    private final StoreService storeService;
    private final Clock clock;

    public StoreController(
        StoreService storeService,
        Clock clock
    ) {
        this.storeService = storeService;
        this.clock = clock;
    }

    /**
     * Returns the stores nearest to the requested position, ordered by ascending distance.
     *
     * @param params the validated query parameters (origin, limit and optional open filter)
     * @return {@code 200 OK} with the matching stores, nearest first (possibly empty)
     */
    @GetMapping
    public ResponseEntity<List<NearestStoreResponse>> findNearestStores(@Valid NearestStoresRequest params) {
        Instant start = clock.instant();

        StoreSearchCriteria criteria = params.toCriteria(clock);

        List<NearestStoreResponse> nearestStores = storeService.findNearestStores(criteria, params.limit())
            .stream()
            .map(NearestStoreResponse::fromDomain)
            .toList();

        Duration elapsed = Duration.between(start, clock.instant());

        log.info("GET /api/v1/stores lat={} lng={} limit={} openOnly={} resultCount={} durationMs={}",
            params.latitude(), params.longitude(), params.limit(), params.open(),
            nearestStores.size(), elapsed.toMillis());

        return ResponseEntity.ok(nearestStores);
    }

}
