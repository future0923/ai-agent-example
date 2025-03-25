package io.github.future0923.ai.agent.example.function.calling.tools;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 工具中可以使用 ToolContext 接收程序传递的信息
 *
 * @author future0923
 */
public class ToolContextTools {

    public record Customer(Long id, String tenantId, String name) {

        public static Customer findById(Long id, String tenantId) {
            return new Customer(id, tenantId, "张三");
        }
    }

    @Tool(description = "获取用户信息")
    public Customer getCustomerInfo(@ToolParam(description = "用户id") Long id, ToolContext toolContext) {
        // 从上下文中获取
        return Customer.findById(id, (String) toolContext.getContext().get("tenantId"));
    }

}
