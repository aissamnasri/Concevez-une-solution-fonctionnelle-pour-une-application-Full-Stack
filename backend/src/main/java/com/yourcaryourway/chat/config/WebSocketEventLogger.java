package com.yourcaryourway.chat.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Observabilite minimale : tracer les connexions et deconnexions WebSocket
 * facilite la demonstration et le diagnostic.
 */
@Component
public class WebSocketEventLogger {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventLogger.class);

    @EventListener
    public void onConnected(SessionConnectedEvent event) {
        log.info("Client WebSocket connecte");
    }

    @EventListener
    public void onDisconnected(SessionDisconnectEvent event) {
        log.info("Client WebSocket deconnecte (statut : {})", event.getCloseStatus());
    }
}
