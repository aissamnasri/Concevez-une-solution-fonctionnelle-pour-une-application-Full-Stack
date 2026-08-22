package com.yourcaryourway.chat.exception;

public class ConversationNotFoundException extends RuntimeException {

    public ConversationNotFoundException(Long conversationId) {
        super("Conversation introuvable : " + conversationId);
    }
}
