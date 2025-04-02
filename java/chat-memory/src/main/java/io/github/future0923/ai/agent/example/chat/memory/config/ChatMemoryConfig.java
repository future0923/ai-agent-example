package io.github.future0923.ai.agent.example.chat.memory.config;

import com.alibaba.cloud.ai.memory.jdbc.MysqlChatMemory;
import com.alibaba.cloud.ai.memory.redis.RedisChatMemory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 聊天记忆配置
 *
 * @author future0923
 */
@Configuration
public class ChatMemoryConfig {

    /**
     * 内存聊天记忆
     */
    @Bean
    public ChatMemory memoryChatMemory() {
        return new InMemoryChatMemory();
    }

    /**
     * MySQL聊天记忆
     */
    @Bean
    public ChatMemory mysqlChatMemory() {
        return new MysqlChatMemory("root", "123456", "jdbc:mysql://127.0.0.1:3306/chat_memory");
    }

    /**
     * Redis聊天记忆
     */
    @Bean
    public ChatMemory redisChatMemory() {
        return new RedisChatMemory("127.0.0.1", 6379, "123456");
    }
}
