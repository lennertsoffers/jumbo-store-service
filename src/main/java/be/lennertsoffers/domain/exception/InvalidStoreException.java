package be.lennertsoffers.domain.exception;

/** Thrown when a required store field is missing or blank. */
public class InvalidStoreException extends DomainException {

    public InvalidStoreException(String message) {
        super(message);
    }

}
