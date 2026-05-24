package com.picbed.exception;

import com.picbed.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Result<Void>> handleForbidden(ForbiddenException e) {
        log.warn("Forbidden: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.error(e.getMessage(), 403));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Result<Void>> handleUnauthorized(UnauthorizedException e) {
        log.warn("Unauthorized: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(e.getMessage(), 401));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Result<Void>> handleNotFound(NotFoundException e) {
        log.warn("Not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.error(e.getMessage(), 404));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleBadRequest(IllegalArgumentException e) {
        log.warn("Bad request: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error(e.getMessage(), 400));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        log.warn("Validation failed: {}", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error(msg, 400));
    }

    @ExceptionHandler(OssOperationException.class)
    public ResponseEntity<Result<Void>> handleOssError(OssOperationException e) {
        log.error("OSS operation failed: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error("OSS operation failed: " + e.getMessage(), 500));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleGeneral(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error("Internal server error", 500));
    }
}
