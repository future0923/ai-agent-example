package io.github.future0923.ai.agent.example.rag.service;

import io.github.future0923.ai.agent.example.rag.RagApplicationTest;
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
