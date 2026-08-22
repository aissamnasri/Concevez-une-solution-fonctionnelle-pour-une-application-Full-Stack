package com.yourcaryourway.chat.service;

import com.yourcaryourway.chat.dto.MessageResponse;
import com.yourcaryourway.chat.dto.SendMessageRequest;
import com.yourcaryourway.chat.entity.Conversation;
import com.yourcaryourway.chat.entity.Message;
import com.yourcaryourway.chat.exception.ConversationNotFoundException;
import com.yourcaryourway.chat.repository.ConversationRepository;
import com.yourcaryourway.chat.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de la logique du tchat (envoi et historique).
 */
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    private static final Long CONVERSATION_ID = 1L;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private ChatService chatService;

    private Conversation conversation;

    @BeforeEach
    void setUp() {
        conversation = new Conversation();
        ReflectionTestUtils.setField(conversation, "id", CONVERSATION_ID);
    }

    @Test
    @DisplayName("Un message envoye est persiste et renvoye au format attendu")
    void saveMessage_persistsAndReturnsMessage() {
        when(conversationRepository.findById(CONVERSATION_ID)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            ReflectionTestUtils.setField(message, "id", 10L);
            ReflectionTestUtils.setField(message, "createdAt", Instant.parse("2026-01-01T10:00:00Z"));
            return message;
        });

        MessageResponse response = chatService.saveMessage(
                CONVERSATION_ID, new SendMessageRequest("Alice", "  Bonjour  "));

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo("Bonjour");
        assertThat(captor.getValue().getConversation()).isSameAs(conversation);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.conversationId()).isEqualTo(CONVERSATION_ID);
        assertThat(response.senderName()).isEqualTo("Alice");
        assertThat(response.content()).isEqualTo("Bonjour");
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-01-01T10:00:00Z"));
    }

    @Test
    @DisplayName("Envoyer dans une conversation inconnue leve une erreur et n'ecrit rien")
    void saveMessage_failsWhenConversationDoesNotExist() {
        when(conversationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.saveMessage(99L, new SendMessageRequest("Alice", "Bonjour")))
                .isInstanceOf(ConversationNotFoundException.class);

        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    @DisplayName("L'historique est renvoye dans l'ordre chronologique")
    void getHistory_returnsMessagesInOrder() {
        when(conversationRepository.findById(CONVERSATION_ID)).thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversationIdOrderByCreatedAtAscIdAsc(CONVERSATION_ID))
                .thenReturn(List.of(
                        persistedMessage(1L, "Alice", "Bonjour"),
                        persistedMessage(2L, "Bob", "Bonjour Alice")));

        List<MessageResponse> history = chatService.getHistory(CONVERSATION_ID);

        assertThat(history)
                .extracting(MessageResponse::senderName)
                .containsExactly("Alice", "Bob");
    }

    private Message persistedMessage(Long id, String senderName, String content) {
        Message message = new Message(conversation, senderName, content);
        ReflectionTestUtils.setField(message, "id", id);
        ReflectionTestUtils.setField(message, "createdAt", Instant.parse("2026-01-01T10:00:00Z"));
        return message;
    }
}
