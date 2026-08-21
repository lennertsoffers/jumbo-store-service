package be.lennertsoffers.domain.exception;

/**
 * Base type for violations of a domain invariant.
 *
 * <p>These represent invalid domain state (typically caused by bad input) and are mapped to
 * {@code 400 Bad Request} by the web layer.
 */
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
}

