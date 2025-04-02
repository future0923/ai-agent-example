package io.github.future0923.ai.agent.example.document.retriever.document.ranker;

import com.alibaba.cloud.ai.dashscope.rerank.DashScopeRerankOptions;
import com.alibaba.cloud.ai.model.RerankModel;
import com.alibaba.cloud.ai.model.RerankRequest;
import com.alibaba.cloud.ai.model.RerankResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.ranking.DocumentRanker;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * DocumentRanker文档重排序
 *
 * @author future0923
 */
@Component
public class DashScopeDocumentRanker implements DocumentRanker {

    private static final Logger logger = LoggerFactory.getLogger(DashScopeDocumentRanker.class);

    private final RerankModel rerankModel;

    public DashScopeDocumentRanker(RerankModel rerankModel) {
        this.rerankModel = rerankModel;
    }

    @NotNull
    @Override
    public List<Document> rank(@NotNull Query query, @NotNull List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return new ArrayList<>();
        }
        try {
            List<Document> reorderDocs = new ArrayList<>();

            // The caller controls the number of documents
            DashScopeRerankOptions rerankOptions = DashScopeRerankOptions.builder()
                    .withTopN(documents.size())
                    .build();

            if (StringUtils.hasText(query.text())) {
                // The assembly parameter calls rerankModel
                RerankRequest rerankRequest = new RerankRequest(
                        query.text(),
                        documents,
                        rerankOptions
                );
                RerankResponse rerankResp = rerankModel.call(rerankRequest);

                rerankResp.getResults().forEach(res -> {
                    Document outputDocs = res.getOutput();

                    // Find and add to a new list
                    Optional<Document> foundDocsOptional = documents.stream()
                            .filter(doc ->
                            {
                                // debug rerank output.
                                logger.debug("DashScopeDocumentRanker#rank() doc id: {}, outputDocs id: {}", doc.getId(), outputDocs.getId());
                                return Objects.equals(doc.getId(), outputDocs.getId());
                            })
                            .findFirst();

                    foundDocsOptional.ifPresent(reorderDocs::add);
                });
            }

            return reorderDocs;
        }
        catch (Exception e) {
            // Further processing is done depending on the type of exception
            logger.error("ranker error", e);
            return documents;
        }
    }
}
