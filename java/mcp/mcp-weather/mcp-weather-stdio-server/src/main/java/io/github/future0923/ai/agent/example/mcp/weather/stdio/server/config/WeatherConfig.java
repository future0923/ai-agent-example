package io.github.future0923.ai.agent.example.mcp.weather.stdio.server.config;

import io.github.future0923.ai.agent.example.mcp.weather.stdio.server.tools.WeatherTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author future0923
 */
@Configuration
public class WeatherConfig {

    /**
     * 注册工具
     */
    @Bean
    public ToolCallbackProvider toolCallbackProvider(WeatherTools weatherTools) {
        return MethodToolCallbackProvider.builder().toolObjects(weatherTools).build();
    }
}
