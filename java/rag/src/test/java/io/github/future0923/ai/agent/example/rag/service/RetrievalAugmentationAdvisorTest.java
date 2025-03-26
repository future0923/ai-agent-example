package io.github.future0923.ai.agent.example.rag.service;

import io.github.future0923.ai.agent.example.rag.RagApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author future0923
 */
public class RetrievalAugmentationAdvisorTest extends RagApplicationTest {

    @Autowired
    private ChatClient.Builder builder;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Test
    public void test() {
        // 1. 初始化向量存储
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel)
                .build();
        // 2. 添加文档到向量存储
        List<Document> documents = List.of(
                new Document("产品说明书:产品名称：智能机器人\n" +
                        "产品描述：智能机器人是一个智能设备，能够自动完成各种任务。\n" +
                        "功能：\n" +
                        "1. 自动导航：机器人能够自动导航到指定位置。\n" +
                        "2. 自动抓取：机器人能够自动抓取物品。\n" +
                        "3. 自动放置：机器人能够自动放置物品。\n"));
        vectorStore.add(documents);
        Advisor advisor = RetrievalAugmentationAdvisor.builder()
                // 配置查询增强器
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        // 允许空上下文查询
                        .allowEmptyContext(true)
                        .build())
                // 配置文档检索器
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        // 返回相似度高于此值的文档。范围从0到1的双精度值，其中接近1的值表示较高的相似度。
                        .similarityThreshold(0.5)
                        // 一个整数，指定要返回的相似文档的最大数量。这通常被称为“顶部K”搜索，或“K近邻算法”。
                        .topK(3)
                        //.filterExpression()
                        .build())
                // 等等
                .build();
        String content = builder.build()
                .prompt()
                .user("智能机器人是什么")
                .advisors(advisor)
                .call()
                .content();
        System.out.println(content);
    }

}
