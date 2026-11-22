package com.duoc.album.pattern;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.duoc.album.error.ErrorResponse;

public final class ErrorResponseBuilder {

    private HttpStatus status;
    private String message;
    private String path;
    private final Map<String, String> fields = new LinkedHashMap<>();

    private ErrorResponseBuilder() {
    }

    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }

    public ErrorResponseBuilder status(HttpStatus status) {
        this.status = status;
        return this;
    }

    public ErrorResponseBuilder message(String message) {
        this.message = message;
        return this;
    }

    public ErrorResponseBuilder path(String path) {
        this.path = path;
        return this;
    }

    public ErrorResponseBuilder field(String name, String error) {
        if (name != null && !name.isBlank()) {
            fields.putIfAbsent(name, error == null ? "Valor inválido" : error);
        }
        return this;
    }

    public ErrorResponse build() {
        if (status == null || !status.isError()) {
            throw new IllegalStateException("Status must be a 4xx or 5xx HTTP status");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalStateException("Message is required");
        }
        if (path == null || path.isBlank()) {
            throw new IllegalStateException("Path is required");
        }
        return new ErrorResponse(Instant.now(), status.value(), message, path,
                Collections.unmodifiableMap(new LinkedHashMap<>(fields)));
    }

    public ResponseEntity<ErrorResponse> toResponseEntity() {
        ErrorResponse body = build();
        return ResponseEntity.status(body.status()).body(body);
    }
}
