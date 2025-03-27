package io.github.future0923.ai.agent.example.etl.pipeline.service;

import io.github.future0923.ai.agent.example.etl.pipeline.EtlPipelineApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 使用生成式人工智能模型从文档内容中提取关键词并将其添加为元数据
 *
 * @author future0923
 */
public class KeywordMetadataEnricherTest extends EtlPipelineApplicationTest {

    @Autowired
    private ChatModel chatModel;

    @Test
    public void test() {
        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(
                // 聊天模型
                chatModel,
                // 为每个文档提取的关键字数。
                5);
        // 文档内容
        Document doc = new Document("This is a document about artificial intelligence and its applications in modern technology.");
        // 提取关键字
        List<Document> enrichedDocs = enricher.apply(List.of(doc));
        Document enrichedDoc = enrichedDocs.get(0);
        // 查看返回的内容
        String keywords = (String) enrichedDoc.getMetadata().get("excerpt_keywords");
        // Extracted keywords: artificial intelligence, machine learning, automation, data analysis, intelligent systems
        System.out.println("Extracted keywords: " + keywords);
    }

}
