package io.github.future0923.ai.agent.example.etl.pipeline.service;

import io.github.future0923.ai.agent.example.etl.pipeline.EtlPipelineApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.DefaultContentFormatter;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.ContentFormatTransformer;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;

import java.util.List;
import java.util.Map;

/**
 * 使用CL100K_BASE编码根据Token计数将文本分割成块。
 *
 * @author future0923
 */
public class TokenTextSplitterTest extends EtlPipelineApplicationTest {

    @Test
    public void test() {
        Document doc1 = new Document("This is a long piece of text that needs to be split into smaller chunks for processing.",
                Map.of("source", "example.txt"));
        Document doc2 = new Document("Another document with content that will be split based on token count.",
                Map.of("source", "example2.txt"));
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                // 标记中每个文本块的目标大小
                .withChunkSize(800)
                // 每个文本块的极小点大小（以字符为单位）
                .withMinChunkSizeChars(350)
                // 要包含的块的极小点长度
                .withMinChunkLengthToEmbed(5)
                // 从文本生成的最大块数
                .withMaxNumChunks(10000)
                // 是否在块中保留分隔符（如换行符）
                .withKeepSeparator(true)
                .build();
        List<Document> splitDocuments = splitter.apply(List.of(doc1, doc2));
        for (Document doc : splitDocuments) {
            System.out.println("Chunk: " + doc.getText());
            System.out.println("Metadata: " + doc.getMetadata());
        }
        // DefaultContentFormatter 配置格式化内容
        ContentFormatTransformer transformer = new ContentFormatTransformer(DefaultContentFormatter.defaultConfig());
        List<Document> documentList = transformer.apply(splitDocuments);

    }
}
