package com.duoc.album.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.duoc.album.pattern.ErrorResponseBuilder;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        ErrorResponseBuilder builder = error(HttpStatus.BAD_REQUEST, "Solicitud inválida", request);
        ex.getBindingResult().getFieldErrors()
                .forEach(fieldError -> builder.field(fieldError.getField(), fieldError.getDefaultMessage()));
        return builder.toResponseEntity();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                        HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request).toResponseEntity();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex,
                                                              HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "JSON con formato incorrecto", request).toResponseEntity();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Valor inválido para el parámetro '" + ex.getName() + "'", request)
                .toResponseEntity();
    }

    @ExceptionHandler(AuthenticationServiceException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationUnavailable(AuthenticationServiceException ex,
                                                                         HttpServletRequest request) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "Servicio de usuarios no disponible", request).toResponseEntity();
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthenticated(AuthenticationException ex,
                                                               HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "Credenciales inválidas o ausentes", request).toResponseEntity();
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
                                                            HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "Acceso denegado", request).toResponseEntity();
    }

    private ErrorResponseBuilder error(HttpStatus status, String message, HttpServletRequest request) {
        return ErrorResponseBuilder.builder()
                .status(status)
                .message(message)
                .path(request.getRequestURI());
    }
}
