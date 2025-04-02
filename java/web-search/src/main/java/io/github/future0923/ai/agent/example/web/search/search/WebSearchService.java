package io.github.future0923.ai.agent.example.web.search.search;

import io.github.future0923.ai.agent.example.web.search.advisor.ThinkingContentAdvisor;
import io.github.future0923.ai.agent.example.web.search.rag.DataClean;
import io.github.future0923.ai.agent.example.web.search.rag.IQSSearchService;
import io.github.future0923.ai.agent.example.web.search.rag.WebSearchDocumentRetriever;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.postretrieval.ranking.DocumentRanker;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author future0923
 */
@Service
public class WebSearchService {

    private final ChatClient chatClient;

    private final WebSearchDocumentRetriever webSearchDocumentRetriever;

    private final QueryTransformer queryTransformer;

    private final QueryExpander queryExpander;

    private final DocumentJoiner documentJoiner;

    private final QueryAugmenter queryAugmenter;

    private final ThinkingContentAdvisor thinkingContentAdvisor;

    private final ChatMemory chatMemory;

    public WebSearchService(ChatClient.Builder builder,
                            IQSSearchService searchService,
                            DataClean dataClean,
                            DocumentRanker documentRanker,
                            QueryTransformer queryTransformer, QueryExpander queryExpander, DocumentJoiner documentJoiner, QueryAugmenter queryAugmenter, ChatMemory chatMemory) {
        this.chatClient = builder.build();
        this.queryTransformer = queryTransformer;
        this.queryExpander = queryExpander;
        this.documentJoiner = documentJoiner;
        this.queryAugmenter = queryAugmenter;
        this.chatMemory = chatMemory;
        this.thinkingContentAdvisor = new ThinkingContentAdvisor(1);
        this.webSearchDocumentRetriever = WebSearchDocumentRetriever.builder()
                .searchService(searchService)
                .dataCleaner(dataClean)
                .maxResults(2)
                .enableRanker(true)
                .documentRanker(documentRanker)
                .build();
    }

    public Flux<String> chat(String query, String chatId) {
        return chatClient.prompt()
                .user(query)
                .advisors(
                        RetrievalAugmentationAdvisor.builder()
                                .documentRetriever(webSearchDocumentRetriever)
                                .queryTransformers(queryTransformer)
                                .queryExpander(queryExpander)
                                .documentJoiner(documentJoiner)
                                .queryAugmenter(queryAugmenter)
                                .build()
                )
                .advisors(new MessageChatMemoryAdvisor(chatMemory))
                .advisors(advisorSpec -> advisorSpec
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .advisors(thinkingContentAdvisor)
                .advisors(new SimpleLoggerAdvisor())
                .stream()
                .content();
    }
}
