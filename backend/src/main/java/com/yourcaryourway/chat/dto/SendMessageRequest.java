package com.yourcaryourway.chat.dto;

import com.yourcaryourway.chat.entity.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Charge utile envoyee par le client (via WebSocket) pour publier un message.
 */
public record SendMessageRequest(

        @NotBlank(message = "Le nom de l'expediteur est obligatoire")
        @Size(max = Message.MAX_SENDER_NAME_LENGTH, message = "Le nom de l'expediteur est trop long")
        String senderName,

        @NotBlank(message = "Le message ne peut pas etre vide")
        @Size(max = Message.MAX_CONTENT_LENGTH, message = "Le message est trop long")
        String content
) {
}
