package com.yourcaryourway.chat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourcaryourway.chat.dto.MessageResponse;
import com.yourcaryourway.chat.dto.SendMessageRequest;
import com.yourcaryourway.chat.entity.Conversation;
import com.yourcaryourway.chat.entity.Message;
import com.yourcaryourway.chat.repository.ConversationRepository;
import com.yourcaryourway.chat.repository.MessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de bout en bout du mecanisme central du PoC : un message envoye en
 * WebSocket est persiste puis diffuse aux abonnes de la conversation.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatWebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Test
    @DisplayName("Un message envoye en WebSocket est diffuse aux abonnes et enregistre en base")
    void messageIsBroadcastAndPersisted() throws Exception {
        Long conversationId = conversationRepository.save(new Conversation()).getId();

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(converter);

        StompSession session = stompClient
                .connectAsync("ws://localhost:" + port + "/ws", new StompSessionHandlerAdapter() { })
                .get(5, TimeUnit.SECONDS);

        CompletableFuture<MessageResponse> received = new CompletableFuture<>();
        session.subscribe("/topic/conversations/" + conversationId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MessageResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                received.complete((MessageResponse) payload);
            }
        });

        // Laisse au serveur le temps d'enregistrer l'abonnement avant la publication.
        Thread.sleep(500);

        session.send("/app/conversations/" + conversationId + "/send",
                new SendMessageRequest("Alice", "Bonjour"));

        MessageResponse broadcastMessage = received.get(5, TimeUnit.SECONDS);
        assertThat(broadcastMessage.senderName()).isEqualTo("Alice");
        assertThat(broadcastMessage.content()).isEqualTo("Bonjour");
        assertThat(broadcastMessage.conversationId()).isEqualTo(conversationId);

        List<Message> history =
                messageRepository.findByConversationIdOrderByCreatedAtAscIdAsc(conversationId);
        assertThat(history)
                .hasSize(1)
                .extracting(Message::getContent)
                .containsExactly("Bonjour");

        session.disconnect();
        stompClient.stop();
    }
}
