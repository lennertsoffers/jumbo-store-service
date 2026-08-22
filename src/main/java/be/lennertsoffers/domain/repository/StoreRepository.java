package be.lennertsoffers.domain.repository;

import be.lennertsoffers.domain.model.StoreSearchCriteria;
import be.lennertsoffers.domain.model.StoreWithDistance;

import java.util.List;

/**
 * Port for querying stores by proximity to an origin.
 */
public interface StoreRepository {

    /**
     * Finds the stores matching the given criteria, ordered by ascending distance to the origin.
     *
     * @param criteria the search criteria
     * @param limit    the maximum number of stores to return
     * @return the matching stores ordered nearest-first, or an empty list if none match
     */
    List<StoreWithDistance> findNearest(StoreSearchCriteria criteria, int limit);

}
