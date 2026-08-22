package com.yourcaryourway.chat.dto;

import com.yourcaryourway.chat.entity.Conversation;

import java.time.Instant;

public record ConversationResponse(Long id, Instant createdAt) {

    public static ConversationResponse from(Conversation conversation) {
        return new ConversationResponse(conversation.getId(), conversation.getCreatedAt());
    }
}
