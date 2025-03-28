package io.github.future0923.ai.agent.example.mcp.weather.stdio.server.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * @author future0923
 */
@Service
public class WeatherTools {

    /**
     * tool响应
     */
    public record WeatherResponse(@ToolParam(description = "城市信息") String city,
                                  @ToolParam(description = "天气情况") String condition,
                                  @ToolParam(description = "温度") int temperature) {

    }

    @Tool(description = "获取城市的天气情况")
    public WeatherResponse currentWeather(@ToolParam(description = "城市信息") String city) {
        // 模拟天气查询逻辑
        return new WeatherResponse(city, "晴天", 25);
    }
}
