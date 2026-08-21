package be.lennertsoffers.domain.exception;

/** Thrown when a required address field is missing or blank. */
public class InvalidAddressException extends DomainException {

    public InvalidAddressException(String message) {
        super(message);
    }

}
