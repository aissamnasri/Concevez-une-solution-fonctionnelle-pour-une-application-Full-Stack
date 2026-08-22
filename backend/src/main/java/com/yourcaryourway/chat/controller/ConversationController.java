package com.yourcaryourway.chat.controller;

import com.yourcaryourway.chat.dto.ConversationResponse;
import com.yourcaryourway.chat.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST de gestion des conversations.
 */
@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ChatService chatService;

    public ConversationController(ChatService chatService) {
        this.chatService = chatService;
    }

    /** POST /api/conversations : cree une nouvelle conversation vide. */
    @PostMapping
    public ResponseEntity<ConversationResponse> createConversation() {
        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.createConversation());
    }

    /** GET /api/conversations/{id} : verifie qu'une conversation existe. */
    @GetMapping("/{conversationId}")
    public ConversationResponse getConversation(@PathVariable Long conversationId) {
        return chatService.getConversation(conversationId);
    }
}
