package be.lennertsoffers.domain.service;

import be.lennertsoffers.domain.model.StoreSearchCriteria;
import be.lennertsoffers.domain.model.StoreWithDistance;
import be.lennertsoffers.domain.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class StoreService {

    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = Objects.requireNonNull(storeRepository, "storeRepository must not be null");
    }

    /**
     * Finds the stores nearest to the origin described by the given criteria.
     *
     * @param criteria the search criteria, must not be null
     * @param limit    the maximum number of stores to return, must be positive
     * @return the nearest stores matching the criteria, ordered by ascending distance
     */
    public List<StoreWithDistance> findNearestStores(StoreSearchCriteria criteria, int limit) {
        Objects.requireNonNull(criteria, "criteria must not be null");

        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive but was " + limit);
        }

        return storeRepository.findNearest(criteria, limit);
    }

}
