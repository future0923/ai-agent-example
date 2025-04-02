package io.github.future0923.ai.agent.example.vector.store.spring.ai.milvus.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.autoconfigure.vectorstore.milvus.MilvusVectorStoreAutoConfiguration;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author future0923
 */
@RestController
public class RagController {

    /**
     * {@link MilvusVectorStoreAutoConfiguration#vectorStore}
     */
    private final MilvusVectorStore vectorStore;

    private final ChatClient.Builder builder;

    private final ChatMemory chatMemory;

    public RagController(MilvusVectorStore vectorStore, ChatClient.Builder builder) {
        this.vectorStore = vectorStore;
        this.builder = builder;
        this.chatMemory = new InMemoryChatMemory();
    }

    @GetMapping("/chat")
    public Flux<String> chat(@RequestParam("query") String query,
                             HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        ChatClient chatClient = builder.build();
        return chatClient.prompt()
                .user(query)
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .advisors(new MessageChatMemoryAdvisor(chatMemory))
                .advisors(advisorSpec -> advisorSpec
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, "default")
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .stream()
                .content();
    }

    @GetMapping("/add")
    public String add() {
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
        documents.add(new Document("""
                产品说明书:产品名称：智能机器人。
                产品描述：智能机器人是一个智能设备，能够自动完成各种任务。
                功能：
                1. 自动导航：机器人能够自动导航到指定位置。
                2. 自动抓取：机器人能够自动抓取物品。
                3. 自动放置：机器人能够自动放置物品。
                """,
                Map.of("type", "robot")));
        vectorStore.add(documents);
        return "success";
    }

    @GetMapping("/search")
    public List<Document> search(@RequestParam("query") String query,
                                 @RequestParam(value = "type", defaultValue = "interior") String type) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(3)
                        .filterExpression(builder.eq("type", type).build())
                        .build()
        );
    }

}
