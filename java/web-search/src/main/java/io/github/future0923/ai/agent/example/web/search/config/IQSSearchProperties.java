package io.github.future0923.ai.agent.example.web.search.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author future0923
 */
@Configuration
@ConfigurationProperties(prefix = "spring.iqs.search")
public class IQSSearchProperties {

    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
