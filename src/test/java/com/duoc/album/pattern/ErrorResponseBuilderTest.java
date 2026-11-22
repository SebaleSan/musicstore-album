package com.duoc.album.pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.duoc.album.error.ErrorResponse;

class ErrorResponseBuilderTest {

    @Test
    void buildsResponseWithFieldsInInsertionOrder() {
        ErrorResponse response = ErrorResponseBuilder.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message("Invalid request")
                .path("/api/items")
                .field("name", "Name is required")
                .field("price", null)
                .field("name", "Ignored duplicate")
                .build();

        assertEquals(400, response.status());
        assertEquals("/api/items", response.path());
        assertEquals(List.of("name", "price"), List.copyOf(response.fields().keySet()));
        assertEquals("Name is required", response.fields().get("name"));
        assertEquals("Valor inválido", response.fields().get("price"));
    }

    @Test
    void rejectsNonErrorStatus() {
        ErrorResponseBuilder builder = ErrorResponseBuilder.builder()
                .status(HttpStatus.OK)
                .message("Done")
                .path("/api/items");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void rejectsMissingMessage() {
        ErrorResponseBuilder builder = ErrorResponseBuilder.builder()
                .status(HttpStatus.NOT_FOUND)
                .path("/api/items/1");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void fieldsAreImmutable() {
        ErrorResponse response = ErrorResponseBuilder.builder()
                .status(HttpStatus.NOT_FOUND)
                .message("Not found")
                .path("/api/items/1")
                .build();

        assertThrows(UnsupportedOperationException.class, () -> response.fields().put("id", "x"));
    }
}
