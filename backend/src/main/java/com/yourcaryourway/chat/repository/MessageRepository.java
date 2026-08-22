package com.yourcaryourway.chat.repository;

import com.yourcaryourway.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Historique d'une conversation, du plus ancien au plus recent.
     * L'id departage les messages enregistres a la meme milliseconde.
     */
    List<Message> findByConversationIdOrderByCreatedAtAscIdAsc(Long conversationId);
}
