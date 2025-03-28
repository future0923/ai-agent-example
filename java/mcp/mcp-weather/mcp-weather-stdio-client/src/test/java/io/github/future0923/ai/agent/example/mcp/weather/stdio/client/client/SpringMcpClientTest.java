package io.github.future0923.ai.agent.example.mcp.weather.stdio.client.client;

import io.github.future0923.ai.agent.example.mcp.weather.stdio.client.StdioClientMcpApplicationTest;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * 通过 spring-ai-mcp-client-spring-boot-starter 调用
 *
 * @author future0923
 */
public class SpringMcpClientTest extends StdioClientMcpApplicationTest {

    @Autowired
    private List<McpSyncClient> mcpSyncClients;

    @Test
    public void test() {
        for (McpSyncClient mcpSyncClient : mcpSyncClients) {
            McpSchema.CallToolResult result = mcpSyncClient.callTool(
                    // 调用mcp server 提供的 currentWeather
                    new McpSchema.CallToolRequest("currentWeather",
                    Map.of("city", "长春"))
            );
            System.out.println(result);
        }
    }
}
