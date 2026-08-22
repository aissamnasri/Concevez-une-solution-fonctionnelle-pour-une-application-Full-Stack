package com.yourcaryourway.chat.controller;

import com.yourcaryourway.chat.dto.MessageResponse;
import com.yourcaryourway.chat.service.ChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST de consultation de l'historique.
 *
 * <p>L'envoi d'un message passe par le WebSocket (voir ChatWebSocketController) :
 * REST sert uniquement a recharger l'historique, par exemple apres un F5.</p>
 */
@RestController
@RequestMapping("/api/conversations/{conversationId}/messages")
public class MessageController {

    private final ChatService chatService;

    public MessageController(ChatService chatService) {
        this.chatService = chatService;
    }

    /** GET /api/conversations/{conversationId}/messages */
    @GetMapping
    public List<MessageResponse> getHistory(@PathVariable Long conversationId) {
        return chatService.getHistory(conversationId);
    }
}
