package ru.practicum.explorewithme.exception;

import jakarta.validation.ConstraintViolationException;
import org.apache.coyote.BadRequestException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.explorewithme.exception.custom.ConflictException;
import ru.practicum.explorewithme.exception.custom.ForbiddenActionException;
import ru.practicum.explorewithme.exception.custom.NotFoundException;
import ru.practicum.explorewithme.exception.custom.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException notFound) {
        return buildError(notFound, HttpStatus.NOT_FOUND, "The required object was not found.");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final ConflictException conflict) {
        return buildError(conflict, HttpStatus.CONFLICT, "Integrity constraint has been violated.");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestException(final BadRequestException badRequest) {
        return buildError(badRequest, HttpStatus.BAD_REQUEST, "Incorrectly made request.");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleForbiddenException(final ForbiddenActionException forbiddenAction) {
        return buildError(forbiddenAction, HttpStatus.FORBIDDEN, "For the requested operation the conditions are not met.");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Throwable unexpectedError) {
        return buildError(unexpectedError, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolation(DataIntegrityViolationException dataBaseError) {
        return buildError(dataBaseError, HttpStatus.CONFLICT,
                "Integrity constraint violated at the database level.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(ValidationException validationException) {
        return buildError(validationException, HttpStatus.BAD_REQUEST, "Invalid request, validation failed");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleJsonParseError(HttpMessageNotReadableException ex) {
        return buildError(ex, HttpStatus.BAD_REQUEST, "JSON parse error");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolationException(ConstraintViolationException constraintViolationException) {
        return buildError(constraintViolationException, HttpStatus.BAD_REQUEST,
                "Validation failed: " + constraintViolationException.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleTypeMismatch(MethodArgumentTypeMismatchException mismatchException) {
        return buildError(mismatchException, HttpStatus.BAD_REQUEST,
                "Incorrect request: " + mismatchException.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingParam(MissingServletRequestParameterException parameterException) {
        return buildError(parameterException, HttpStatus.BAD_REQUEST,
                "Missing required request parameter: " + parameterException.getParameterName());
    }

    private ApiError buildError(Throwable e, HttpStatus status, String reason) {
        return ApiError.builder()
                .status(status)
                .reason(reason)
                .message(e.getMessage())
                .errors(List.of(e.getClass().getName()))
                .timestamp(LocalDateTime.now())
                .build();
    }
}