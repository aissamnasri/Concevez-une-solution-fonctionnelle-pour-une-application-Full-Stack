package com.yourcaryourway.chat.repository;

import com.yourcaryourway.chat.entity.Conversation;
import com.yourcaryourway.chat.entity.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de persistance : les messages sont bien ecrits en base et relus
 * dans l'ordre, conversation par conversation.
 */
@DataJpaTest
class MessageRepositoryTest {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Test
    @DisplayName("Les messages sont persistes et relus dans l'ordre chronologique")
    void savesAndReadsHistoryInOrder() {
        Conversation conversation = conversationRepository.save(new Conversation());

        messageRepository.save(new Message(conversation, "Alice", "Bonjour"));
        messageRepository.save(new Message(conversation, "Bob", "Bonjour Alice"));

        List<Message> history =
                messageRepository.findByConversationIdOrderByCreatedAtAscIdAsc(conversation.getId());

        assertThat(history)
                .hasSize(2)
                .extracting(Message::getContent)
                .containsExactly("Bonjour", "Bonjour Alice");
        assertThat(history.get(0).getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("L'historique d'une conversation ne contient pas les messages des autres")
    void historyIsScopedToOneConversation() {
        Conversation first = conversationRepository.save(new Conversation());
        Conversation second = conversationRepository.save(new Conversation());
        messageRepository.save(new Message(first, "Alice", "Bonjour"));

        List<Message> history =
                messageRepository.findByConversationIdOrderByCreatedAtAscIdAsc(second.getId());

        assertThat(history).isEmpty();
    }
}
