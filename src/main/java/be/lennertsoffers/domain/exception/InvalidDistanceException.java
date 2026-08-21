package be.lennertsoffers.domain.exception;

/** Thrown when a distance is not a finite, non-negative number. */
public class InvalidDistanceException extends DomainException {

    public InvalidDistanceException(double distanceInKm) {
        super(String.format("Distance must be a finite, non-negative number, but was: %f", distanceInKm));
    }

}
