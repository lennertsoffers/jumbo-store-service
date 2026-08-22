package be.lennertsoffers.infrastructure.repository.dto;

import java.util.List;

/**
 * Root object of the store seed JSON, wrapping the list of store records.
 *
 * @param stores the store records; may be empty
 */
public record StoreCollectionDto(List<StoreDto> stores) {
}
