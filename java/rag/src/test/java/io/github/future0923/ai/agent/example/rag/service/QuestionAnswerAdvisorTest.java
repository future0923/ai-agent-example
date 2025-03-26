package io.github.future0923.ai.agent.example.rag.service;

import io.github.future0923.ai.agent.example.rag.RagApplicationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

/**
 * @author future0923
 */
public class QuestionAnswerAdvisorTest extends RagApplicationTest {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private ChatClient.Builder builder;

    private VectorStore vectorStore;

    @BeforeEach
    public void vectorStore() {
        vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        // 生成一个机器人产品说明书的文档
        List<Document> documents = List.of(
                new Document("""
                        产品说明书:产品名称：智能机器人。
                        产品描述：智能机器人是一个智能设备，能够自动完成各种任务。
                        功能：
                        1. 自动导航：机器人能够自动导航到指定位置。
                        2. 自动抓取：机器人能够自动抓取物品。
                        3. 自动放置：机器人能够自动放置物品。
                        """,
                        Map.of("type", "robot"))
        );
        vectorStore.add(documents);
    }

    @Test
    public void use() {
        ChatClient chatClient = builder.build();
        Flux<String> flux = chatClient.prompt()
                .user("智能机器人能做什么")
                .advisors(new QuestionAnswerAdvisor(
                        vectorStore,
                        SearchRequest.builder()
                                .filterExpression("type == 'robot'")
                                .build()))
                .stream()
                .content();
        StepVerifier.create(flux)
                .thenConsumeWhile(res -> {
                    System.out.print(res);
                    return true;
                })
                .verifyComplete();
    }
}
