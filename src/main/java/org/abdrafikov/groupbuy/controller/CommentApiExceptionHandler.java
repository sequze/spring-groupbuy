package org.abdrafikov.groupbuy.controller;

import jakarta.validation.ConstraintViolationException;
import org.abdrafikov.groupbuy.exception.AccessDeniedException;
import org.abdrafikov.groupbuy.exception.ResourceNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.stream.Collectors;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = CommentRestController.class)
public class CommentApiExceptionHandler {

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<Map<String, String>> handleBadRequest(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException validationException) {
            String message = validationException.getBindingResult().getFieldErrors().stream()
                    .map(error -> toValidationMessage(error.getField(), error.getDefaultMessage()))
                    .collect(Collectors.joining("; "));
            return ResponseEntity.badRequest().body(Map.of("message", message));
        }

        if (ex instanceof ConstraintViolationException validationException) {
            String message = validationException.getConstraintViolations().stream()
                    .map(violation -> violation.getMessage() == null ? "Некорректное значение" : violation.getMessage())
                    .collect(Collectors.joining("; "));
            return ResponseEntity.badRequest().body(Map.of("message", message));
        }

        if (ex instanceof MissingServletRequestParameterException missingParameterException) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message",
                    "Не указан обязательный параметр: " + missingParameterException.getParameterName()
            ));
        }

        if (ex instanceof HttpMessageNotReadableException) {
            return ResponseEntity.badRequest().body(Map.of("message", "Некорректный JSON-запрос"));
        }

        if (ex instanceof MethodArgumentTypeMismatchException) {
            return ResponseEntity.badRequest().body(Map.of("message", "Некорректный параметр запроса"));
        }

        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    private String toValidationMessage(String field, String defaultMessage) {
        if ("purchaseItemId".equals(field)) {
            return "Позиция закупки обязательна";
        }
        if ("content".equals(field)) {
            return "Комментарий обязателен и должен быть не длиннее 2000 символов";
        }
        return defaultMessage == null ? "Некорректное значение" : defaultMessage;
    }
}
