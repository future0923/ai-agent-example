# 模型上下文协议(Model Context Protocol)

[模型上下文协议(MCP)](https://modelcontextprotocol.io/introduction)是一个开放协议，它规范了应用程序如何向大型语言模型（LLM）提供上下文。

MCP 提供了一种统一的方式将 AI 模型连接到不同的数据源和工具，它定义了统一的集成方式。

在开发智能体（Agent）的过程中，我们经常需要将将智能体与数据和工具集成，MCP 以标准的方式规范了智能体与数据及工具的集成方式，可以帮助您在 LLM 之上构建智能体（Agent）和复杂的工作流。

目前已经有大量的服务接入并提供了 MCP server 实现，当前这个生态正在以非常快的速度不断的丰富中，具体可参见：[MCP Servers](https://github.com/modelcontextprotocol/servers)。

Java也可以开发MCP，[MCP Java Sdk](https://github.com/modelcontextprotocol/java-sdk)，文档[地址](https://modelcontextprotocol.io/sdk/java/mcp-overview)。

## Spring API MCP

Spring AI MCP 为模型上下文协议提供 Java 和 Spring 框架集成。它使 Spring AI 应用程序能够通过标准化的接口与不同的数据源和工具进行交互，支持同步和异步通信模式。

**MCP Client**

![img.png](/images/ghwqdjqoifhq.png){v-zoom}{loading="lazy"}

MCP Client是模型上下文协议（MCP）架构中的关键组件，负责建立和管理与MCP服务器的连接。它实现协议的客户端，处理：
- 协议版本协商以确保与服务器的兼容性
- 确定可用特征的能力协商
- 消息传输和JSON-RPC通信
- 工具发现和执行
- 资源访问和管理
- 提示系统交互
- 可选功能：
  - 根管理
  - 采样支持
- 同步和异步操作
- 交通选择：
  - 基于Stdio的传输，用于基于进程的通信
  - Java基于HttpClient的SSE客户端传输
  - 用于响应式HTTP流的WebFlux SSE客户端传输

**MCP Server**

![img.png](/images/jwfiohqhfwijdiwasadsa.png){v-zoom}{loading="lazy"}

MCP Server是模型上下文协议（MCP）架构中的基础组件，为客户端提供工具、资源和功能。它实现协议的服务器端，负责：

- 服务器端协议操作实现
  - 工具暴露和发现
  - 使用基于URI的访问进行资源管理
  - 及时提供和处理模板
  - 与客户的能力谈判
  - 结构化日志记录和通知
- 并发客户端连接管理
- 同步和异步API支持
- 运输实施：
  - 基于Stdio的传输，用于基于进程的通信
  - 基于Servlet的SSE服务器传输
  - 用于响应式HTTP流的WebFlux SSE服务器传输
  - 用于基于servlet的HTTP流的WebMVC SSE服务器传输

## 示例

### 文件系统

基于社区的[filesystem](https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem).

示例代码[地址](https://github.com/future0923/ai-agent-example/tree/main/java/mcp/mcp-filesystem).

初始化配置

```java
import org.springframework.ai.mcp.client.McpClient;
import org.springframework.ai.mcp.client.McpSyncClient;
import org.springframework.ai.mcp.client.transport.ServerParameters;
import org.springframework.ai.mcp.client.transport.StdioClientTransport;
import org.springframework.ai.mcp.spring.McpFunctionCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * @author future0923
 */
@Configuration
public class McpConfig {

    /**
     * 创建同步的 McpSyncClient
     */
    @Bean(destroyMethod = "close")
    public McpSyncClient mcpClient() {
        // https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem
        var stdioParams = ServerParameters.builder("npx")
                .args(
                        "-y",
                        "@modelcontextprotocol/server-filesystem",
                        System.getProperty("user.dir")
                )
                .build();
        var mcpClient = McpClient.using(new StdioClientTransport(stdioParams))
                .requestTimeout(Duration.ofSeconds(10)).sync();
        var init = mcpClient.initialize();
        System.out.println("MCP Initialized: " + init);
        return mcpClient;

    }

    /**
     * 注册McpFunctionCallback
     */
    @Bean
    public List<McpFunctionCallback> functionCallbacks(McpSyncClient mcpClient) {
        return mcpClient.listTools(null)
                //获取可用的工具
                .tools()
                .stream()
                // 自动注册 MCP 可用的函数回调（Function Callbacks）
                .map(tool -> new McpFunctionCallback(mcpClient, tool))
                .toList();
    }

}
```

使用Filesystem操作文件

```java
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
```

### 操作SQLite

基于社区的[SQLite](https://github.com/modelcontextprotocol/servers/tree/main/src/sqlite).

示例代码[地址](https://github.com/future0923/ai-agent-example/tree/main/java/mcp/mcp-sqlite).

初始化配置:

```java
import org.springframework.ai.mcp.client.McpClient;
import org.springframework.ai.mcp.client.McpSyncClient;
import org.springframework.ai.mcp.client.transport.ServerParameters;
import org.springframework.ai.mcp.client.transport.StdioClientTransport;
import org.springframework.ai.mcp.spring.McpFunctionCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

/**
 * @author future0923
 */
@Configuration
public class McpConfig {

    /**
     * 安装uvx
     * curl -LsSf https://astral.sh/uv/install.sh | sh
     * 先手动运行，因为会安装依赖，要不肯定10秒超时 uvx mcp-server-sqlite --db-path test.db
     */
    @Bean(destroyMethod = "close")
    public McpSyncClient mcpClient() {
        var stdioParams = ServerParameters.builder("uvx")
                .args(
                        "mcp-server-sqlite",
                        "--db-path",
                        Paths.get(System.getProperty("user.dir"), "test.db").toString()
                )
                .build();
        var mcpClient = McpClient.using(new StdioClientTransport(stdioParams))
                .requestTimeout(Duration.ofSeconds(10)).sync();
        var init = mcpClient.initialize();
        System.out.println("MCP Initialized: " + init);
        return mcpClient;
    }

    @Bean
    public List<McpFunctionCallback> functionCallbacks(McpSyncClient mcpClient) {
        return mcpClient.listTools(null)
                //获取可用的工具
                .tools()
                .stream()
                // 自动注册 MCP 可用的函数回调（Function Callbacks）
                .map(tool -> new McpFunctionCallback(mcpClient, tool))
                .toList();
    }

}
```

使用

```java
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
```

### 自己开发


**Spring AI 客户端 starter**
- `spring-ai-mcp-client-spring-boot-starter` 提供STDIO和基于HTTP的SSE支持的核心启动器
- `spring-ai-mcp-client-webflux-spring-boot-starter` 基于WebFlux的SSE传输实现

**Spring AI 服务端 starter**
- `spring-ai-mcp-server-spring-boot-starter` 支持STDIO传输的核心服务器
- `spring-ai-mcp-server-webmvc-spring-boot-starter` 基于Spring MVC的SSE传输实现
- `spring-ai-mcp-server-webflux-spring-boot-starter` 基于WebFlux的SSE传输实现

#### STDIO