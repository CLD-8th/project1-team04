package com.team4.usedtradeapp.common;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException exception) {
        return error(HttpStatus.UNAUTHORIZED, exception);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException exception) {
        return error(HttpStatus.FORBIDDEN, exception);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException exception) {
        return error(HttpStatus.CONFLICT, exception);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, RuntimeException exception) {
        ErrorResponse body = new ErrorResponse(
                status.value(), status.name(), exception.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(status).body(body);
    }
}
