package io.github.future0923.ai.agent.example.etl.pipeline.service;

import io.github.future0923.ai.agent.example.etl.pipeline.EtlPipelineApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * 使用 Apache PdfBox 库解析PDF文档。
 * 使用PDF目录（例如TOC）信息将输入的PDF拆分成文本段落，并为每个段落输出一个单独的文档。注意：并非所有的PDF文档都包含PDF目录。
 *
 * @author future0923
 */
public class PagePdfDocumentReaderTest extends EtlPipelineApplicationTest {

    @Value("classpath:sample.pdf")
    private Resource resource;

    @Test
    public void readText() {
        PagePdfDocumentReader pdfDocumentReader = new PagePdfDocumentReader(
                resource,
                PdfDocumentReaderConfig.builder()
                        //  设置 页面顶部边距（0，表示不留边）
                        .withPageTopMargin(0)
                        //  设置 页面底部边距（0，表示不留边）
                        .withPageBottomMargin(0)
                        // 配置 文本提取格式：
                        .withPageExtractedTextFormatter(
                                ExtractedTextFormatter.builder()
                                        // 不删除任何顶部文本行。
                                        .withNumberOfTopTextLinesToDelete(0)
                                        // 还有很多配置
                                        .build())
                        // 每个 Document 只包含 1 页。
                        .withPagesPerDocument(1)
                        .build());
        List<Document> documents = pdfDocumentReader.read();
        documents.forEach(System.out::println);
    }
}
