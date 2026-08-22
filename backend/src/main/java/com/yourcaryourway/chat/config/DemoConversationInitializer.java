package com.yourcaryourway.chat.config;

import com.yourcaryourway.chat.entity.Conversation;
import com.yourcaryourway.chat.repository.ConversationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Cree une conversation de demonstration au premier demarrage, afin que le
 * frontend soit immediatement utilisable (conversation n° 1) sans etape manuelle.
 * Simplification assumee du PoC.
 */
@Component
public class DemoConversationInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoConversationInitializer.class);

    private final ConversationRepository conversationRepository;

    public DemoConversationInitializer(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    @Override
    public void run(String... args) {
        if (conversationRepository.count() > 0) {
            return;
        }
        Conversation conversation = conversationRepository.save(new Conversation());
        log.info("Conversation de demonstration creee (id = {})", conversation.getId());
    }
}
