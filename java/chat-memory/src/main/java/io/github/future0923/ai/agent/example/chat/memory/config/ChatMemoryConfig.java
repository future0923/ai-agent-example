package io.github.future0923.ai.agent.example.chat.memory.config;

import com.alibaba.cloud.ai.memory.mysql.MysqlChatMemory;
import com.alibaba.cloud.ai.memory.redis.RedisChatMemory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {

    @Bean
    public ChatMemory memoryChatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    public ChatMemory mysqlChatMemory() {
        return new MysqlChatMemory("root", "123456", "127.0.0.1:3306");
    }

    @Bean
    public ChatMemory redisChatMemory() {
        return new RedisChatMemory("127.0.0.1", 6379, "123456");
    }
}
