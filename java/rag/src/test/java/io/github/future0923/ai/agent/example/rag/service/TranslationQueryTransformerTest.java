package io.github.future0923.ai.agent.example.rag.service;

import io.github.future0923.ai.agent.example.rag.RagApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author future0923
 */
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