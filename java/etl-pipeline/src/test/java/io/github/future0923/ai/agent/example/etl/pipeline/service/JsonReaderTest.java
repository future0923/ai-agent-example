package io.github.future0923.ai.agent.example.etl.pipeline.service;

import io.github.future0923.ai.agent.example.etl.pipeline.EtlPipelineApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.JsonReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * 处理JSON文档，将它们转换为Document对象列表。
 *
 * @author future0923
 */
public class JsonReaderTest extends EtlPipelineApplicationTest {

    @Value("classpath:bikes.json")
    private Resource resource;

    @Test
    public void readJson() {
        // resource 资源文件
        // jsonKeysToUse JSON中的键数组，可用作结果Document对象中的文本内容。
        // jsonMetadataGenerator 可选JsonMetadataGenerator为每个Document创建元信息。
        JsonReader jsonReader = new JsonReader(this.resource, "description", "content");
        List<Document> documents = jsonReader.get();
        documents.forEach(System.out::println);
        // 基于上面提取之后的数据继续通过 pointer 获取
        List<Document> documentList = jsonReader.get("/0");
        documentList.forEach(System.out::println);
    }
}
