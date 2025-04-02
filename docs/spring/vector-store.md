# 向量存储(Vector Store)

## 核心概念

向量存储（VectorStore）是一种用于存储和检索高维向量数据的数据库或存储解决方案，它特别适用于处理那些经过嵌入模型转化后的数据。在 VectorStore 中，查询与传统关系数据库不同。它们执行相似性搜索，而不是精确匹配。当给定一个向量作为查询时，VectorStore 返回与查询向量“相似”的向量。

VectorStore 用于将您的数据与 AI 模型集成。在使用它们时的第一步是将您的数据加载到矢量数据库中。然后，当要将用户查询发送到 AI 模型时，首先检索一组相似文档。然后，这些文档作为用户问题的上下文，并与用户的查询一起发送到 AI 模型。这种技术被称为检索增强生成（Retrieval Augmented Generation，RAG）。

## API

演示[代码](https://github.com/future0923/ai-agent-example/blob/main/java/vector-store/vector-store-spring-ai/vector-store-spring-ai-memory)

### VectorStore

Spring AI提供了一个抽象的API，用于通过 **VectorStore** 接口与向量数据库进行交互。

- [添加](#write-vector-store)文档到向量数据库
- [删除](#delete-document)向量数据库中的内容。
- [查询](#metadata-filter)向量数据库中的内容。

```java
package org.springframework.ai.vectorstore;

import java.util.List;
import java.util.Optional;

import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentWriter;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.observation.DefaultVectorStoreObservationConvention;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public interface VectorStore extends DocumentWriter {

    /**
     * 向量存储名称
     */
	default String getName() {
		
	}

	/**
     * 添加 Document 到向量数据库
	 */
	void add(List<Document> documents);

    /**
     * 添加 Document 到向量数据库
     */
	@Override
	default void accept(List<Document> documents) {
		add(documents);
	}

    /**
     * 删除向量数据库中的内容。
     * 通过文档id删除
     */
	void delete(List<String> idList);

    /**
     * 删除向量数据库中的内容。
     * 通过元数据过滤当时删除
     */
	void delete(Filter.Expression filterExpression);

    /**
     * 删除向量数据库中的内容。
     * * 通过元数据过滤当时删除
     */
	default void delete(String filterExpression) {
		
	}

	/**
	 * 从向量数据库中查询Document
	 */
	@Nullable
	List<Document> similaritySearch(SearchRequest request);

    /**
     * 从向量数据库中查询Document
     */
	@Nullable
	default List<Document> similaritySearch(String query) {
		return this.similaritySearch(SearchRequest.builder().query(query).build());
	}

}
```

向量存储只存储向量化后的 `float[]`，一般需要配合 [EmbeddingModel](embedding-model) 进行使用。

写入向量[使用示例](#write-vector-store).

查询使用 SearchRequest 构建请求，并可以对元数据进行筛选。

```java
public class SearchRequest {

	public static final double SIMILARITY_THRESHOLD_ACCEPT_ALL = 0.0;

	public static final int DEFAULT_TOP_K = 4;

	private String query = "";

	private int topK = DEFAULT_TOP_K;

	private double similarityThreshold = SIMILARITY_THRESHOLD_ACCEPT_ALL;

	@Nullable
	private Filter.Expression filterExpression;

    public static Builder from(SearchRequest originalSearchRequest) {
		return builder().query(originalSearchRequest.getQuery())
			.topK(originalSearchRequest.getTopK())
			.similarityThreshold(originalSearchRequest.getSimilarityThreshold())
			.filterExpression(originalSearchRequest.getFilterExpression());
	}

	public static class Builder {

		private final SearchRequest searchRequest = new SearchRequest();

		public Builder query(String query) {
			Assert.notNull(query, "Query can not be null.");
			this.searchRequest.query = query;
			return this;
		}

		public Builder topK(int topK) {
			Assert.isTrue(topK >= 0, "TopK should be positive.");
			this.searchRequest.topK = topK;
			return this;
		}

		public Builder similarityThreshold(double threshold) {
			Assert.isTrue(threshold >= 0 && threshold <= 1, "Similarity threshold must be in [0,1] range.");
			this.searchRequest.similarityThreshold = threshold;
			return this;
		}

		public Builder similarityThresholdAll() {
			this.searchRequest.similarityThreshold = 0.0;
			return this;
		}

		public Builder filterExpression(@Nullable Filter.Expression expression) {
			this.searchRequest.filterExpression = expression;
			return this;
		}

		public Builder filterExpression(@Nullable String textExpression) {
			this.searchRequest.filterExpression = (textExpression != null)
					? new FilterExpressionTextParser().parse(textExpression) : null;
			return this;
		}

		public SearchRequest build() {
			return this.searchRequest;
		}

	}

	public String getQuery() {...}
	public int getTopK() {...}
	public double getSimilarityThreshold() {...}
	public Filter.Expression getFilterExpression() {...}
}
```

| 参数                       | 含义                                                                  |
|--------------------------|---------------------------------------------------------------------|
| query()                  | 查询内容                                                                |
| topK()                   | 一个整数，指定要返回的相似文档的最大数量。这通常被称为“顶部K”搜索，或“K近邻算法”。                        |
| similarityThreshold()    | 返回相似度高于此值的文档。范围从0到1的双精度值，其中接近1的值表示较高的相似度。                           |
| similarityThresholdAll() | 设置为 0.0 则表示所有                                                       |
| filterExpression()       | 基于ANTLR4的外部DSL，它接受滤波器表达式作为字符串，对文档元数据进行过滤。[点击了解详情](#metadata-filter) |

SearchRequest[使用示例](#metadata-filter).

### SimpleVectorStore

Spring AI 提供 `SimpleVectorStore` 实现，它使用内存存储向量。

### BatchingStrategy

在使用向量存储时，通常需要嵌入大量文档。嵌入模型将文本作为标记处理，并具有最大Token限制，通常称为上下文窗口大小。此限制限制了单个嵌入请求中可以处理的文本量。尝试在一次调用中嵌入太多标记可能会导致错误或截断嵌入。

为了解决这个Token限制，Spring AI实施了批处理策略。这种方法将大量文档分解为适合嵌入模型最大上下文窗口的较小批处理。批处理不仅解决了Token限制问题，还可以提高性能和更有效地使用API速率限制。

Spring AI通过 `BatchingStrategy` 接口提供此功能，该接口允许根据Token计数分批次处理文档。

```java
public interface BatchingStrategy {
    List<List<Document>> batch(List<Document> documents);
}
```

在 [EmbeddingModel](embedding-model) 调用 `embed()` 方法进行向量化时，会调用 `batchingStrategy.batch(documents)` 进行分批处理。

```java
public interface EmbeddingModel extends Model<EmbeddingRequest, EmbeddingResponse> {
    
    // 更多代码

    default List<float[]> embed(List<Document> documents, EmbeddingOptions options, BatchingStrategy batchingStrategy) {
        Assert.notNull(documents, "Documents must not be null");
        List<float[]> embeddings = new ArrayList<>(documents.size());
        List<List<Document>> batch = batchingStrategy.batch(documents); // [!code focus]
        for (List<Document> subBatch : batch) {
            List<String> texts = subBatch.stream().map(Document::getText).toList();
            EmbeddingRequest request = new EmbeddingRequest(texts, options);
            EmbeddingResponse response = this.call(request);
            for (int i = 0; i < subBatch.size(); i++) {
                embeddings.add(response.getResults().get(i).getOutput());
            }
        }
        Assert.isTrue(embeddings.size() == documents.size(),
                "Embeddings must have the same number as that of the documents");
        return embeddings;
    }

    // 更多代码
}
```

Spring AI提供了一个名为 `TokenCountBatchingStrategy` 的默认实现，该策略根据文档的 Token 计数对其进行批量处理，确保每个批量不超过计算的最大输入 Token 计数。

- 使用OpenAI的最大输入代币计数（8191）作为默认上限。
- 包含保留百分比（默认为10%）以为潜在开销提供缓冲区。
- 计算实际最大输入代币计数为：`actualMaxInputTokenCount = originalMaxInputTokenCount * (1 - RESERVE_PERCENTAGE)`

该策略估计每个文档的代币计数，在不超过最大输入代币计数的情况下将它们分组，如果单个文档超过此限制，则抛出异常。


可以自定义TokenCountBatchingStrategy以更好地满足您的特定要求。这可以通过在Spring Boot@Configuration类中使用自定义参数创建新实例来完成。

```java
@Configuration
public class EmbeddingConfig {
    @Bean
    public BatchingStrategy customTokenCountBatchingStrategy() {
        return new TokenCountBatchingStrategy(
            // 指定编码类型
            EncodingType.CL100K_BASE,
            // 最大的token输入
            8000,             
            //设置预留百分比
            0.1           
        );
    }
}
```

## 写入向量存储{#write-vector-store}

一般使用[ETL提供的DocumentReader](etl-pipeline#document-reader)可以对Document进行读取，实现[ETL提供的DocumentTransformer](etl-pipeline#document-transformer)对Document进行转换，将提取到的数据转换为特定的格式后写入。

```java
@Value("classpath:rag/terms-of-service.txt")
private Resource resource;

/**
 * 内存向量数据库
 */
@Bean
public VectorStore vectorStore(EmbeddingModel embeddingModel) {
    return SimpleVectorStore.builder(embeddingModel).build();
}

@Test
public void add() {
    // TokenTextSplitter 类型的 ELT DocumentTransformer 转换 TextReader 读取到 ELT Document
    vectorStore.add(new TokenTextSplitter().transform(new TextReader(resource).read()));
}
```

## 元数据过滤{#metadata-filter}

您可以将类似SQL的滤波器表达式作为String传递给similaritySearch重载之一。

考虑以下示例：
- `"country == 'BG'"`
- `"genre == 'drama' && year >= 2020"`
- `"genre in ['comedy', 'documentary', 'drama']"`
- Filter.Expression
- Filter.ExpressionBuilder
- FilterExpressionTextParser

```java
/**
 * 使用SearchRequest查询，提供查询内容并指定元数据筛选。
 * 如果 Document.getMetadata() 如下格式：
 * <pre>{@code
 * "country": <Text>,
 * "city": <Text>,
 * "year": <Number>,
 * "price": <Decimal>,
 * "isActive": <Boolean>
 * }</pre>
 */
@Test
public void searchRequest() {
    FilterExpressionBuilder builder = new FilterExpressionBuilder();
    SearchRequest searchRequest = SearchRequest
            .builder()
            // 查询内容
            .query("退订")
            // 一个整数，指定要返回的相似文档的最大数量。这通常被称为“顶部K”搜索，或“K近邻算法”。
            .topK(SearchRequest.DEFAULT_TOP_K)
            // 返回相似度高于此值的文档。范围从0到1的双精度值，其中接近1的值表示较高的相似度。
            .similarityThreshold(SearchRequest.SIMILARITY_THRESHOLD_ACCEPT_ALL)
            // 设置为 0.0 则表示所有
            .similarityThresholdAll()
            // 基于ANTLR4的外部DSL，它接受滤波器表达式作为字符串。
            .filterExpression("""
                    country == 'UK' && year >= 2020 && isActive == true
                    Or
                    country == 'BG' && (city NOT IN ['Sofia', 'Plovdiv'] || price < 134.34)
                    """)
            .filterExpression(new Filter.Expression(Filter.ExpressionType.AND,
                    new Filter.Expression(Filter.ExpressionType.EQ, new Filter.Key("country"), new Filter.Value("UK")),
                    new Filter.Expression(Filter.ExpressionType.AND,
                            new Filter.Expression(Filter.ExpressionType.GTE, new Filter.Key("year"), new Filter.Value(2020)),
                            new Filter.Expression(Filter.ExpressionType.EQ, new Filter.Key("isActive"), new Filter.Value(true)))))
            .filterExpression(builder.and(
                    builder.eq("country", "UK"),
                    builder.and(
                            builder.gte("year", 2020),
                            builder.eq("isActive", true)
                    )
            ).build())
            .filterExpression(new FilterExpressionTextParser().parse("country == 'UK' && isActive == true && year >=2020"))
            .build();
    List<Document> documents = vectorStore.similaritySearch(searchRequest);
    documents.forEach(System.out::println);
}
```

## 删除文档{#delete-document}

通过文档id删除

```java
@Test
public void delete() {
    // 增加文档并设置元数据
    Document document = new Document("世界真大啊", Map.of("country", "中国"));
    vectorStore.add(List.of(document));
    // 通过id删除
    vectorStore.delete(List.of(document.getId()));
}
```

使用[过滤器](#metadata-filter)删除

```java
@Test
public void deleteFilter() {
    // 增加文档并设置元数据
    Document bgDocument = new Document("世界真大啊", Map.of("country", "中国"));
    // 增加文档并设置元数据
    Document nlDocument = new Document("世界真大啊", Map.of("country", "中华人民共和国"));
    // 存储
    vectorStore.add(List.of(bgDocument, nlDocument));
    Filter.Expression filterExpression = new Filter.Expression(
            Filter.ExpressionType.EQ,
            new Filter.Key("country"),
            new Filter.Value("中国")
    );
    // 删除
    vectorStore.delete(filterExpression);
    // 验证
    SearchRequest request = SearchRequest.builder()
            .query("世界")
            .filterExpression("country == '中国'")
            .build();
    List<Document> results = vectorStore.similaritySearch(request);
    results.forEach(System.out::println);
}
```

## 其他实现

这里只使用的内存向量存储，
- Spring AI 提供了很多[向量存储](https://docs.spring.io/spring-ai/reference/1.0/api/vectordbs.html)的实现。
- Spring AI Alibaba 社区也提供了很多，[源码](https://github.com/alibaba/spring-ai-alibaba/tree/main/community/vector-stores)。

## 示例源码

- 向量存储[spring-ai-memory](https://github.com/future0923/ai-agent-example/tree/main/java/vector-store/vector-store-spring-ai/vector-store-spring-ai-memory)
- 向量存储[spring-ai-milvus](https://github.com/future0923/ai-agent-example/tree/main/java/vector-store/vector-store-spring-ai/vector-store-spring-ai-milvus)