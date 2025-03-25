package io.github.future0923.ai.agent.example.function.calling.tools;

import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

/**
 * @author future0923
 */
@Configuration(proxyBeanMethods = false)
public class WeatherTools {

    /**
     * tool请求
     */
    public record WeatherRequest(@ToolParam(description = "城市信息") String city) {

    }

    /**
     * tool响应
     */
    public record WeatherResponse(@ToolParam(description = "城市信息") String city,
                                  @ToolParam(description = "天气情况") String condition,
                                  @ToolParam(description = "温度") int temperature) {

    }

    @Bean("currentWeather")
    @Description("获取城市的天气情况")
    public Function<WeatherRequest, WeatherResponse> currentWeather() {
        return request -> {
            // 模拟天气查询逻辑
            return new WeatherResponse(request.city(), "晴天", 25);
        };
    }
}
