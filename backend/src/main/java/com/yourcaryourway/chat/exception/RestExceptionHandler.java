package com.yourcaryourway.chat.exception;

import com.yourcaryourway.chat.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Traduit les exceptions applicatives en reponses HTTP lisibles.
 */
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(ConversationNotFoundException.class)
    public ResponseEntity<ApiError> handleConversationNotFound(ConversationNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(HttpStatus.NOT_FOUND.value(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        String details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST.value(), details));
    }
}
