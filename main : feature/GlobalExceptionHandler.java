package com.roombooking.exception;

import com.roombooking.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException exception) {
        return error(exception.getStatus(), exception.getMessages());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException exception) {
        return error(HttpStatus.BAD_REQUEST, Map.of("body", "Request body is invalid"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissing(MissingServletRequestParameterException exception) {
        String name = exception.getParameterName();
        return error(HttpStatus.BAD_REQUEST, Map.of(name, name + " is required"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMismatch(MethodArgumentTypeMismatchException exception) {
        String name = exception.getName() == null ? "request" : exception.getName();
        return error(HttpStatus.BAD_REQUEST, Map.of(name, name + " is invalid"));
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, Map<String, String> messages) {
        ErrorResponse body = new ErrorResponse(status.value(), status.getReasonPhrase(), messages);
        return ResponseEntity.status(status).body(body);
    }
}
