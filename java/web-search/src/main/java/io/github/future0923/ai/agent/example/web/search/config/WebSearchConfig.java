package io.github.future0923.ai.agent.example.web.search.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.memory.jdbc.MysqlChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author future0923
 */
@Configuration
public class WebSearchConfig {

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

    /**
     * ContextualQueryAugmenter使用来自所提供文档内容的上下文数据来扩充用户查询。
     */
    @Bean
    public QueryAugmenter queryAugmenter(PromptTemplate queryArgumentPromptTemplate) {
        return new ContextualQueryAugmenter(queryArgumentPromptTemplate, null, true);
    }

    /**
     * 基于MySQL的聊天记忆
     */
    @Bean
    public ChatMemory MysqlChatMemory() {
        return new MysqlChatMemory("root", "123456Aa", "jdbc:mysql://192.168.0.11:3306/ai-chat-memory");
    }
}
