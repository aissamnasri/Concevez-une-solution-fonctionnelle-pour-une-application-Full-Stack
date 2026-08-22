package com.yourcaryourway.chat.dto;

import com.yourcaryourway.chat.entity.Message;

import java.time.Instant;

/**
 * Representation d'un message exposee au frontend (REST et WebSocket).
 */
public record MessageResponse(
        Long id,
        Long conversationId,
        String senderName,
        String content,
        Instant createdAt
) {

    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSenderName(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
