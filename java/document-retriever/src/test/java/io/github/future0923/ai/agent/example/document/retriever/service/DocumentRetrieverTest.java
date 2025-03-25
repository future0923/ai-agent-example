package io.github.future0923.ai.agent.example.document.retriever.service;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import io.github.future0923.ai.agent.example.document.retriever.DocumentRetrieverApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class DocumentRetrieverTest extends DocumentRetrieverApplicationTest {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Test
    public void test() {
        DocumentRetriever documentRetriever = new DashScopeDocumentRetriever(
                new DashScopeApi(apiKey),
                DashScopeDocumentRetrieverOptions.builder()
                        .withIndexName("合众房产通知公告")
                        .withRerankTopN(3)
                        .build());
        var result = documentRetriever.retrieve(Query.builder().text("房客源常见问题解析").build());
        result.forEach(System.out::println);
    }
}