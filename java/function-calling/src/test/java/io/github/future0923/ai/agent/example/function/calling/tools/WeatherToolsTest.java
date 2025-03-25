package io.github.future0923.ai.agent.example.function.calling.tools;

import io.github.future0923.ai.agent.example.function.calling.AbstractFunctionCallingApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author future0923
 */
public class WeatherToolsTest extends AbstractFunctionCallingApplicationTest {

    @Autowired
    private ChatClient.Builder builder;

    @Autowired
    private ChatModel chatModel;

    /**
     * 使用@Bean注解添加到ChatClient。
     */
    @Test
    public void currentWeatherBeanChatClient() {
        ChatClient chatClient = builder.build();
        String content = chatClient.prompt()
                // 传入Bean的名称
                .tools("currentWeather")
                .user("长春天气怎么样？")
                .call()
                .content();
        System.out.println(content);
    }

    /**
     * 使用@Bean注解添加到ChatClient.Builder。
     */
    @Test
    public void currentWeatherBeanChatClientBuilder() {
        ChatClient chatClient = builder
                // 传入Bean的名称
                .defaultTools("currentWeather")
                .build();
        String content = chatClient.prompt()
                .user("长春天气怎么样？")
                .call()
                .content();
        System.out.println(content);
    }

    /**
     * 使用@Bean注解添加到ChatModel
     */
    @Test
    public void currentWeatherBeanChatModel() {
        ToolCallingChatOptions toolCallingChatOptions = ToolCallingChatOptions.builder()
                // 传入Bean的名称
                .toolNames("currentWeather")
                .build();
        Prompt prompt = new Prompt("长春天气怎么样？", toolCallingChatOptions);
        String content = chatModel.call(prompt).getResult().getOutput().getText();
        System.out.println(content);
    }

    /**
     * 通过FunctionToolCallback方式构建工具
     */
    private FunctionToolCallback<WeatherServiceTools.WeatherRequest, WeatherServiceTools.WeatherResponse> getCurrentWeatherFunctionToolCallback() {
        return FunctionToolCallback
                .builder("currentWeather", new WeatherServiceTools())
                .description("获取城市的天气情况")
                .inputType(WeatherServiceTools.WeatherRequest.class)
                .toolMetadata(ToolMetadata.builder()
                        .returnDirect(false)
                        .build())
                .build();
    }

    /**
     * 通过FunctionToolCallback方式将工具添加到ChatClient
     */
    @Test
    public void currentWeatherFunctionToolCallbackChatClient() {
        ChatClient chatClient = builder.build();
        String content = chatClient.prompt()
                .tools(getCurrentWeatherFunctionToolCallback())
                .user("长春天气怎么样？")
                .call()
                .content();
        System.out.println(content);
    }

    /**
     * 通过FunctionToolCallback方式将工具添加到ChatClient.Builder
     */
    @Test
    public void currentWeatherFunctionToolCallbackChatClientBuilder() {
        ChatClient chatClient = builder
                .defaultTools(getCurrentWeatherFunctionToolCallback())
                .build();
        String content = chatClient.prompt()
                .user("长春天气怎么样？")
                .call()
                .content();
        System.out.println(content);
    }

    /**
     * 使用WeatherFunctionToolCallback注解添加到ChatModel
     */
    @Test
    public void currentWeatherFunctionToolCallbackChatModel() {
        ToolCallingChatOptions toolCallingChatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(getCurrentWeatherFunctionToolCallback())
                .build();
        Prompt prompt = new Prompt("长春天气怎么样？", toolCallingChatOptions);
        String content = chatModel.call(prompt).getResult().getOutput().getText();
        System.out.println(content);
    }
}