package io.github.future0923.ai.agent.example.etl.pipeline.service;

import io.github.future0923.ai.agent.example.etl.pipeline.EtlPipelineApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * 处理Markdown文档，将它们转换为Document对象列表。
 *
 * @author future0923
 */
public class MarkdownDocumentReaderTest extends EtlPipelineApplicationTest {

    @Value("classpath:code.md")
    private Resource resource;

    @Test
    public void readText() {
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                // 当设置为true时，Markdown中的水平规则将创建新的Document对象。
                .withHorizontalRuleCreateDocument(true)
                // 当设置为true时，代码块将包含在与周围文本相同的Document中。当false时，代码块创建单独的Document对象。
                .withIncludeCodeBlock(false)
                // 当设置为true时，块引用将包含在与周围文本相同的Document中。当false时，块引用创建单独的Document对象。
                .withIncludeBlockquote(false)
                // 允许您向所有创建的Document对象添加自定义元信息。
                .withAdditionalMetadata("filename", "code.md")
                .build();
        MarkdownDocumentReader reader = new MarkdownDocumentReader(this.resource, config);
        List<Document> documents = reader.get();
        documents.forEach(System.out::println);
    }
}
