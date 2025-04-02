# Web Search 应用搭建

构建智能 Web Search 应用，可以联网搜索互联网内容丰富实时热点信息，还可以开启推理模式进行深度思考(大模型要支持)。 

类似chatgpt如下功能：

![jiwdjiojoidjassd.png](/images/jiwdjiojoidjassd.png){v-zoom}{loading="lazy"}

## Spring AI 知识点

利用 [Spring AI RAG](../spring/rag) 实现

### Pre-Retrieval

::: tip
增强和转换用户输入，使其更有效地执行检索任务，解决格式不正确的查询、query 语义不清晰、或不受支持的语言等。
:::

1. `QueryAugmenter` 查询增强：使用附加的上下文数据信息增强用户 query，提供大模型回答问题时的必要上下文信息；
2. `QueryTransformer` 查询改写：因为用户的输入通常是片面的，关键信息较少，不便于大模型理解和回答问题。因此需要使用 prompt 调优手段或者大模型改写用户 query；
3. `QueryExpander` 查询扩展：将用户 query 扩展为多个语义不同的变体以获得不同视角，有助于检索额外的上下文信息并增加找到相关结果的机会。

### Retrieval

::: tip
负责查询向量存储等数据系统并检索和用户 query 相关性最高的 Document。
:::

1. DocumentRetriever：检索器，根据 QueryExpander 使用不同的数据源进行检索，例如 搜索引擎、向量存储、数据库或知识图等；
2. DocumentJoiner：将从多个 query 和从多个数据源检索到的 Document 合并为一个 Document 集合；

### Post-Retrieval

::: tip
负责处理检索到的 Document 以获得最佳的输出结果，解决模型中的中间丢失和上下文长度限制等。
:::

1. DocumentRanker：根据 Document 和用户 query 的相关性对 document 进行排序和排名；
2. DocumentSelector：用于从检索到的 Document 列表中删除不相关或冗余文档；
3. DocumentCompressor：用于压缩每个 Document，减少检索到的信息中的噪音和冗余。

## 示例

- 框架使用 `spring-ai` 和 `spring-ai-alibaba`.
- 大模型使用 `qwen-max`、`qwen-plus`
- 联网搜索使用 `IQS`

### Pre-Retrieval

通过 `ContextualQueryAugmenter` 将用户 Query 使用 qwen-plus 大模型进行增强改写。

```java
@Bean
public PromptTemplate queryArgumentPromptTemplate() {

    return new PromptTemplate(
            """
            You'll get a set of document contexts that are relevant to the issue.
            Each document begins with a reference number, such as [[x]], where x is a number that can be repeated.
            Documents that are not referenced will be marked as [[null]].
            Use context and refer to it at the end of each sentence, if applicable.
            The context information is as follows:
            
            ---------------------
            {context}
            ---------------------
            
            Generate structured responses to user questions given contextual information and without prior knowledge.
            
            When you answer user questions, follow these rules:
            
            1. If the answer is not in context, say you don't know;
            2. Don't provide any information that is not relevant to the question, and don't output any duplicate content;
            3. Avoid using "context-based..." or "The provided information..." said;
            4. Your answers must be correct, accurate, and written in an expertly unbiased and professional tone;
            5. The appropriate text structure in the answer is determined according to the characteristics of the content, please include subheadings in the output to improve readability;
            6. When generating a response, provide a clear conclusion or main idea first, without a title;
            7. Make sure each section has a clear subtitle so that users can better understand and refer to your output;
            8. If the information is complex or contains multiple sections, make sure each section has an appropriate heading to create a hierarchical structure;
            9. Please refer to the sentence or section with the reference number at the end in [[x]] format;
            10. If a sentence or section comes from more than one context, list all applicable references, e.g. [[x]][[y]];
            11. Your output answers must be in beautiful and rigorous markdown format.
            12. Because your output is in markdown format, please include the link in the reference document in the form of a hyperlink when referencing the context, so that users can click to view it;
            13. If a reference is marked as [[null]], it does not have to be cited;
            14. Except for Code. Aside from the specific name and citation, your answer must be written in the same language as the question.
            
            User Issue:
            
            {query}
            
            Your answer:
            """
    );
}

/**
 * ContextualQueryAugmenter使用来自所提供文档内容的上下文数据来扩充用户查询。
 */
@Bean
public QueryAugmenter queryAugmenter(PromptTemplate queryArgumentPromptTemplate) {
    return new ContextualQueryAugmenter(queryArgumentPromptTemplate, null, true);
}
```

使用 `RewriteQueryTransformer` 将用户的原始查询转换成更加结构化和明确的形式。这种转换可以提高检索的准确性，并帮助系统更好地理解用户的真实意图。

```java
@Bean
public PromptTemplate transformerPromptTemplate() {

    return new PromptTemplate(
            """
            Given a user query, rewrite the user question to provide better results when querying {target}.
            
            You should follow these rules:
            
            1. Remove any irrelevant information and make sure the query is concise and specific;
            2. The output must be consistent with the language of the user's query;
            3. Ensure better understanding and answers from the perspective of large models.
            
            Original query:
            {query}
            
            Query after rewrite:
            """
    );
}

/**
 * RewriteQueryTransformer
 * 查询重写是RAG系统中的一个重要优化技术，它能够将用户的原始查询转换成更加结构化和明确的形式。这种转换可以提高检索的准确性，并帮助系统更好地理解用户的真实意图。
 */
@Bean
public QueryTransformer queryTransformer(
        ChatClient.Builder chatClientBuilder,
        @Qualifier("transformerPromptTemplate") PromptTemplate transformerPromptTemplate
) {
    ChatClient chatClient = chatClientBuilder.defaultOptions(
            DashScopeChatOptions.builder()
                    .withModel("qwen-plus")
                    .build()
    ).build();
    return RewriteQueryTransformer.builder()
            .chatClientBuilder(chatClient.mutate())
            .promptTemplate(transformerPromptTemplate)
            .targetSearchSystem("Web Search")
            .build();
}
```

使用 `MultiQueryExpander` 将查询扩展为多个语义不同的变体，以捕获不同的视角，这对于检索额外的上下文信息和增加找到相关结果的机会很有用。

```java
 /**
 * 大型语言模型将查询扩展为多个语义不同的变体，以捕获不同的视角，这对于检索额外的上下文信息和增加找到相关结果的机会很有用。
 */
@Bean
public QueryExpander queryExpander(ChatClient.Builder chatClientBuilder) {

    ChatClient chatClient = chatClientBuilder.defaultOptions(
            DashScopeChatOptions.builder()
                    .withModel("qwen-plus")
                    .build()
    ).build();
    return MultiQueryExpander.builder()
            .chatClientBuilder(chatClient.mutate())
            .numberOfQueries(2)
            .build();
}
```

### Retrieval

使用 `WebSearchDocumentRetriever` 根据 Document 和用户 query 的相关性对 Document 进行排序和排名；

```java
import io.github.future0923.ai.agent.example.web.search.dto.websearch.GenericSearchResult;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.ranking.DocumentRanker;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;

import java.util.List;

/**
 * @author future0923
 */
public class WebSearchDocumentRetriever implements DocumentRetriever {

    private static final Logger logger = LoggerFactory.getLogger(WebSearchDocumentRetriever.class);

    private final IQSSearchService iqsSearchService;

    private final DataClean dataClean;

    private final DocumentRanker documentRanker;

    private final int maxResults;

    private final boolean enableRanker;

    private WebSearchDocumentRetriever(Builder builder) {

        this.iqsSearchService = builder.iqsSearchService;
        this.maxResults = builder.maxResults;
        this.dataClean = builder.dataCleaner;
        this.enableRanker = builder.enableRanker;
        this.documentRanker = builder.documentRanker;
    }

    @NotNull
    @Override
    public List<Document> retrieve(@NotNull Query query) {
        // 使用阿里 iqs 搜索实时数据
        GenericSearchResult searchResult = iqsSearchService.search(query);
        // 清洗结果
        List<Document> documentList = dataClean.getData(searchResult);
        // 限制最大结果数
        List<Document> documents = dataClean.limitResults(documentList, maxResults);
        // 文档重排序
        return enableRanker ? ranking(query, documents) : documents;
    }

    private List<Document> ranking(Query query, List<Document> documents) {
        if (documents.size() == 1) {
            // 只有一个时，不需要 rank
            return documents;
        }
        try {
            return documentRanker.rank(query, documents);
        } catch (Exception e) {
            // 降级返回原始结果
            logger.error("ranking error", e);
            return documents;
        }
    }

    public static WebSearchDocumentRetriever.Builder builder() {
        return new WebSearchDocumentRetriever.Builder();
    }


    public static final class Builder {

        private IQSSearchService iqsSearchService;

        private int maxResults;

        private DataClean dataCleaner;

        private DocumentRanker documentRanker;

        // 默认开启 ranking
        private Boolean enableRanker = true;

        public WebSearchDocumentRetriever.Builder searchService(IQSSearchService searchService) {

            this.iqsSearchService = searchService;
            return this;
        }

        public WebSearchDocumentRetriever.Builder dataCleaner(DataClean dataCleaner) {

            this.dataCleaner = dataCleaner;
            return this;
        }

        public WebSearchDocumentRetriever.Builder maxResults(int maxResults) {

            this.maxResults = maxResults;
            return this;
        }

        public WebSearchDocumentRetriever.Builder documentRanker(DocumentRanker documentRanker) {
            this.documentRanker = documentRanker;
            return this;
        }

        public WebSearchDocumentRetriever.Builder enableRanker(Boolean enableRanker) {
            this.enableRanker = enableRanker;
            return this;
        }

        public WebSearchDocumentRetriever build() {
            return new WebSearchDocumentRetriever(this);
        }
    }
}
```

使用 `ConcatenationDocumentJoiner` 合并文档

```java
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 合并文档
 *
 * @author future0923
 */
@Component
public class ConcatenationDocumentJoiner implements DocumentJoiner {

    private static final Logger logger = LoggerFactory.getLogger(ConcatenationDocumentJoiner.class);

    @NotNull
    @Override
    public List<Document> join(@Nullable Map<Query, List<List<Document>>> documentsForQuery) {
        Assert.notNull(documentsForQuery, "documentsForQuery cannot be null");
        Assert.noNullElements(documentsForQuery.keySet(), "documentsForQuery cannot contain null keys");
        Assert.noNullElements(documentsForQuery.values(), "documentsForQuery cannot contain null values");
        logger.debug("Joining documents by concatenation");
        Map<Query, List<List<Document>>> selectDocuments = selectDocuments(documentsForQuery, 10);
        Set<String> seen = new HashSet<>();
        return selectDocuments.values().stream()
                .flatMap(List::stream)
                .flatMap(List::stream)
                .filter(doc -> {
                    List<String> keys = extractKeys(doc);
                    for (String key : keys) {
                        if (!seen.add(key)) {
                            logger.info("Duplicate document metadata: {}", doc.getMetadata());
                            return false;
                        }
                    }
                    // All keys are unique.
                    return true;
                })
                .collect(Collectors.toList());
    }

    private Map<Query, List<List<Document>>> selectDocuments(
            Map<Query, List<List<Document>>> documentsForQuery,
            int totalDocuments
    ) {
        Map<Query, List<List<Document>>> selectDocumentsForQuery = new HashMap<>();
        int numberOfQueries = documentsForQuery.size();
        if (Objects.equals(0, numberOfQueries)) {
            return selectDocumentsForQuery;
        }
        int baseCount = totalDocuments / numberOfQueries;
        int remainder = totalDocuments % numberOfQueries;
        // To ensure consistent distribution. sort the keys (optional)
        List<Query> sortedQueries = new ArrayList<>(documentsForQuery.keySet());
        // Other sort
        // sortedQueries.sort(Comparator.comparing(Query::getSomeProperty));
        for (int i = 0; i < numberOfQueries; i++) {
            Query query = sortedQueries.get(i);
            int documentToSelect = baseCount + (i < remainder ? 1 : 0);
            List<List<Document>> originalDocuments = documentsForQuery.get(query);
            List<List<Document>> selectedNestLists = new ArrayList<>();
            int remainingDocuments = documentToSelect;
            for (List<Document> documentList : originalDocuments) {
                if (remainingDocuments <= 0) {
                    break;
                }
                List<Document> selectSubList = new ArrayList<>();
                for (Document docs : documentList) {
                    if (remainingDocuments <= 0) {
                        break;
                    }
                    selectSubList.add(docs);
                    remainingDocuments--;
                }
                if (!selectSubList.isEmpty()) {
                    selectedNestLists.add(selectSubList);
                }
            }
            selectDocumentsForQuery.put(query, selectedNestLists);
        }
        return selectDocumentsForQuery;
    }

    private List<String> extractKeys(Document document) {
        List<String> keys = new ArrayList<>();
        if (Objects.nonNull(document)) {
            keys.add(document.getId());
        }
        Object src = document.getMetadata().get("source");
        if (src instanceof String) {
            keys.add("SOURCE:" + src);
        }
        Object fn = document.getMetadata().get("file_name");
        if (fn instanceof String) {
            keys.add("FILE_NAME:" + fn);
        }
        return keys;
    }
}
```

### Post-Retrieval

使用 `DocumentRanker` 文档重排序

```java

import com.alibaba.cloud.ai.dashscope.rerank.DashScopeRerankOptions;
import com.alibaba.cloud.ai.model.RerankModel;
import com.alibaba.cloud.ai.model.RerankRequest;
import com.alibaba.cloud.ai.model.RerankResponse;
import io.github.future0923.ai.agent.example.web.search.exception.ProjectException;
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
            throw new ProjectException(e.getMessage());
        }
    }
}
```

### 思考

返回思考内容，返回格式<think>%s</think>为内容

```java

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * 返回思考内容，返回格式<think>%s</think>为内容
 *
 * @author future0923
 */
public class ThinkingContentAdvisor implements BaseAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(ThinkingContentAdvisor.class);

    private final int order;

    public ThinkingContentAdvisor(Integer order) {
        this.order = order != null ? order : 0;
    }

    @NotNull
    @Override
    public AdvisedRequest before(@NotNull AdvisedRequest request) {
        return request;
    }

    @NotNull
    @Override
    public AdvisedResponse after(AdvisedResponse advisedResponse) {
        ChatResponse resp = advisedResponse.response();
        if (Objects.isNull(resp)) {
            return advisedResponse;
        }
        logger.debug(String.valueOf(resp.getResults().get(0).getOutput().getMetadata()));
        String reasoningContent = String.valueOf(resp.getResults().get(0).getOutput().getMetadata().get("reasoningContent"));
        if (StringUtils.hasText(reasoningContent)) {
            List<Generation> thinkGenerations = resp.getResults().stream()
                    .map(generation -> {
                        AssistantMessage output = generation.getOutput();
                        AssistantMessage thinkAssistantMessage = new AssistantMessage(
                                String.format("<think>%s</think>", reasoningContent) + output.getText(),
                                output.getMetadata(),
                                output.getToolCalls(),
                                output.getMedia()
                        );
                        return new Generation(thinkAssistantMessage, generation.getMetadata());
                    }).toList();
            ChatResponse thinkChatResp = ChatResponse.builder().from(resp).generations(thinkGenerations).build();
            return AdvisedResponse.from(advisedResponse).response(thinkChatResp).build();
        }
        return advisedResponse;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

}
```

### 记忆

```java
import com.alibaba.cloud.ai.memory.jdbc.MysqlChatMemory;
import org.springframework.ai.chat.memory.ChatMemory;

/**
 * 基于MySQL的聊天记忆
 */
@Bean
public ChatMemory MysqlChatMemory() {
    return new MysqlChatMemory("root", "123456Aa", "jdbc:mysql://192.168.0.11:3306/ai-chat-memory");
}
```

### 使用

```java
import io.github.future0923.ai.agent.example.web.search.advisor.ThinkingContentAdvisor;
import io.github.future0923.ai.agent.example.web.search.rag.DataClean;
import io.github.future0923.ai.agent.example.web.search.rag.IQSSearchService;
import io.github.future0923.ai.agent.example.web.search.rag.WebSearchDocumentRetriever;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.postretrieval.ranking.DocumentRanker;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author future0923
 */
@Service
public class WebSearchService {

    private final ChatClient chatClient;

    private final WebSearchDocumentRetriever webSearchDocumentRetriever;

    private final QueryTransformer queryTransformer;

    private final QueryExpander queryExpander;

    private final DocumentJoiner documentJoiner;

    private final QueryAugmenter queryAugmenter;

    private final ThinkingContentAdvisor thinkingContentAdvisor;

    private final ChatMemory chatMemory;

    public WebSearchService(ChatClient.Builder builder,
                            IQSSearchService searchService,
                            DataClean dataClean,
                            DocumentRanker documentRanker,
                            QueryTransformer queryTransformer, QueryExpander queryExpander, DocumentJoiner documentJoiner, QueryAugmenter queryAugmenter, ChatMemory chatMemory) {
        this.chatClient = builder.build();
        this.queryTransformer = queryTransformer;
        this.queryExpander = queryExpander;
        this.documentJoiner = documentJoiner;
        this.queryAugmenter = queryAugmenter;
        this.chatMemory = chatMemory;
        this.thinkingContentAdvisor = new ThinkingContentAdvisor(1);
        this.webSearchDocumentRetriever = WebSearchDocumentRetriever.builder()
                .searchService(searchService)
                .dataCleaner(dataClean)
                .maxResults(2)
                .enableRanker(true)
                .documentRanker(documentRanker)
                .build();
    }

    public Flux<String> chat(String query, String chatId) {
        return chatClient.prompt()
                .user(query)
                .advisors(
                        RetrievalAugmentationAdvisor.builder()
                                .documentRetriever(webSearchDocumentRetriever)
                                .queryTransformers(queryTransformer)
                                .queryExpander(queryExpander)
                                .documentJoiner(documentJoiner)
                                .queryAugmenter(queryAugmenter)
                                .build()
                )
                .advisors(new MessageChatMemoryAdvisor(chatMemory))
                .advisors(advisorSpec -> advisorSpec
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .advisors(thinkingContentAdvisor)
                .advisors(new SimpleLoggerAdvisor())
                .stream()
                .content();
    }
}
```

### 源码

[源码](https://github.com/future0923/ai-agent-example/tree/main/java/web-search)