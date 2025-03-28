package io.github.future0923.ai.agent.example.mcp.weather.stdio.client.client;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * 使用 modelcontextprotocol client 调用server
 *
 * @author future0923
 */
public class McpClientTest {

    @Test
    public void test() {
        var stdioParams = ServerParameters.builder("java")
                .args(
                        "-jar",
                        "/Users/weilai/Documents/ai-agent-example/java/mcp/mcp-weather/mcp-weather-stdio-server/target/mcp-weather-stdio-server-1.0.0-SNAPSHOT.jar")
                .build();

        var transport = new StdioClientTransport(stdioParams);
        var client = McpClient.sync(transport).build();

        client.initialize();

        // List and demonstrate tools
        McpSchema.ListToolsResult toolsList = client.listTools();
        System.out.println("Available Tools = " + toolsList);

        McpSchema.CallToolResult weatherForcastResult = client.callTool(new McpSchema.CallToolRequest("currentWeather",
                Map.of("city", "长春")));
        System.out.println("Weather Forcast: " + weatherForcastResult);

        client.closeGracefully();
    }
}
