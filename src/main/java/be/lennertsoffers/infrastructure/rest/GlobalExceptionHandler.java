package be.lennertsoffers.infrastructure.rest;

import be.lennertsoffers.domain.exception.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Clock;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                GlobalExceptionHandler::resolveFieldErrorMessage,
                (existing, _) -> existing
            ));

        log.warn("Request validation failed reason={}", errors);

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation Failed");
        problemDetail.setProperty("timestamp", clock.instant());
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not supported for request reason={}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.METHOD_NOT_ALLOWED);
        problemDetail.setTitle("Method Not Allowed");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setProperty("timestamp", clock.instant());
        if (ex.getSupportedMethods() != null && ex.getSupportedMethods().length > 0) {
            problemDetail.setProperty("supportedMethods", ex.getSupportedMethods());
        }

        return problemDetail;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("Resource or endpoint not found path={}", ex.getResourcePath());

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle("Not Found");
        problemDetail.setDetail("The requested resource or endpoint was not found.");
        problemDetail.setProperty("timestamp", clock.instant());

        return problemDetail;
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(DomainException ex) {
        log.warn("Domain invariant violated reason={}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation error");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setProperty("timestamp", clock.instant());

        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleClientError(IllegalArgumentException ex) {
        log.warn("Bad client request argument reason={}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Bad Request");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setProperty("timestamp", clock.instant());

        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUncaughtException(Exception ex) {
        log.error("Unhandled error occurred while processing request", ex);

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setDetail("An unexpected server error occurred.");
        problemDetail.setProperty("timestamp", clock.instant());

        return problemDetail;
    }

    /**
     * Resolves a client-friendly message for a field error, hiding the framework's default type-conversion
     * message (which leaks internal Java type names) behind a stable, readable message.
     */
    private static String resolveFieldErrorMessage(FieldError error) {
        if (error.isBindingFailure()) {
            Object rejectedValue = error.getRejectedValue();
            return rejectedValue == null
                ? error.getField() + " has an invalid value"
                : "'" + rejectedValue + "' is not a valid value for " + error.getField();
        }

        return error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value";
    }

}