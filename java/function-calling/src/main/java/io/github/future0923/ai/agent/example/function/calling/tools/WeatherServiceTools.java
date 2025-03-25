package io.github.future0923.ai.agent.example.function.calling.tools;

import org.springframework.ai.tool.annotation.ToolParam;

import java.util.function.Function;

/**
 * @author future0923
 */
public class WeatherServiceTools implements Function<WeatherServiceTools.WeatherRequest, WeatherServiceTools.WeatherResponse>{

    @Override
    public WeatherResponse apply(WeatherRequest weatherRequest) {
        // 模拟天气查询逻辑
        return new WeatherResponse(weatherRequest.city(), "晴天", 25);
    }

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
}
