package io.github.future0923.ai.agent.example.rag.service;

import io.github.future0923.ai.agent.example.rag.RagApplicationTest;
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

    /**
     * 过滤掉没有的信息，提取出用户真正想问的信息
     */
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
