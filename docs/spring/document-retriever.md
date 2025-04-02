# 文档检索 (Document Retriever)

演示[代码](https://github.com/future0923/ai-agent-example/blob/main/java/document-retriever)

## 核心概念

文档检索（DocumentRetriever）是一种信息检索技术，旨在从大量未结构化或半结构化文档中快速找到与特定查询相关的文档或信息。文档检索通常以在线(online)方式运行。

DocumentRetriever通常基于[向量](../guide/concepts#embedding)搜索。它将用户的查询问题(query)转化为[Embeddings](../guide/concepts#embedding)后，在存储文档中进行相似性搜索，返回相关的片段。

片段的用途之一是作为提示词(prompt)的一部分，发送给大模型(LLM)汇总处理后，作为答案呈现给用户。

## API

Spring AI 提供了 **DocumentRetriever** 相关的 API 让开发者使用自定义的检索系统快速的查找定位。

### DocumentRetriever

DocumentRetriever 文档检索接口。检索器，根据 QueryExpander 使用不同的数据源进行检索，例如 搜索引擎、向量存储、数据库或知识图等；

```java
package org.springframework.ai.rag.retrieval.search;

import java.util.List;
import java.util.function.Function;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;

public interface DocumentRetriever extends Function<Query, List<Document>> {

    /**
     * 根据给定的查询从数据源中检索相关文档。
     * 
     * @param query 查询内容
     * @return 返回相关的每个Document集合
     */
	List<Document> retrieve(Query query);

	default List<Document> apply(Query query) {
		return retrieve(query);
	}

}
```

### Query

一次查询请求的参数。可以以设置查询文本、历史消息、上下文等。

```java
public record Query(String text, List<Message> history, Map<String, Object> context) {
    
}
```

### Document

Document 是一个包含文本和元数据的对象。表示一个文档片段，它包含一个文本内容，以及一个或多个元数据。

```java
@JsonIgnoreProperties({ "contentFormatter", "embedding" })
public class Document {

	/**
	 * 唯一id
	 */
	private final String id;

	/**
	 * 文档内容
	 */
	private final String text;

	/**
	 * 媒体信息
	 */
	private final Media media;

	/**
	 * 文档的元数据信息
	 */
	private final Map<String, Object> metadata;

    /**
     * 得分
     */
	@Nullable
	private final Double score;
    
    // 更多
}
```

### DocumentRanker

DocumentRanker 文档重排序接口。根据 Document 和用户 query 的相关性对 Document 进行排序和排名

```java
package org.springframework.ai.rag.postretrieval.ranking;

import java.util.List;
import java.util.function.BiFunction;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;

public interface DocumentRanker extends BiFunction<Query, List<Document>, List<Document>> {

    /**
    * 根据 Document 和用户 query 的相关性对 Document 进行排序和排名
    */
	List<Document> rank(Query query, List<Document> documents);

	default List<Document> apply(Query query, List<Document> documents) {
		return rank(query, documents);
	}

}
```

## 示例

演技基于 Spring AI Alibaba 集成的 阿里云百炼平台。DashScopeDocumentRetriever 为百炼实现的 DocumentRetriever。

```java
package com.alibaba.cloud.ai.dashscope.rag;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.common.DashScopeException;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author nuocheng.lxm
 * @since 2024/8/5 14:42
 */
public class DashScopeDocumentRetriever implements DocumentRetriever {

	private final DashScopeDocumentRetrieverOptions options;

	private final DashScopeApi dashScopeApi;

	public DashScopeDocumentRetriever(DashScopeApi dashScopeApi, DashScopeDocumentRetrieverOptions options) {
		Assert.notNull(options, "RetrieverOptions must not be null");
		Assert.notNull(options.getIndexName(), "IndexName must not be null");
		this.options = options;
		this.dashScopeApi = dashScopeApi;
	}

	@Override
	public List<Document> retrieve(Query query) {
		String pipelineId = dashScopeApi.getPipelineIdByName(options.getIndexName());
		if (pipelineId == null) {
			throw new DashScopeException("Index:" + options.getIndexName() + " NotExist");
		}
        return dashScopeApi.retriever(pipelineId, query.text(), options);
	}

}
```

DashScopeDocumentRetrieverOptions 文档检索参数选项

| 参数                   | 含义                                           | 默认值（不同版本可能不一样）         |
|----------------------|----------------------------------------------|------------------------|
| indexName            | 知识库的索引名                                      | -                      |
| denseSimilarityTopK  | 密集相似度前K个 ，指定了在基于向量的（即密集）相似度计算中，返回最相关的前多少个结果。 | 100                    |
| sparseSimilarityTopK | 稀疏相似度前K个 ， 针对的是基于关键词匹配（即稀疏）的相似度计算。           | 100                    |
| enableRewrite        | 启用重写  ，查询重写可以帮助改进或优化用户的查询以获得更好的检索结果。         | false                  |
| rewriteModelName     | 重写模型名称                                       | conv-rewrite-qwen-1.8b |
| enableReranking      | 启用重新排序 ，是否启用结果重新排序。重新排序可以用来提升检索结果的相关性。       | true                   |
| rerankModelName      | 重新排序模型名称                                     | gte-rerank-hybrid      |
| rerankMinScore       | 重新排序最小分数  ，当进行结果重新排序时，只有得分高于这个阈值的结果才会被保留。    | 0.01f                  |
| rerankTopN           | 重新排序前N个，重新排序后返回的前N个最佳结果                      | 5                      |

```java
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import com.alibaba.cloud.ai.model.RerankModel;
import io.github.future0923.ai.agent.example.document.retriever.DocumentRetrieverApplicationTest;
import io.github.future0923.ai.agent.example.document.retriever.document.ranker.DashScopeDocumentRanker;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;

public class DocumentRetrieverTest extends DocumentRetrieverApplicationTest {

  @Value("${spring.ai.dashscope.api-key}")
  private String apiKey;

  @Autowired
  private RerankModel rerankModel;

  @Test
  public void test() {
    DocumentRetriever documentRetriever = new DashScopeDocumentRetriever(
            new DashScopeApi(apiKey),
            DashScopeDocumentRetrieverOptions.builder()
                    // 知识库的索引名
                    .withIndexName("二手房信息")
                    // 密集相似度前K个 ，指定了在基于向量的（即密集）相似度计算中，返回最相关的前多少个结果。
                    .withDenseSimilarityTopK(10)
                    // 稀疏相似度前K个 ， 针对的是基于关键词匹配（即稀疏）的相似度计算。
                    .withSparseSimilarityTopK(10)
                    // 启用重写 ，查询重写可以帮助改进或优化用户的查询以获得更好的检索结果。
                    .withEnableRewrite(true)
                    // 重写模型名称
                    .withRewriteModelName("conv-rewrite-qwen-1.8b")
                    // 启用重新排序 ，是否启用结果重新排序。重新排序可以用来提升检索结果的相关性。
                    .withEnableReranking(true)
                    // 重新排序模型名称
                    .withRerankModelName("gte-rerank-hybrid")
                    // 重新排序最小分数 ，当进行结果重新排序时，只有得分高于这个阈值的结果才会被保留。
                    .withRerankMinScore(0.01f)
                    // 重新排序前N个，重新排序后返回的前N个最佳结果
                    .withRerankTopN(1)
                    .build());
    Query query = Query.builder()
            // 检索的内容
            .text("我想找一个君悦豪庭B区1室的房源，面积在60平左右")
            // 历史消息
            .history(List.of())
            // 上下文信息
            .context(Map.of())
            .build();
    List<Document> result = documentRetriever.retrieve(query);
    // 重排序
    DashScopeDocumentRanker dashScopeDocumentRanker = new DashScopeDocumentRanker(rerankModel);
    result = dashScopeDocumentRanker.rank(query, result);
    Document document = result.get(0);
    // 获取元数据信息（提取到的key -> value）
    Map<String, Object> metadata = document.getMetadata();
    // MetaData格式化好的数据内容
    String formattedContent = document.getFormattedContent();
    // 搜索到的文档内容
    String text = document.getText();
  }
}
```

DashScopeDocumentRanker 重排序

```java

import com.alibaba.cloud.ai.dashscope.rerank.DashScopeRerankOptions;
import com.alibaba.cloud.ai.model.RerankModel;
import com.alibaba.cloud.ai.model.RerankRequest;
import com.alibaba.cloud.ai.model.RerankResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.ranking.DocumentRanker;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * DocumentRanker文档重排序
 *
 * @author future0923
 */
@Component
public class DashScopeDocumentRanker implements DocumentRanker {

    private static final Logger logger = LoggerFactory.getLogger(DashScopeDocumentRanker.class);

    private final RerankModel rerankModel;

    public DashScopeDocumentRanker(RerankModel rerankModel) {
        this.rerankModel = rerankModel;
    }

    @NotNull
    @Override
    public List<Document> rank(@NotNull Query query, @NotNull List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return new ArrayList<>();
        }
        try {
            List<Document> reorderDocs = new ArrayList<>();

            // The caller controls the number of documents
            DashScopeRerankOptions rerankOptions = DashScopeRerankOptions.builder()
                    .withTopN(documents.size())
                    .build();

            if (StringUtils.hasText(query.text())) {
                // The assembly parameter calls rerankModel
                RerankRequest rerankRequest = new RerankRequest(
                        query.text(),
                        documents,
                        rerankOptions
                );
                RerankResponse rerankResp = rerankModel.call(rerankRequest);

                rerankResp.getResults().forEach(res -> {
                    Document outputDocs = res.getOutput();

                    // Find and add to a new list
                    Optional<Document> foundDocsOptional = documents.stream()
                            .filter(doc ->
                            {
                                // debug rerank output.
                                logger.debug("DashScopeDocumentRanker#rank() doc id: {}, outputDocs id: {}", doc.getId(), outputDocs.getId());
                                return Objects.equals(doc.getId(), outputDocs.getId());
                            })
                            .findFirst();

                    foundDocsOptional.ifPresent(reorderDocs::add);
                });
            }

            return reorderDocs;
        }
        catch (Exception e) {
            // Further processing is done depending on the type of exception
            logger.error("ranker error", e);
            return documents;
        }
    }
}
```

百炼 indexName 位置:

![img.png](/images/hwuidhwiuhdaiuwhdw.png){v-zoom}{loading="lazy"}

**使用方向：**
- 文档检索一般要结合[向量存储](vector-store.md)进行使用，提前将Document信息存储在向量存储中，然后进行检索。
- Document信息通过[ETL Pipeline](etl-pipeline)可以对Document进行操作，如：
  - 实现[DocumentReader](etl-pipeline#document-reader)可以对Document进行读取，提取需要的数据。
  - 实现[DocumentTransformer](etl-pipeline#document-transformer)对Document进行转换，将提取到的数据转换为特定的格式。
  - 实现[DocumentWriter](etl-pipeline#document-writer)对Document进行写入，将提取到的数据写入到数据库中。
- 搜索提取的Document信息可以在程序中使用，比如元数据中增加数据id信息，查询数据库后进行操作。
- 将Document信息利用RAG传入大模型后续使用。

