package be.lennertsoffers.domain.exception;

/** Thrown when opening hours are inconsistent, e.g. the closing time is not after the opening time. */
public class InvalidOpeningHoursException extends DomainException {

    public InvalidOpeningHoursException(String message) {
        super(message);
    }

}
