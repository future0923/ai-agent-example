package io.github.future0923.ai.agent.example.mcp.filesystem.config;

import io.github.future0923.ai.agent.example.mcp.filesystem.McpFileSystemApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.spring.McpFunctionCallback;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

/**
 * @author future0923
 */
class FileSystemTest extends McpFileSystemApplicationTest {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private List<McpFunctionCallback> functionCallbacks;

    @Test
    public void predefinedQuestions() {
        var chatClient = chatClientBuilder
                .defaultTools(functionCallbacks.toArray(new McpFunctionCallback[0]))
                .build();

        // Question 1
        String question1 = "你能解释一下 spring-ai-mcp-overview.txt 文件的内容吗？";
        System.out.println("问: " + question1);
        System.out.print("答: ");
        Flux<String> flux1 = chatClient.prompt(question1).stream().content();
        StepVerifier.create(flux1)
                .thenConsumeWhile(res -> {
                    System.out.print(res);
                    return true;
                })
                .verifyComplete();
        // Question 2
        System.out.println();
        String question2 = "请将 spring-ai-mcp-overview.txt 文件的内容摘要转换为中文并以 Markdown 格式存储到一个新的 summary.md 文件中。";
        System.out.println("问: " + question2);
        System.out.print("答: ");
        Flux<String> flux2 = chatClient.prompt(question2).stream().content();
        StepVerifier.create(flux2)
                .thenConsumeWhile(res -> {
                    System.out.print(res);
                    return true;
                })
                .verifyComplete();
    }

}