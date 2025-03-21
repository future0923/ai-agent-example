package io.github.future0923.ai.agent.example.embedding.models.service;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingOptions;
import io.github.future0923.ai.agent.example.embedding.models.AbstractEmbeddingModelApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 嵌入模型示例
 *
 * @author future0923
 */
public class EmbeddingModelTest extends AbstractEmbeddingModelApplicationTest {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Test
    public void embedString() {
        float[] embed = embeddingModel.embed("长春市长是张三");
        System.out.println(Arrays.toString(embed));
    }

    /**
     * 通过传入{@link Document}对象调用
     */
    @Test
    public void embedDocument() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("location", "长春");
        // 还有很多构造函数
        Document document = new Document("长春市长是张三", metadata);
        float[] embed = embeddingModel.embed(document);
        System.out.println(Arrays.toString(embed));
    }

    /**
     * 通过embedForResponse方式请求，可以获取到{@link EmbeddingResponse}返回数据信息
     */
    @Test
    public void embedForResponse() {
        EmbeddingResponse embeddingResponse = embeddingModel.embedForResponse(List.of("长春市长是张三", "宽城区区长是李四"));
        System.out.println(embeddingResponse);
        // 元数据
        System.out.println(embeddingResponse.getMetadata());
        // 输出float[]
        System.out.println(Arrays.toString(embeddingResponse.getResult().getOutput()));
    }

    /**
     * 通过call的方式，可以配置参数
     */
    @Test
    public void call() {
        // DashScopeEmbeddingOptions 实现了 EmbeddingOptions
        DashScopeEmbeddingOptions embeddingOptions = DashScopeEmbeddingOptions.builder()
                //.withModel("qwen-max")
                //.withTextType(null")
                //.withDimensions(null)
                .build();
        EmbeddingRequest embeddingRequest = new EmbeddingRequest(
                List.of("长春市长是张三", "宽城区区长是李四"),
                embeddingOptions
        );
        EmbeddingResponse embeddingResponse = embeddingModel.call(embeddingRequest);
        System.out.println(embeddingResponse);
    }

}
