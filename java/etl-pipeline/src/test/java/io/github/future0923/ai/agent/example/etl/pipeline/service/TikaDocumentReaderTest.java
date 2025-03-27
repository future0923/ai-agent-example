package io.github.future0923.ai.agent.example.etl.pipeline.service;

import io.github.future0923.ai.agent.example.etl.pipeline.EtlPipelineApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * 使用Apache Tika从各种文档格式中提取文本，如PDF、DOC/DOCX、PPT/PPTX和超文本标记语言。有关支持格式的全面列表，详情可见Tika文档。
 *
 * @author future0923
 */
public class TikaDocumentReaderTest extends EtlPipelineApplicationTest {

    @Value("classpath:sample.docx")
    private Resource resource;

    @Test
    public void readText() {
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(this.resource);
        List<Document> documents = tikaDocumentReader.read();
        documents.forEach(System.out::println);
    }
}
