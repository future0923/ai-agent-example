package io.github.future0923.ai.agent.example.vector.store.spring.ai.memory.server;

import io.github.future0923.ai.agent.example.vector.store.spring.ai.memory.SpringAiMemoryVectorStoreApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

/**
 * @author future0923
 */
class VectorMemoryTest extends SpringAiMemoryVectorStoreApplicationTest {

    @Autowired
    private VectorStore vectorStore;

    @Value("classpath:rag/terms-of-service.txt")
    private Resource resource;

    @Test
    public void add() {
        // TokenTextSplitter 类型的 ELT DocumentTransformer 转换 TextReader 读取到 ELT Document
        vectorStore.add(new TokenTextSplitter().transform(new TextReader(resource).read()));
        List<Document> documents = vectorStore.similaritySearch("取消预订");
        documents.forEach(System.out::println);
    }

    /**
     * 使用SearchRequest查询，提供查询内容并指定元数据筛选。
     * 如果 Document.getMetadata() 如下格式：
     * <pre>{@code
     * "country": <Text>,
     * "city": <Text>,
     * "year": <Number>,
     * "price": <Decimal>,
     * "isActive": <Boolean>
     * }</pre>
     */
    @Test
    public void searchRequest() {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        SearchRequest searchRequest = SearchRequest
                .builder()
                // 查询内容
                .query("退订")
                // 一个整数，指定要返回的相似文档的最大数量。这通常被称为“顶部K”搜索，或“K近邻算法”。
                .topK(SearchRequest.DEFAULT_TOP_K)
                // 返回相似度高于此值的文档。范围从0到1的双精度值，其中接近1的值表示较高的相似度。
                .similarityThreshold(SearchRequest.SIMILARITY_THRESHOLD_ACCEPT_ALL)
                // 设置为 0.0 则表示所有
                .similarityThresholdAll()
                // 基于ANTLR4的外部DSL，它接受滤波器表达式作为字符串。
                .filterExpression("""
                        country == 'UK' && year >= 2020 && isActive == true
                        Or
                        country == 'BG' && (city NOT IN ['Sofia', 'Plovdiv'] || price < 134.34)
                        """)
                .filterExpression(new Filter.Expression(Filter.ExpressionType.AND,
                        new Filter.Expression(Filter.ExpressionType.EQ, new Filter.Key("country"), new Filter.Value("UK")),
                        new Filter.Expression(Filter.ExpressionType.AND,
                                new Filter.Expression(Filter.ExpressionType.GTE, new Filter.Key("year"), new Filter.Value(2020)),
                                new Filter.Expression(Filter.ExpressionType.EQ, new Filter.Key("isActive"), new Filter.Value(true)))))
                .filterExpression(builder.and(
                        builder.eq("country", "UK"),
                        builder.and(
                                builder.gte("year", 2020),
                                builder.eq("isActive", true)
                        )
                ).build())
                .filterExpression(new FilterExpressionTextParser().parse("country == 'UK' && isActive == true && year >=2020"))
                .build();
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        documents.forEach(System.out::println);
    }

    @Test
    public void delete() {
        // 增加文档并设置元数据
        Document document = new Document("世界真大啊", Map.of("country", "中国"));
        vectorStore.add(List.of(document));
        // 通过id删除
        vectorStore.delete(List.of(document.getId()));
    }

    @Test
    public void deleteFilter() {
        // 增加文档并设置元数据
        Document bgDocument = new Document("世界真大啊", Map.of("country", "中国"));
        // 增加文档并设置元数据
        Document nlDocument = new Document("世界真大啊", Map.of("country", "中华人民共和国"));
        // 存储
        vectorStore.add(List.of(bgDocument, nlDocument));
        Filter.Expression filterExpression = new Filter.Expression(
                Filter.ExpressionType.EQ,
                new Filter.Key("country"),
                new Filter.Value("中国")
        );
        // 删除
        vectorStore.delete(filterExpression);
        // 验证
        SearchRequest request = SearchRequest.builder()
                .query("世界")
                .filterExpression("country == '中国'")
                .build();
        List<Document> results = vectorStore.similaritySearch(request);
        results.forEach(System.out::println);
    }
}