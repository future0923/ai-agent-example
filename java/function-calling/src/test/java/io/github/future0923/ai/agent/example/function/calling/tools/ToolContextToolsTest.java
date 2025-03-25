package io.github.future0923.ai.agent.example.function.calling.tools;

import io.github.future0923.ai.agent.example.function.calling.AbstractFunctionCallingApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author future0923
 */
public class ToolContextToolsTest extends AbstractFunctionCallingApplicationTest {

    @Autowired
    private ChatClient.Builder builder;

    @Autowired
    private ChatModel chatModel;

    /**
     * 使用 ChatClient 的 toolContext 设置上下文信息
     */
    @Test
    public void contextChatClient() {
        ChatClient chatClient = builder.build();
        String content = chatClient.prompt("获取id为18的用户信息")
                .tools(new ToolContextTools())
                .toolContext(Map.of("tenantId", "acme"))
                .call()
                .content();
        System.out.println(content);
    }

    /**
     * 使用 ChatModel 的 toolContext 设置上下文信息
     */
    @Test
    public void contextChatModel() {
        ToolCallback[] customerTools = ToolCallbacks.from(new ToolContextTools());
        ChatOptions chatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(customerTools)
                .toolContext(Map.of("tenantId", "acme"))
                .build();
        Prompt prompt = new Prompt("获取id为18的用户信息", chatOptions);
        System.out.println(chatModel.call(prompt).getResult().getOutput().getText());
    }
}