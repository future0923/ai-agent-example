package io.github.future0923.ai.agent.example.flight.booking.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author future0923
 */
@Configuration
public class Config {

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }
}
