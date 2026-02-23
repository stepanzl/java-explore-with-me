package ru.practicum.main.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
    public ApiError handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .toList();

        return build(HttpStatus.BAD_REQUEST, REASON_BAD_REQUEST,
                "Validation failed", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiError handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();

        return build(HttpStatus.BAD_REQUEST, REASON_BAD_REQUEST,
                "Validation failed", errors);
    }

    @ExceptionHandler(BadRequestException.class)
    public ApiError handleBadRequest(BadRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, REASON_BAD_REQUEST,
                ex.getMessage(), Collections.emptyList());
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class
    })
    public ApiError handleBadRequestFramework(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, REASON_BAD_REQUEST,
                ex.getMessage(), Collections.emptyList());
    }

    @ExceptionHandler(NotFoundException.class)
    public ApiError handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, REASON_NOT_FOUND,
                ex.getMessage(), Collections.emptyList());
    }

    @ExceptionHandler(ConflictException.class)
    public ApiError handleConflict(ConflictException ex) {
        return build(HttpStatus.CONFLICT, REASON_CONDITIONS_NOT_MET,
                ex.getMessage(), Collections.emptyList());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiError handleDataIntegrity(DataIntegrityViolationException ex) {
        return build(HttpStatus.CONFLICT, REASON_INTEGRITY_VIOLATED,
                ex.getMostSpecificCause().getMessage(),
                Collections.emptyList());
    }

    @ExceptionHandler(Exception.class)
    public ApiError handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error.",
                ex.getMessage(),
                Collections.emptyList());
    }

    private ApiError build(HttpStatus status, String reason,
                           String message, List<String> errors) {
        return ApiError.builder()
                .errors(errors == null ? Collections.emptyList() : errors)
                .message(message)
                .reason(reason)
                .status(status.value() + " " + status.name())
                .timestamp(LocalDateTime.now().format(TS_FORMAT))
                .build();
    }

    private String formatFieldError(FieldError fe) {
        return fe.getField() + ": " + fe.getDefaultMessage();
    }
}