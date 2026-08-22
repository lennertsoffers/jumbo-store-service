package be.lennertsoffers.infrastructure.repository;

import be.lennertsoffers.domain.model.Coordinates;
import be.lennertsoffers.domain.model.Store;
import be.lennertsoffers.domain.model.StoreSearchCriteria;
import be.lennertsoffers.domain.model.StoreWithDistance;
import be.lennertsoffers.domain.repository.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;

/**
 * In-memory {@link StoreRepository} backed by the seed data loaded at startup.
 *
 * <p>Ranks stores with a linear scan and sort per request.
 */
@Repository
public class InMemoryStoreRepository implements StoreRepository {

    private static final Logger log = LoggerFactory.getLogger(InMemoryStoreRepository.class);

    private final List<Store> stores;

    public InMemoryStoreRepository(JsonStoreDataLoader jsonStoreDataLoader) {
        this.stores = jsonStoreDataLoader.getStores();
    }

    @Override
    public List<StoreWithDistance> findNearest(StoreSearchCriteria criteria, int limit) {
        if (stores.isEmpty() || limit <= 0) {
            log.debug("Skipping nearest store search totalStores={} limit={}", stores.size(), limit);
            return List.of();
        }

        final Coordinates origin = criteria.origin();

        return stores.stream()
            .filter(store -> matchesCriteria(store, criteria))
            .map(store -> calculateDistance(store, origin))
            .sorted(Comparator.comparingDouble(StoreWithDistance::distanceInKm))
            .limit(limit)
            .toList();
    }

    private static boolean matchesCriteria(Store store, StoreSearchCriteria criteria) {
        if (!criteria.openOnly()) return true;

        return store.isOpenAt(criteria.searchTime());
    }

    private static StoreWithDistance calculateDistance(Store store, Coordinates origin) {
        double distanceKm = origin.distanceTo(store.getCoordinates());

        return new StoreWithDistance(store, distanceKm);
    }

}
