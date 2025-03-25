package io.github.future0923.ai.agent.example.vector.store.dash.scope.service;

import com.alibaba.cloud.ai.dashscope.rag.DashScopeCloudStore;
import io.github.future0923.ai.agent.example.vector.store.dash.scope.DashScopeVectorStoreApplicationTest;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author future0923
 */
public class DashScopeCloudStoreTest extends DashScopeVectorStoreApplicationTest {

    @Autowired
    private DashScopeCloudStore dashScopeCloudStore;

    public void test() {
        List<Document> documents = dashScopeCloudStore.similaritySearch("");
    }

}
