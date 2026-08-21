package be.lennertsoffers.domain.exception;

/** Thrown when coordinates fall outside their valid latitude/longitude range. */
public class InvalidCoordinatesException extends DomainException {

    public InvalidCoordinatesException(String message) {
        super(message);
    }

}
