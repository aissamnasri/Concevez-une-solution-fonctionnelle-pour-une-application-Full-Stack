package com.yourcaryourway.chat.controller;

import com.yourcaryourway.chat.dto.MessageResponse;
import com.yourcaryourway.chat.exception.ConversationNotFoundException;
import com.yourcaryourway.chat.service.ChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test du endpoint REST principal : la recuperation de l'historique.
 */
@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @Test
    @DisplayName("GET /api/conversations/{id}/messages renvoie l'historique")
    void returnsHistory() throws Exception {
        when(chatService.getHistory(1L)).thenReturn(List.of(
                new MessageResponse(1L, 1L, "Alice", "Bonjour", Instant.parse("2026-01-01T10:00:00Z")),
                new MessageResponse(2L, 1L, "Bob", "Bonjour Alice", Instant.parse("2026-01-01T10:00:05Z"))));

        mockMvc.perform(get("/api/conversations/1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].senderName").value("Alice"))
                .andExpect(jsonPath("$[0].content").value("Bonjour"))
                .andExpect(jsonPath("$[1].senderName").value("Bob"));
    }

    @Test
    @DisplayName("GET sur une conversation inconnue renvoie 404")
    void returnsNotFoundForUnknownConversation() throws Exception {
        when(chatService.getHistory(99L)).thenThrow(new ConversationNotFoundException(99L));

        mockMvc.perform(get("/api/conversations/99/messages"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
