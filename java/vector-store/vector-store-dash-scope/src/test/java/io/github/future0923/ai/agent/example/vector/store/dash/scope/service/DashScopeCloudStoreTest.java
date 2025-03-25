package io.github.future0923.ai.agent.example.vector.store.dash.scope.service;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeCloudStore;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeStoreOptions;
import io.github.future0923.ai.agent.example.vector.store.dash.scope.DashScopeVectorStoreApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * @author future0923
 */
public class DashScopeCloudStoreTest extends DashScopeVectorStoreApplicationTest {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Test
    public void test() {
        DashScopeCloudStore dashScopeCloudStore = new DashScopeCloudStore(
                new DashScopeApi(apiKey),
                new DashScopeStoreOptions("合众房产通知公告")
        );
        List<Document> documents = dashScopeCloudStore.similaritySearch("房客源常见问题解析");
        documents.forEach(System.out::println);
    }

}
