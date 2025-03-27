package io.github.future0923.ai.agent.example.etl.pipeline.service;

import io.github.future0923.ai.agent.example.etl.pipeline.EtlPipelineApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * @author future0923
 */
public class TextReaderTest extends EtlPipelineApplicationTest {

    @Value("classpath:text-source.txt")
    private Resource resource;

    @Test
    public void readText() {
        // 创建 TextReader
        TextReader textReader = new TextReader(this.resource);
        // 设置元数据
        textReader.getCustomMetadata().put("filename", "text-source.txt");
        // 读取文档
        List<Document> documents = textReader.read();
        // 切割文档
        List<Document> splitter = new TokenTextSplitter().apply(documents);
        splitter.forEach(System.out::println);
    }
}
