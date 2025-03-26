package io.github.future0923.ai.agent.example.rag.service;

import io.github.future0923.ai.agent.example.rag.RagApplicationTest;
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
