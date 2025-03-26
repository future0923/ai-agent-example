package io.github.future0923.ai.agent.example.rag.service;

import io.github.future0923.ai.agent.example.rag.RagApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author future0923
 */
public class ConcatenationDocumentJoinerTest extends RagApplicationTest {

    @Autowired
    private ChatClient.Builder builder;

    /**
     * 从多个查询或多个数据源获取文档。为了有效地管理和整合这些文档，Spring AI提供了ConcatenationDocumentJoiner文档合并器。这个工具可以将多个来源的文档智能地合并成一个统一的文档集合。
     */
    @Test
    public void test() {
        Map<Query, List<List<Document>>> documentsForQuery = new HashMap<>();
        documentsForQuery.put(
                new Query("长春怎么样?"),
                List.of(
                        List.of(new Document("长春市是吉林的省会")), List.of(new Document("长春市被称为春城")),
                        List.of(new Document("长春市滑雪比较好")), List.of(new Document("长春市净月潭很好"))
                )
        );
        ConcatenationDocumentJoiner concatenationDocumentJoiner = new ConcatenationDocumentJoiner();
        List<Document> join = concatenationDocumentJoiner.join(documentsForQuery);
        // Document{id='9355d2b1-9306-4044-a62c-60863f81a1f1', text='长春市被称为春城', media='null', metadata={}, score=null}
        // Document{id='ee3aa308-206f-4195-a868-628c747162ce', text='长春市净月潭很好', media='null', metadata={}, score=null}
        // Document{id='251fc31a-9682-4354-997e-e6278a3e9d6d', text='长春市滑雪比较好', media='null', metadata={}, score=null}
        // Document{id='1e593180-2d00-4418-8150-7e2de08d415a', text='长春市是吉林的省会', media='null', metadata={}, score=null}
        join.forEach(System.out::println);
    }
}
