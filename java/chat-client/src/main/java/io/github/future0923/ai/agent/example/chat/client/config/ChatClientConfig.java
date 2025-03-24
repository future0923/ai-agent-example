package io.github.future0923.ai.agent.example.chat.client.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * 配置
 *
 * @author future0923
 */
@Configuration
public class ChatClientConfig {

    /**
     * 通过ChatModel创建ChatClient，多模型时有用
     */
    public void chatClient(ChatModel chatModel) {
        // 创建 ChatClient.Builder
        ChatClient.Builder builder = ChatClient.builder(chatModel);
        // 创建 ChatClient
        ChatClient chatClient = ChatClient.create(chatModel);
    }

    /**
     * 通过ChatClient.Builder创建ChatClient
     */
    public void chatClient(ChatClient.Builder builder) {
        ChatClient client = builder.build();
    }

    /**
     * 聊天记忆
     */
    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    /**
     * 向量数据库
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * 初始化向量数据库
     */
    @Bean
    public CommandLineRunner ingestTermOfServiceToVectorStore(
            VectorStore vectorStore,
            @Value("classpath:rag/terms-of-service.txt") Resource resource) {
        return args -> vectorStore.write(new TokenTextSplitter().transform(new TextReader(resource).read()));
    }

}
