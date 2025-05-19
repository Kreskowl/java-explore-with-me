package ru.practicum.explorewithme.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(NotFoundException notFound) {
        logger.error("Error: {}", notFound.getMessage());
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(),
                "Not Found", notFound.getMessage());
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpectedError(Throwable unexpectedError) {
        logger.error("Unexpected error occurred", unexpectedError);
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred. Please try again later.", unexpectedError.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        logger.error("Invalid request param", ex);
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad request",
                "Invalid value for parameter: " + ex.getName()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        logger.error("Validation failed: {}", message);

        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                "Validation error",
                message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidFormat(HttpMessageNotReadableException ex) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid request format", ex.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingParams(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                "Missing required parameter: " + paramName, ex.getMessage());
    }

    @ExceptionHandler(TimeRangeValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTimeRangeValidation(TimeRangeValidationException ex) {
        logger.error("Invalid time range: {}", ex.getMessage());
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                "Invalid time range", ex.getMessage());
    }

    @ExceptionHandler(InvalidUriParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidUriParam(InvalidUriParameterException ex) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                "Invalid 'uris' parameter", ex.getMessage());
    }
}

