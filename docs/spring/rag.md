# 索引增强生成(RAG)

演示[代码](https://github.com/future0923/ai-agent-example/tree/main/java/rag/src/test/java/io/github/future0923/ai/agent/example/rag/service)

## 简介

[RAG](../guide/concepts#rag)（Retrieval Augmented Generation，检索增强生成）是一种结合信息检索和文本生成的技术范式。可用于克服大型语言模型的局限性，这些模型难以处理长格式内容、事实精度和上下文感知。就像给AI装上了「实时百科大脑」，通过先查资料后回答的机制，让AI摆脱传统模型的”知识遗忘”困境。

## API

Spring AI使用 [Advisor](chat-client#advisors) API为常见RAG流提供开箱即用的支持。

### QuestionAnswerAdvisor

[向量数据库](vector-store)存储AI模型不知道的数据，当向AI模型发送用户问题时，QuestionAnswerAdvisor向向量数据库查询与该用户问题相关的文档。来自向量数据库的响应被附加到用户文本中，为AI模型生成响应提供上下文。

**创建一个QuestionAnswerAdvisor实例的参数**
- [VectorStore](vector-store)：从哪个向量存储中获取数据。
- [SearchRequest](vector-store#metadata-filter)：查询请求。设置查询参数和元数据过滤条件优化查询。

假设您已经将数据加载到VectorStore中，您可以通过向QuestionAnswerAdvisor提供ChatClient实例来执行检索增强生成（RAG）。

可以在创建QuestionAnswerAdvisor时配置[过滤器](vector-store#metadata-filter)，因此将始终应用于所有ChatClient请求，或者可以在每个请求的运行时提供。

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

public class QuestionAnswerAdvisorTest extends RagApplicationTest {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private ChatClient.Builder builder;

    private VectorStore vectorStore;

    @BeforeEach
    public void vectorStore() {
        vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        // 生成一个机器人产品说明书的文档
        List<Document> documents = List.of(
                new Document("""
                        产品说明书:产品名称：智能机器人。
                        产品描述：智能机器人是一个智能设备，能够自动完成各种任务。
                        功能：
                        1. 自动导航：机器人能够自动导航到指定位置。
                        2. 自动抓取：机器人能够自动抓取物品。
                        3. 自动放置：机器人能够自动放置物品。
                        """,
                        Map.of("type", "robot"))
        );
        vectorStore.add(documents);
    }

    @Test
    public void use() {
        ChatClient chatClient = builder.build();
        Flux<String> flux = chatClient.prompt()
                .user("智能机器人能做什么")
                .advisors(new QuestionAnswerAdvisor(
                        vectorStore,
                        // 具体点击上面连接查看更详细的SearchRequest设置
                        SearchRequest.builder()
                                .filterExpression("type == 'robot'")
                                .build()))
                .stream()
                .content();
        StepVerifier.create(flux)
                .thenConsumeWhile(res -> {
                    System.out.print(res);
                    return true;
                })
                .verifyComplete();
    }
}
```

### RetrievalAugmentationAdvisor

Spring AI包含一个RAG模块库，您可以使用它来构建自己的RAG流。[RetrievalAugmentationAdvisor](#retrieval-augmentation-advisor)是一个实验性Advisor，基于模块化架构，为最常见的RAG流提供开箱即用的实现。

```java
Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
        .queryTransformer(RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder.build().mutate())
                .build())
        .documentRetriever(VectorStoreDocumentRetriever.builder()
                .similarityThreshold(0.50)
                .vectorStore(vectorStore)
                .build())
        .build();

String answer = chatClient.prompt()
        .advisors(retrievalAugmentationAdvisor)
        .user(question)
        .call()
        .content();
```

## 高级特性

### 多查询扩展(Multi Query Expansion)

[MultiQueryExpander](../guide/concepts#multi-query-expansion)大型语言模型将查询扩展为多个语义不同的变体，以捕获不同的视角，这对于检索额外的上下文信息和增加找到相关结果的机会很有用。

| 参数                  | 含义                                  |
|---------------------|-------------------------------------|
| chatClientBuilder() | 传入ChatClientBuilder实例用于构建ChatClient |
| promptTemplate()    | 此方法设置提示模板                           |
| includeOriginal()   | 是否包含原始查询                            |
| numberOfQueries()   | 生成几个查询变体                            |


```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class MultiQueryExpanderTest {

    @Autowired
    private ChatClient.Builder builder;

    /**
     * 构建查询扩展器，用于生成多个相关的查询变体，以获得更全面的搜索结果
     */
    @Test
    public void test() {
        MultiQueryExpander queryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(builder)
                // // 设置系统提示信息，定义AI助手作为专业的室内设计顾问角色
                .promptTemplate(new PromptTemplate("你是一位专业的室内设计顾问，精通各种装修风格、材料选择和空间布局。请基于提供的参考资料，为用户提供专业、详细且实用的建议。在回答时，请注意：\n" +
                        "1. 准确理解用户的具体需求\n" +
                        "2. 结合参考资料中的实际案例\n" +
                        "3. 提供专业的设计理念和原理解释\n" +
                        "4. 考虑实用性、美观性和成本效益\n" +
                        "5. 如有需要，可以提供替代方案"))
                // 不包含原始查询
                .includeOriginal(false)
                // 生成3个查询变体
                .numberOfQueries(3)
                .build();
        // 执行查询扩展
        // 将原始问题"请提供几种推荐的装修风格?"扩展成多个相关查询
        List<Query> queries = queryExpander.expand(new Query("请提供几种推荐的装修风格?"));
        // Query[text=当前流行的几种室内装修风格推荐?, history=[], context={}]
        // Query[text=适合小户型的装修风格有哪些推荐?, history=[], context={}]
        // Query[text=根据预算选择的最佳装修风格推荐?, history=[], context={}]
        queries.forEach(System.out::println);
    }
}
```

### 查询重写(Query Rewrite)

[RewriteQueryTransformer](../guide/concepts#query-rewrite) 查询重写是RAG系统中的一个重要优化技术，它能够将用户的原始查询转换成更加结构化和明确的形式。这种转换可以提高检索的准确性，并帮助系统更好地理解用户的真实意图。

| 参数                  | 含义                                  |
|---------------------|-------------------------------------|
| chatClientBuilder() | 传入ChatClientBuilder实例用于构建ChatClient |
| promptTemplate()    | 此方法设置提示模板                           |

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author future0923
 */
public class RewriteQueryTransformerTest extends RagApplicationTest {

    @Autowired
    private ChatClient.Builder builder;

    @Test
    public void test() {
        // 创建一个模拟用户学习AI的查询场景
        Query query = new Query("今天上午吃完饭后的我正在学习人工智能，什么是大语言模型？");
        // 创建查询重写转换器
        QueryTransformer queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .promptTemplate(new PromptTemplate("过滤掉没有的信息，提取出用户真正想问的信息"))
                .build();
        // 执行查询重写
        Query transformedQuery = queryTransformer.transform(query);
        // 输出重写后的查询
        // 大语言模型的定义和作用是什么？
        System.out.println(transformedQuery.text());
    }
}
```

### 查询翻译(Query Translation)

[TranslationQueryTransformer](../guide/concepts#query-translation) 查询翻译是RAG系统中的一个实用功能，它能够将用户的查询从一种语言翻译成另一种语言。这对于多语言支持和跨语言检索特别有用。

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.beans.factory.annotation.Autowired;

public class TranslationQueryTransformerTest extends RagApplicationTest {

    @Autowired
    private ChatClient.Builder builder;

    /**
     * 翻译用户提问的问题
     */
    @Test
    public void test() {
        // 创建一个英文查询
        Query query = new Query("What is LLM?");
        // 创建查询翻译转换器，设置目标语言为中文
        QueryTransformer queryTransformer = TranslationQueryTransformer.builder()
                .chatClientBuilder(builder)
                .targetLanguage("chinese")  // 设置目标语言为中文
                .build();
        // 执行查询翻译
        Query transformedQuery = queryTransformer.transform(query);
        // 输出翻译后的查询
        // 什么是大语言模型？
        System.out.println(transformedQuery.text());
    }
}
```

### 上下文感知查询(Context-aware Queries)

[CompressionQueryTransformer](../guide/concepts#context-aware-queries) 在实际对话中，用户的问题往往依赖于之前的对话上下文。`将问题通过上下文解析为明确的问题后发给大模型`。

还有一种方式时通过[聊天记忆](chat-memory)方式`将聊天记录传给大模型`，从而实现上下文感知查询。两个有本质的区别。

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author future0923
 */
public class CompressionQueryTransformerTest extends RagApplicationTest {

    @Autowired
    private ChatClient.Builder builder;

    /**
     * 在实际对话中，用户的问题往往依赖于之前的对话上下文。将问题通过上下文解析为明确的问题
     */
    @Test
    public void test() {
        // 构建带有历史上下文的查询
        // 这个例子模拟了一个房地产咨询场景，用户先问小区位置，再问房价
        Query query = Query.builder()
                .text("那这个小区的二手房均价是多少?")  // 当前用户的提问
                .history(new UserMessage("深圳市南山区的碧海湾小区在哪里?"),  // 历史对话中用户的问题
                        new AssistantMessage("碧海湾小区位于深圳市南山区后海中心区，临近后海地铁站。"))  // AI的回答
                .build();
        // 创建查询转换器
        // QueryTransformer用于将带有上下文的查询转换为完整的独立查询
        QueryTransformer queryTransformer = CompressionQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();
        // 执行查询转换
        // 将模糊的代词引用（"这个小区"）转换为明确的实体名称（"碧海湾小区"）
        Query transformedQuery = queryTransformer.transform(query);
        // 深圳市南山区碧海湾小区的二手房均价是多少？
        System.out.println(transformedQuery.text());
    }
}
```

### 文档合并器(DocumentJoiner)

[ConcatenationDocumentJoiner](../guide/concepts#document-joiner) 需要从多个查询或多个数据源获取文档。为了有效地管理和整合这些文档，文档合并器可以将多个来源的文档智能地合并成一个统一的文档集合。

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author future0923
 */
public class ConcatenationDocumentJoinerTest extends RagApplicationTest {

    @Autowired
    private ChatClient.Builder builder;

    /**
     * 从多个查询或多个数据源获取文档。为了有效地管理和整合这些文档，Spring AI提供了ConcatenationDocumentJoiner文档合并器。这个工具可以将多个来源的文档智能地合并成一个统一的文档集合。
     */
    @Test
    public void test() {
        Map<Query, List<List<Document>>> documentsForQuery = new HashMap<>();
        documentsForQuery.put(
                new Query("长春怎么样?"),
                List.of(
                        List.of(new Document("长春市是吉林的省会")), List.of(new Document("长春市被称为春城")),
                        List.of(new Document("长春市滑雪比较好")), List.of(new Document("长春市净月潭很好"))
                )
        );
        ConcatenationDocumentJoiner concatenationDocumentJoiner = new ConcatenationDocumentJoiner();
        List<Document> join = concatenationDocumentJoiner.join(documentsForQuery);
        // Document{id='9355d2b1-9306-4044-a62c-60863f81a1f1', text='长春市被称为春城', media='null', metadata={}, score=null}
        // Document{id='ee3aa308-206f-4195-a868-628c747162ce', text='长春市净月潭很好', media='null', metadata={}, score=null}
        // Document{id='251fc31a-9682-4354-997e-e6278a3e9d6d', text='长春市滑雪比较好', media='null', metadata={}, score=null}
        // Document{id='1e593180-2d00-4418-8150-7e2de08d415a', text='长春市是吉林的省会', media='null', metadata={}, score=null}
        join.forEach(System.out::println);
    }
}
```

### 检索增强顾问(Retrieval Augmentation Advisor){#retrieval-augmentation-advisor}

[RetrievalAugmentationAdvisor](../guide/concepts#retrieval-augmentation-advisor) RetrievalAugmentationAdvisor是Spring AI提供的一个强大工具，它能够自动化地处理文档检索和查询增强过程。这个顾问组件将文档检索与查询处理无缝集成，使得AI助手能够基于检索到的相关文档提供更准确的回答。

上面的代码都可以通过这个设置。

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author future0923
 */
public class RetrievalAugmentationAdvisorTest extends RagApplicationTest {

    @Autowired
    private ChatClient.Builder builder;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Test
    public void test() {
        // 1. 初始化向量存储
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel)
                .build();
        // 2. 添加文档到向量存储
        List<Document> documents = List.of(
                new Document("产品说明书:产品名称：智能机器人\n" +
                        "产品描述：智能机器人是一个智能设备，能够自动完成各种任务。\n" +
                        "功能：\n" +
                        "1. 自动导航：机器人能够自动导航到指定位置。\n" +
                        "2. 自动抓取：机器人能够自动抓取物品。\n" +
                        "3. 自动放置：机器人能够自动放置物品。\n"));
        vectorStore.add(documents);
        Advisor advisor = RetrievalAugmentationAdvisor.builder()
                // 配置查询增强器
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        // 允许空上下文查询
                        .allowEmptyContext(true)
                        .build())
                // 配置文档检索器
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        // 返回相似度高于此值的文档。范围从0到1的双精度值，其中接近1的值表示较高的相似度。
                        .similarityThreshold(0.5)
                        // 一个整数，指定要返回的相似文档的最大数量。这通常被称为“顶部K”搜索，或“K近邻算法”。
                        .topK(3)
                        //.filterExpression()
                        .build())
                // 等等
                .build();
        String content = builder.build()
                .prompt()
                .user("智能机器人是什么")
                .advisors(advisor)
                .call()
                .content();
        System.out.println(content);
    }

}
```

### 文档选择(Document Selection)

[文档选择](../guide/concepts#document-selection) 可以对文档进行筛选过滤。

- [FilterExpression](vector-store#metadata-filter)：查询请求。设置查询参数和元数据过滤条件优化查询。
- [DocumentRetriever](document-retriever)：文档检索可以配置相似度控制。
- [RetrievalAugmentationAdvisor](#retrieval-augmentation-advisor)：上下文感知的查询增强器，如允许空上下文查询

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文档选择
 * 
 * @author future0923
 */
public class DocumentSelectionTest extends RagApplicationTest {

    @Autowired
    private ChatClient.Builder builder;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Test
    public void test() {
        // 生成室内设计案例文档
        List<Document> documents = new ArrayList<>();
        // 现代简约风格客厅案例
        documents.add(new Document(
                "案例编号：LR-2023-001\n" +
                        "项目概述：180平米大平层现代简约风格客厅改造\n" +
                        "设计要点：\n" +
                        "1. 采用5.2米挑高的落地窗，最大化自然采光\n" +
                        "2. 主色调：云雾白(哑光，NCS S0500-N)配合莫兰迪灰\n" +
                        "3. 家具选择：意大利B&B品牌真皮沙发，北欧白橡木茶几\n" +
                        "4. 照明设计：嵌入式筒灯搭配意大利Flos吊灯\n" +
                        "5. 软装配饰：进口黑胡桃木电视墙，几何图案地毯\n" +
                        "空间效果：通透大气，适合商务接待和家庭日常起居",
                Map.of(
                        "type", "interior",    // 文档类型
                        "year", "2023",        // 年份
                        "month", "06",         // 月份
                        "location", "indoor",   // 位置类型
                        "style", "modern",      // 装修风格
                        "room", "living_room"   // 房间类型
                )));
        // 1. 初始化向量存储
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        vectorStore.write(documents);

        // 2. 配置AI助手角色
        ChatClient chatClient = builder
                .defaultSystem("你是一位专业的室内设计顾问，精通各种装修风格、材料选择和空间布局。请基于提供的参考资料，为用户提供专业、详细且实用的建议。在回答时，请注意：\n" +
                        "1. 准确理解用户的具体需求\n" +
                        "2. 结合参考资料中的实际案例\n" +
                        "3. 提供专业的设计理念和原理解释\n" +
                        "4. 考虑实用性、美观性和成本效益\n" +
                        "5. 如有需要，可以提供替代方案")
                .build();

        // 3. 构建复杂的文档过滤条件
        var b = new FilterExpressionBuilder();
        var filterExpression = b.and(
                b.and(
                        b.eq("year", "2023"),         // 筛选2023年的案例
                        b.eq("location", "indoor")),   // 仅选择室内案例
                b.and(
                        b.eq("type", "interior"),      // 类型为室内设计
                        b.in("room", "living_room", "study", "kitchen")  // 指定房间类型
                ));

        // 4. 配置文档检索器
        DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.01)    // 设置相似度阈值
                .topK(3)                     // 返回前3个最相关的文档
                .filterExpression(filterExpression.build())
                .build();

        // 5. 创建上下文感知的查询增强器
        Advisor advisor = RetrievalAugmentationAdvisor.builder()
                .queryAugmenter(
                        // 该ContextualQueryAugmenter使用来自所提供文档内容的上下文数据来扩充用户查询。
                        ContextualQueryAugmenter.builder()
                        // 默认情况下，ContextualQueryAugmenter不允许检索到的上下文为空。发生这种情况时，它会指示模型不要回答用户查询。
                        .allowEmptyContext(true)
                        .build())
                .documentRetriever(retriever)
                .build();

        // 6. 执行查询并获取响应
        String userQuestion = "根据已经提供的资料，请描述所有相关的场景风格，输出案例编号，尽可能详细地描述其内容。";
        Flux<String> flux = chatClient.prompt()
                .user(userQuestion)
                .advisors(advisor)
                .stream()
                .content();
        StepVerifier
                .create(flux)
                .thenConsumeWhile(res -> {
                    System.out.print(res);
                    return true;
                })
                .verifyComplete();
    }
}
```

### 错误处理和边界情况(Error Handling and Edge Cases)

该 `ContextualQueryAugmenter` 使用来自所提供文档内容的上下文数据来扩充用户查询。

```java
QueryAugmenter queryAugmenter = ContextualQueryAugmenter.builder().build();
```

默认情况下，ContextualQueryAugmenter不允许检索到的上下文为空。发生这种情况时，它会指示模型不要回答用户查询。

即使检索到的上下文为空，您也可以启用allowEmptyContext选项以允许模型生成响应。

```java
QueryAugmenter queryAugmenter = ContextualQueryAugmenter.builder()
.allowEmptyContext(true)
.build();
```

该组件使用的提示可以通过构建器中可用的promptTemplate()和emptyContextPromptTemplate()方法进行定制。