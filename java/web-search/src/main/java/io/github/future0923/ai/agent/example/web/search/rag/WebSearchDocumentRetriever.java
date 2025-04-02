package io.github.future0923.ai.agent.example.web.search.rag;

import io.github.future0923.ai.agent.example.web.search.dto.websearch.GenericSearchResult;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.ranking.DocumentRanker;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;

import java.util.List;

/**
 * @author future0923
 */
public class WebSearchDocumentRetriever implements DocumentRetriever {

    private static final Logger logger = LoggerFactory.getLogger(WebSearchDocumentRetriever.class);

    private final IQSSearchService iqsSearchService;

    private final DataClean dataClean;

    private final DocumentRanker documentRanker;

    private final int maxResults;

    private final boolean enableRanker;

    private WebSearchDocumentRetriever(Builder builder) {

        this.iqsSearchService = builder.iqsSearchService;
        this.maxResults = builder.maxResults;
        this.dataClean = builder.dataCleaner;
        this.enableRanker = builder.enableRanker;
        this.documentRanker = builder.documentRanker;
    }

    @NotNull
    @Override
    public List<Document> retrieve(@NotNull Query query) {
        // iqs搜索实时数据
        GenericSearchResult searchResult = iqsSearchService.search(query);
        // 清洗结果
        List<Document> documentList = dataClean.getData(searchResult);
        // 限制最大结果数
        List<Document> documents = dataClean.limitResults(documentList, maxResults);
        // 文档重排序
        return enableRanker ? ranking(query, documents) : documents;
    }

    private List<Document> ranking(Query query, List<Document> documents) {
        if (documents.size() == 1) {
            // 只有一个时，不需要 rank
            return documents;
        }
        try {
            return documentRanker.rank(query, documents);
        } catch (Exception e) {
            // 降级返回原始结果
            logger.error("ranking error", e);
            return documents;
        }
    }

    public static WebSearchDocumentRetriever.Builder builder() {
        return new WebSearchDocumentRetriever.Builder();
    }


    public static final class Builder {

        private IQSSearchService iqsSearchService;

        private int maxResults;

        private DataClean dataCleaner;

        private DocumentRanker documentRanker;

        // 默认开启 ranking
        private Boolean enableRanker = true;

        public WebSearchDocumentRetriever.Builder searchService(IQSSearchService searchService) {

            this.iqsSearchService = searchService;
            return this;
        }

        public WebSearchDocumentRetriever.Builder dataCleaner(DataClean dataCleaner) {

            this.dataCleaner = dataCleaner;
            return this;
        }

        public WebSearchDocumentRetriever.Builder maxResults(int maxResults) {

            this.maxResults = maxResults;
            return this;
        }

        public WebSearchDocumentRetriever.Builder documentRanker(DocumentRanker documentRanker) {
            this.documentRanker = documentRanker;
            return this;
        }

        public WebSearchDocumentRetriever.Builder enableRanker(Boolean enableRanker) {
            this.enableRanker = enableRanker;
            return this;
        }

        public WebSearchDocumentRetriever build() {
            return new WebSearchDocumentRetriever(this);
        }
    }
}
