package com.yourcaryourway.chat.dto;

import java.time.Instant;

/**
 * Format unique des erreurs renvoyees par l'API REST.
 */
public record ApiError(int status, String message, Instant timestamp) {

    public static ApiError of(int status, String message) {
        return new ApiError(status, message, Instant.now());
    }
}
