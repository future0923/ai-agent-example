package io.github.future0923.ai.agent.example.etl.pipeline.service;

import io.github.future0923.ai.agent.example.etl.pipeline.EtlPipelineApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.SummaryMetadataEnricher;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 使用生成式人工智能模型为文档生成摘要并将其作为元数据添加。它可以为当前文档以及相邻文档（前一篇和下一篇）生成摘要。
 *
 * @author future0923
 */
public class SummaryMetadataEnricherTest extends EtlPipelineApplicationTest {

    @Autowired
    private ChatModel chatModel;

    @Test
    public void test() {
        SummaryMetadataEnricher enricher = new SummaryMetadataEnricher(
                // 用于生成摘要的AI模型
                chatModel,
                // SummaryType值的列表，指示要生成哪些摘要（上一个、当前、下一个）
                List.of(SummaryMetadataEnricher.SummaryType.PREVIOUS, SummaryMetadataEnricher.SummaryType.CURRENT, SummaryMetadataEnricher.SummaryType.NEXT),
                // 用于摘要生成的自定义模板（可选）。
                null,
                // 指定生成摘要时如何处理文档元信息（可选）。
                null
        );
        Document doc1 = new Document("Content of document 1");
        Document doc2 = new Document("Content of document 2");
        List<Document> enrichedDocs = enricher.apply(List.of(doc1, doc2));
        for (Document doc : enrichedDocs) {
            System.out.println("当前文件的摘要: " + doc.getMetadata().get("section_summary"));
            System.out.println("上一份文件的摘要: " + doc.getMetadata().get("prev_section_summary"));
            System.out.println("下一份文件的摘要: " + doc.getMetadata().get("next_section_summary"));
        }
        // 当前文件的摘要: I'm happy to help summarize the key topics and entities, but I need the actual content of the section to do so. Could you please provide the text or details from "Content of document 1"?
        // 上一份文件的摘要: null
        // 下一份文件的摘要: I apologize, but you've mentioned "Content of document 2" without providing the actual content. Could you please share the text or details from the section so that I can summarize the key topics and entities for you?
        // 当前文件的摘要: I apologize, but you've mentioned "Content of document 2" without providing the actual content. Could you please share the text or details from the section so that I can summarize the key topics and entities for you?
        // 上一份文件的摘要: I'm happy to help summarize the key topics and entities, but I need the actual content of the section to do so. Could you please provide the text or details from "Content of document 1"?
        // 下一份文件的摘要: null
    }
}
