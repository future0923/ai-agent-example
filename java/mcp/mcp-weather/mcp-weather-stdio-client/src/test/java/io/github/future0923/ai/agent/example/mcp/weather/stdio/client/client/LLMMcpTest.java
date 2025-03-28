package io.github.future0923.ai.agent.example.mcp.weather.stdio.client.client;

import io.github.future0923.ai.agent.example.mcp.weather.stdio.client.StdioClientMcpApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * @author future0923
 */
public class LLMMcpTest extends StdioClientMcpApplicationTest {

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Test
    public void test() {
        var chatClient = chatClientBuilder
                .defaultTools(toolCallbackProvider)
                .build();

        // Question 1
        String question1 = "长春的天气怎么样？";
        System.out.println("问: " + question1);
        System.out.print("答: ");
        Flux<String> flux1 = chatClient.prompt(question1).stream().content();
        StepVerifier.create(flux1)
                .thenConsumeWhile(res -> {
                    System.out.print(res);
                    return true;
                })
                .verifyComplete();
    }


}
