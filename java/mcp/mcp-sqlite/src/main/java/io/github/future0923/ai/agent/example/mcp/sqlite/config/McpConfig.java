package io.github.future0923.ai.agent.example.mcp.sqlite.config;

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
