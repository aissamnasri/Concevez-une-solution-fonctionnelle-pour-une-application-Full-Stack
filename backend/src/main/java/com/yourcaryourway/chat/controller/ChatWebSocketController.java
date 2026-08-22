package com.yourcaryourway.chat.controller;

import com.yourcaryourway.chat.dto.MessageResponse;
import com.yourcaryourway.chat.dto.SendMessageRequest;
import com.yourcaryourway.chat.service.ChatService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Point d'entree WebSocket du tchat.
 *
 * <p>Flux : le client publie sur /app/conversations/{id}/send, le message est
 * persiste, puis il est diffuse a tous les clients abonnes a
 * /topic/conversations/{id} (y compris l'expediteur).</p>
 */
@Controller
public class ChatWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);
    private static final String TOPIC_PREFIX = "/topic/conversations/";

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/conversations/{conversationId}/send")
    public void handleMessage(@DestinationVariable Long conversationId,
                              @Valid @Payload SendMessageRequest request) {

        MessageResponse savedMessage = chatService.saveMessage(conversationId, request);
        messagingTemplate.convertAndSend(TOPIC_PREFIX + conversationId, savedMessage);

        log.info("Message #{} diffuse sur {}{}", savedMessage.id(), TOPIC_PREFIX, conversationId);
    }

    /**
     * Filet de securite : une erreur de traitement ne doit pas couper la session
     * WebSocket des autres participants. Le PoC se limite a journaliser l'erreur
     * (voir README, section "Limites du PoC").
     */
    @MessageExceptionHandler
    public void handleException(Exception exception) {
        log.warn("Message WebSocket rejete : {}", exception.getMessage());
    }
}
