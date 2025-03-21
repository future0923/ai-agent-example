# 嵌入模型(Embedding Model)

## 概念

[嵌入模型](../guide/concepts#embedding)（Embedding Model）是嵌入过程中采用的模型。当前 EmbeddingModel 的接口主要用于将文本转换为数值向量，接口的设计主要围绕这两个目标展开：

- 可移植性：该接口确保在各种嵌入模型之间的轻松适配。它允许开发者在不同的嵌入技术或模型之间切换，所需的代码更改最小化。这一设计与 Spring 模块化和互换性的理念一致。
- 简单性：嵌入模型简化了文本转换为嵌入的过程。通过提供如 `embed(String text)` 和 `embed(Document document)` 这样简单的方法，它去除了处理原始文本数据和嵌入算法的复杂性。这个设计选择使开发者，尤其是那些初次接触 AI 的开发者，更容易在他们的应用程序中使用嵌入，而无需深入了解其底层机制。

演示 [代码](https://github.com/future0923/ai-agent-example/tree/main/java/embedding-models/src/test/java/io/github/future0923/ai/agent/example/embedding/models/service) 为 [SpringAiAlibaba](https://github.com/alibaba/spring-ai-alibaba) 对 SpringAi 的实现.

## API

### EmbeddingModel

EmbeddingModel提供多种方法将文本转换为Embeddings，支持单个字符串、结构化的Document对象或文本批处理。

通常，Embedding返回一个 `float[]`，以数值向量格式表示Embeddings。

`call()` 和 ``embedForResponse()` 方法提供了更全面的输出，可能包括有关Embeddings的其他信息。

`dimensions()` 方法是开发人员快速确定 Embedding 向量大小的便利工具，这对于理解 Embedding space 和后续处理步骤非常重要。

```java
public interface EmbeddingModel extends Model<EmbeddingRequest, EmbeddingResponse> {
    /**
     * EmbeddingRequest 调用嵌入模型得到 EmbeddingResponse响应
     */
    @Override
    EmbeddingResponse call(EmbeddingRequest request);

    /**
     * 将 String 转换为 Embedding
     */
    default float[] embed(String text) {
		
    }

    /**
     * 将 Document 转换为 Embedding
     */
    float[] embed(Document document);

    /***
     * 将 List<String> 转换为 Embedding
     */
    default List<float[]> embed(List<String> texts) {
		
    }

    /**
     * 将 List<Document> 转换为 Embedding
     * EmbeddingOptions 可以传入参数
     * BatchingStrategy 为批量向量化时的策略
     */
    default List<float[]> embed(List<Document> documents, EmbeddingOptions options, BatchingStrategy batchingStrategy) {
		
    }

    /**
     * 将 List<String> 转换为 Embedding，返回更灵活的EmbeddingResponse
     */
    default EmbeddingResponse embedForResponse(List<String> texts) {
		
    }

    /**
     * 获取Embedding向量大小
     */
    default int dimensions() {
		
    }

}
```

### EmbeddingOptions

Embedding模型请求时可以通过实现EmbeddingOptions携带参数并扩展更多的参数。getDimensions返回的向量的维度数参数非常重要。

```java
public interface EmbeddingOptions extends ModelOptions {

    @Nullable
    String getModel();

    /**
     * 向量的维度数，即每个生成的向量有多少个数值组成。
     */
    @Nullable
    Integer getDimensions();

}
```

**什么是 dimensions？**
- dimensions 指定了生成的嵌入向量的大小（向量的维度数）。
- 举个例子，如果设置 dimensions = 128，那么每个文本（或其他输入）将被转化为一个包含 128 个浮点数的向量。

**为什么需要设置 dimensions？**
- 维度越高，理论上可以承载更丰富的语义信息，但也更占用内存和计算资源。
- 维度越低，效率更高，但可能损失一些表达能力。

**设置多少合适？**

| 使用场景             | 推荐维度                                     |
|------------------|------------------------------------------|
| 简单文本搜索 / 分类      | 128 / 256                                |
| 高性能语义搜索          | 512 / 768                                |
| 精准匹配 / 多语言支持     | 1024+                                    |
| OpenAI Embedding | 固定维度，比如 text-embedding-ada-002 输出 1536 维 |


### EmbeddingRequest

EmbeddingRequest 是一种 ModelRequest，它接受文本对象列表和可选的Embedding请求选项。以下代码片段简要地显示了 EmbeddingRequest 类，省略了构造函数和其他工具方法：

```java
public class EmbeddingRequest implements ModelRequest<List<String>> {

    /**
     * 要向量化的字符串集合
     */
    private final List<String> inputs;

    /**
     * 参数
     */
    private final EmbeddingOptions options;
    
    // other methods omitted
}
```

### EmbeddingResponse

EmbeddingResponse类保存了AI模型的输出，其中每个 Embedding 实例包含来自单个文本输入的结果向量数据。同时，它还携带了有关 AI 模型响应的EmbeddingResponseMetadata元数据。

```java
public class EmbeddingResponse implements ModelResponse<Embedding> {

    /**
     * 向量化后的 Embedding 数据集合
     */
    private List<Embedding> embeddings;

    /**
     * 元数据信息
     */
    private EmbeddingResponseMetadata metadata = new EmbeddingResponseMetadata();
    
    // other methods omitted
}
```

### Embedding

每个 Embedding 类都表示一个向量。

```java
public class Embedding implements ModelResult<List<Double>> {

    /**
     * 向量float数据集合
     */
    private List<Double> embedding;

    /**
     * 索引
     */
    private Integer index;

    /**
     * 元数据信息
     */
    private EmbeddingResultMetadata metadata;

}
```

## 示例

```java
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

    /**
     * SpringAiAlibaba已经自动注入了嵌入模型DashScopeEmbeddingModel
     */
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
                .withModel("text-embedding-v1")
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
```