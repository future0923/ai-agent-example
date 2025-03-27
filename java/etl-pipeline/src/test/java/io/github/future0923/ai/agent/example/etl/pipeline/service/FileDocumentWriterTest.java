package io.github.future0923.ai.agent.example.etl.pipeline.service;

import io.github.future0923.ai.agent.example.etl.pipeline.EtlPipelineApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.writer.FileDocumentWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * Document写入文件
 *
 * @author future0923
 */
public class FileDocumentWriterTest extends EtlPipelineApplicationTest {

    @Value("classpath:text-source.txt")
    private Resource resource;

    @Test
    public void test() {
        // 创建 TextReader
        TextReader textReader = new TextReader(this.resource);
        // 设置元数据
        textReader.getCustomMetadata().put("filename", "text-source.txt");
        // 读取文档
        List<Document> documents = textReader.read();
        // 切割文档
        List<Document> splitter = new TokenTextSplitter().apply(documents);
        FileDocumentWriter fileDocumentWriter = new FileDocumentWriter(
                // 要写入文档的文件的名称。
                "output.txt",
                // 是否在输出中包含文档标记（默认值：false）
                true,
                // 指定要写入文件的文档内容（默认值：MetadataMode. NONE）。
                // ALL（全部）
                //  该模式表示所有元数据都会被存储，包括用户提供的元数据和从文档内容中推断出的元数据。
                //  适用于需要完整信息以便后续分析或搜索的场景。
                // EMBED（嵌入）
                //  该模式表示元数据会被嵌入到文件或文档的内容中，而不是单独存储。
                //  适用于希望元数据与文档一起传输或处理的情况，例如在 JSON、PDF、Markdown 等格式中嵌入元数据。
                // INFERENCE（推理）
                //  该模式表示系统会根据文档内容自动推断元数据，而不会存储用户提供的元数据。
                //  适用于希望利用 AI 或 NLP 技术自动生成标签、分类、关键词等信息的场景，而不依赖手动提供的元数据。
                // NONE（无）
                //  该模式表示不会存储任何元数据，既不保存用户提供的元数据，也不进行推理。
                //  适用于对元数据不关心或希望减少存储空间的场景。
                MetadataMode.ALL,
                // 如果为true，数据将写入文件的末尾而不是开头（默认值：false）
                false
        );
        fileDocumentWriter.write(splitter);
    }
}
