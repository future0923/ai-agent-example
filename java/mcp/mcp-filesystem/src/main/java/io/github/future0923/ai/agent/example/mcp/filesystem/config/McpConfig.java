package io.github.future0923.ai.agent.example.mcp.filesystem.config;

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
