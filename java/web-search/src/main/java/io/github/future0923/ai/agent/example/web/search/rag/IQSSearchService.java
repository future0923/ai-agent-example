package io.github.future0923.ai.agent.example.web.search.rag;

import io.github.future0923.ai.agent.example.web.search.config.IQSSearchProperties;
import io.github.future0923.ai.agent.example.web.search.dto.websearch.GenericSearchResult;
import io.github.future0923.ai.agent.example.web.search.exception.ProjectException;
import org.springframework.ai.rag.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Objects;

/**
 * @author future0923
 */
@Component
public class IQSSearchService {

    private final RestClient restClient;

    private static final String BASE_URL = "https://cloud-iqs.aliyuncs.com";

    public IQSSearchService(IQSSearchProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    httpHeaders.set("user-agent", String.format("%s/%s; java/%s; platform/%s; processor/%s", "SpringAiAlibabaPlayground", "1.0.0", System.getProperty("java.version"), System.getProperty("os.name"), System.getProperty("os.arch")));
                    httpHeaders.set("X-API-Key", properties.getApiKey());
                })
                .build();
    }

    public GenericSearchResult search(Query query) {
        // 搜索
        ResponseEntity<GenericSearchResult> response = restClient.get()
                .uri(
                        "/search/genericSearch?query={query}&timeRange={timeRange}",
                        query.text().length() >= 100 ? query.text().substring(0, 99) : query.text(),
                        "OneWeek"
                )
                .retrieve()
                .toEntity(GenericSearchResult.class);
        if ((!Objects.equals(response.getStatusCode(), HttpStatus.OK)) || Objects.isNull(response.getBody())) {
            throw new ProjectException("查询WebSearch失败");
        }
        return response.getBody();
    }
}
