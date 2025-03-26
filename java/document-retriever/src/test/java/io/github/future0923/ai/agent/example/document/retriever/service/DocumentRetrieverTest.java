package io.github.future0923.ai.agent.example.document.retriever.service;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import io.github.future0923.ai.agent.example.document.retriever.DocumentRetrieverApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;

public class DocumentRetrieverTest extends DocumentRetrieverApplicationTest {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Test
    public void test() {
        DocumentRetriever documentRetriever = new DashScopeDocumentRetriever(
                new DashScopeApi(apiKey),
                DashScopeDocumentRetrieverOptions.builder()
                        // 知识库的索引名
                        .withIndexName("二手房信息")
                        // 密集相似度前K个 ，指定了在基于向量的（即密集）相似度计算中，返回最相关的前多少个结果。
                        .withDenseSimilarityTopK(10)
                        // 稀疏相似度前K个 ， 针对的是基于关键词匹配（即稀疏）的相似度计算。
                        .withSparseSimilarityTopK(10)
                        // 启用重写 ，查询重写可以帮助改进或优化用户的查询以获得更好的检索结果。
                        .withEnableRewrite(true)
                        // 重写模型名称
                        .withRewriteModelName("conv-rewrite-qwen-1.8b")
                        // 启用重新排序 ，是否启用结果重新排序。重新排序可以用来提升检索结果的相关性。
                        .withEnableReranking(true)
                        // 重新排序模型名称
                        .withRerankModelName("gte-rerank-hybrid")
                        // 重新排序最小分数 ，当进行结果重新排序时，只有得分高于这个阈值的结果才会被保留。
                        .withRerankMinScore(0.01f)
                        // 重新排序前N个，重新排序后返回的前N个最佳结果
                        .withRerankTopN(1)
                        .build());
        List<Document> result = documentRetriever.retrieve(
                Query.builder()
                        // 检索的内容
                        .text("我想找一个君悦豪庭B区1室的房源，面积在60平左右")
                        // 历史消息
                        .history(List.of())
                        // 上下文信息
                        .context(Map.of())
                        .build());
        Document document = result.get(0);
        // 获取元数据信息（提取到的key -> value）
        Map<String, Object> metadata = document.getMetadata();
        // MetaData格式化好的数据内容
        String formattedContent = document.getFormattedContent();
        // 搜索到的文档内容
        String text = document.getText();
    }
}