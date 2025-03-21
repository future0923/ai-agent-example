package io.github.future0923.ai.agent.example.chat.client.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.time.LocalDate;

/**
 * 配置
 *
 * @author future0923
 */
@Configuration
public class ChatClientConfig {

    public void chatClient(ChatModel chatModel) {
        // 创建 ChatClient.Builder
        ChatClient.Builder builder = ChatClient.builder(chatModel);
        // 创建 ChatClient
        ChatClient chatClient = ChatClient.create(chatModel);
    }

    public void chatClient(ChatClient.Builder builder) {
        ChatClient client = builder.build();
    }

    /**
     * 聊天记忆
     */
    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    /**
     * 向量数据库
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * 初始化向量数据库
     */
    @Bean
    public CommandLineRunner ingestTermOfServiceToVectorStore(
            VectorStore vectorStore,
            @Value("classpath:rag/terms-of-service.txt") Resource resource) {
        return args -> vectorStore.write(new TokenTextSplitter().transform(new TextReader(resource).read()));
    }


    @Bean
    public ChatClient chatClient(
            ChatClient.Builder builder,
            ChatMemory chatMemory,
            VectorStore vectorStore,
            ToolCallbackProvider weatherToolsProvider) {
        return builder
                .defaultSystem("""
                        # 角色
                        你是合众小助手，作为专业且高效的智能AI选房助手🎈，能深入精准理解用户需求，从海量数据库中迅速筛选出高度匹配条件的房源信息，并以清晰、直观、准确且富有吸引力的方式展示给用户。
                        ## 技能
                        今天的日期是 {current_date}。
                        ### 技能 1: 处理用户房源信息输入
                        1. 当用户输入想买的房源信息时，运用强大精准的信息提取能力，准确提炼出关键信息，如房屋位置📍、期望面积范围、预算区间💰、房型结构等。对于模糊、不完整或有歧义的信息，要通过合理、恰当且友好的提问引导用户补充完整、明确含义。
                        ### 技能 2：快捷取消预订
                        1. 您必须始终从用户处获取以下信息：预订号、客户姓名。
                        ### 技能 3：天气查询
                        可以获取获取指定经纬度的天气预报和获取指定位置的空气质量信息
                        ## 限制:
                        - 仅处理与房源信息相关的内容，拒绝回答与AI选房无关的任何话题。
                        - 输出的房源信息必须严格按照给定的格式组织，不得有任何偏离框架要求的情况。
                        """)
                .defaultSystem(promptSystemSpec -> promptSystemSpec.param("current_date", LocalDate.now().toString()))
                .defaultAdvisors(
                        new PromptChatMemoryAdvisor(chatMemory),
                        new SimpleLoggerAdvisor(),
                        new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().build())
                )
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .withTopP(0.7)
                                .build()
                )
                .defaultTools("cancelBooking")
                .defaultTools(weatherToolsProvider)
                .build();
    }

}
