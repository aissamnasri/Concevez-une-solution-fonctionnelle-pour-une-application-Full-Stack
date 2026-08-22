package com.yourcaryourway.chat.service;

import com.yourcaryourway.chat.dto.ConversationResponse;
import com.yourcaryourway.chat.dto.MessageResponse;
import com.yourcaryourway.chat.dto.SendMessageRequest;
import com.yourcaryourway.chat.entity.Conversation;
import com.yourcaryourway.chat.entity.Message;
import com.yourcaryourway.chat.exception.ConversationNotFoundException;
import com.yourcaryourway.chat.repository.ConversationRepository;
import com.yourcaryourway.chat.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Toute la logique metier du tchat.
 *
 * <p>Les controleurs (REST et WebSocket) ne font que traduire des requetes :
 * ils n'accedent jamais directement aux repositories.</p>
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ChatService(ConversationRepository conversationRepository, MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public ConversationResponse createConversation() {
        Conversation conversation = conversationRepository.save(new Conversation());
        log.info("Conversation #{} creee", conversation.getId());
        return ConversationResponse.from(conversation);
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversation(Long conversationId) {
        Conversation conversation = findConversationOrFail(conversationId);
        return ConversationResponse.from(conversation);
    }

    /**
     * Enregistre un message puis renvoie sa representation.
     * La persistance a lieu AVANT la diffusion : un client ne recoit jamais
     * un message qui ne serait pas en base.
     */
    @Transactional
    public MessageResponse saveMessage(Long conversationId, SendMessageRequest request) {
        Conversation conversation = findConversationOrFail(conversationId);

        Message message = new Message(conversation, request.senderName().trim(), request.content().trim());
        Message savedMessage = messageRepository.save(message);

        // On ne journalise pas le contenu du message (donnee potentiellement personnelle).
        log.info("Message #{} persiste dans la conversation #{} ({} caracteres)",
                savedMessage.getId(), conversationId, savedMessage.getContent().length());

        return MessageResponse.from(savedMessage);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getHistory(Long conversationId) {
        findConversationOrFail(conversationId);

        List<MessageResponse> history = messageRepository
                .findByConversationIdOrderByCreatedAtAscIdAsc(conversationId)
                .stream()
                .map(MessageResponse::from)
                .toList();

        log.debug("Historique de la conversation #{} : {} message(s)", conversationId, history.size());
        return history;
    }

    private Conversation findConversationOrFail(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));
    }
}
