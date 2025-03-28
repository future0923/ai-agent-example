package io.github.future0923.ai.agent.example.mcp.sqlite.config;

import io.github.future0923.ai.agent.example.mcp.sqlite.McpSqliteApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.mcp.spring.McpFunctionCallback;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

/**
 * @author future0923
 */
class SqliteTest extends McpSqliteApplicationTest {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private List<McpFunctionCallback> functionCallbacks;

    /**
     * 安装uvx
     * curl -LsSf https://astral.sh/uv/install.sh | sh
     * 先手动运行，因为会安装依赖，要不肯定10秒超时 uvx mcp-server-sqlite --db-path test.db
     */
    @Test
    public void predefinedQuestions() {
        var chatClient = chatClientBuilder
                .defaultTools(functionCallbacks.toArray(new McpFunctionCallback[0]))
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .build();

        // Question 1
        String question1 = "你能连接到我的 SQLite 数据库，告诉我有哪些产品，以及它们的价格吗？";
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
        String question2 = "数据库中所有产品的平均价格是多少？";
        System.out.println("\n问: " + question2);
        System.out.println("答: ");
        Flux<String> flux2 = chatClient.prompt(question2).stream().content();
        StepVerifier.create(flux2)
                .thenConsumeWhile(res -> {
                    System.out.print(res);
                    return true;
                })
                .verifyComplete();
        // Question 3
        String question3 = "你能分析一下价格分布并提出任何定价优化建议吗？";
        System.out.println("\n问: " + question3);
        System.out.println("答: " );
        Flux<String> flux3 = chatClient.prompt(question3).stream().content();
        StepVerifier.create(flux3)
                .thenConsumeWhile(res -> {
                    System.out.print(res);
                    return true;
                })
                .verifyComplete();
        // Question 4
        String question4 = "你能帮我设计并创建一个新的表格，用于存储客户订单吗？";
        System.out.println("\n问: " + question4);
        System.out.println("答: ");
        Flux<String> flux4 = chatClient.prompt(question4).stream().content();
        StepVerifier.create(flux4)
                .thenConsumeWhile(res -> {
                    System.out.print(res);
                    return true;
                })
                .verifyComplete();
    }

}