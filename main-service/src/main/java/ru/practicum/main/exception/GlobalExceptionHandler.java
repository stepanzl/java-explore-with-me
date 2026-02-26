package ru.practicum.main.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String REASON_BAD_REQUEST = "Incorrectly made request.";
    private static final String REASON_NOT_FOUND = "The required object was not found.";
    private static final String REASON_CONDITIONS_NOT_MET = "For the requested operation the conditions are not met.";
    private static final String REASON_INTEGRITY_VIOLATED = "Integrity constraint has been violated.";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .toList();

        log.warn("Validation failed: {}", ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, REASON_BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();

        log.warn("Constraint violation: {}", ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, REASON_BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, REASON_BAD_REQUEST, ex.getMessage(), Collections.emptyList());
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiError> handleBadRequestFramework(Exception ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, REASON_BAD_REQUEST, ex.getMessage(), Collections.emptyList());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return respond(HttpStatus.NOT_FOUND, REASON_NOT_FOUND, ex.getMessage(), Collections.emptyList());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return respond(HttpStatus.CONFLICT, REASON_CONDITIONS_NOT_MET, ex.getMessage(), Collections.emptyList());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause().getMessage();

        if (message == null) {
            message = ex.getMessage();
        }

        log.warn("Integrity violation: {}", message);

        return respond(
                HttpStatus.CONFLICT,
                REASON_INTEGRITY_VIOLATED,
                message,
                Collections.emptyList()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error.", ex.getMessage(), Collections.emptyList());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, REASON_BAD_REQUEST, ex.getMessage(), Collections.emptyList());
    }

    private ResponseEntity<ApiError> respond(HttpStatus status, String reason, String message, List<String> errors) {
        ApiError body = ApiError.builder()
                .errors(errors == null ? Collections.emptyList() : errors)
                .message(message)
                .reason(reason)
                .status(status.value() + " " + status.name())
                .timestamp(LocalDateTime.now().format(TS_FORMAT))
                .build();
        return ResponseEntity.status(status).body(body);
    }

    private String formatFieldError(FieldError fe) {
        return fe.getField() + ": " + fe.getDefaultMessage();
    }
}