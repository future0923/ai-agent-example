package io.github.future0923.ai.agent.example.function.calling.tools;

import io.github.future0923.ai.agent.example.function.calling.AbstractFunctionCallingApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.execution.DefaultToolCallResultConverter;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.util.json.schema.JsonSchemaGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * @author future0923
 */
public class DateTimeToolsTest extends AbstractFunctionCallingApplicationTest {

    @Autowired
    private ChatClient.Builder builder;

    @Autowired
    private ChatModel chatModel;

    /**
     * 使用@Tool注解添加到ChatClient。
     * 我们要获取明天多少号就要知道今天的时间，大模型就会调用DateTimeTools的getCurrentTime方法获取当前时间
     */
    @Test
    public void getCurrentTime() {
        ChatClient chatClient = builder.build();
        String content = chatClient.prompt()
                .user("明天是多少号？")
                .tools(new DateTimeTools())
                .call()
                .content();
        System.out.println(content);
    }

    /**
     * 使用@Tool注解添加到ChatClient。
     * 当我们要设置提醒是，大模型就会调用DateTimeTools的setAlarm方法设置提醒
     */
    @Test
    public void setAlarm() {
        ChatClient chatClient = builder.build();
        String content = chatClient.prompt()
                .user("十分钟后提醒我")
                .tools(new DateTimeTools())
                .call()
                .content();
        System.out.println(content);
    }

    /**
     * 使用@Tool注解添加到ChatClient.Builder。
     * 我们要获取明天多少号就要知道今天的时间，大模型就会调用DateTimeTools的getCurrentTime方法获取当前时间
     */
    @Test
    public void getCurrentDateTimeBuilder() {
        ChatClient chatClient = builder
                .defaultTools(new DateTimeTools())
                .build();
        String content = chatClient.prompt()
                .user("明天是多少号？")
                .call()
                .content();
        System.out.println(content);
    }

    /**
     * 使用@Tool注解添加到ChatModel。
     * 我们要获取明天多少号就要知道今天的时间，大模型就会调用DateTimeTools的getCurrentTime方法获取当前时间
     */
    @Test
    public void getCurrentDateTimeChatModel() {
        ToolCallback[] tools = ToolCallbacks.from(new DateTimeTools());
        ToolCallingChatOptions toolCallingChatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(tools)
                .build();
        Prompt prompt = new Prompt("明天是多少号？", toolCallingChatOptions);
        String content = chatModel.call(prompt).getResult().getOutput().getText();
        System.out.println(content);
    }

    /**
     * 使用MethodToolCallback添加到ChatClient。
     * 我们要获取明天多少号就要知道今天的时间，大模型就会调用DateTimeTools的getCurrentTime方法获取当前时间
     */
    @Test
    public void getCurrentDateTimeMethodToolCallbackChatClient() {
        ChatClient chatClient = builder.build();
        String content = chatClient.prompt()
                .user("明天是多少号？")
                .tools(getCurrentDateTimeMethodToolCallback())
                .call()
                .content();
        System.out.println(content);
    }

    /**
     * 使用MethodToolCallback添加到ChatClient.Builder。
     * 我们要获取明天多少号就要知道今天的时间，大模型就会调用DateTimeTools的getCurrentTime方法获取当前时间
     */
    @Test
    public void getCurrentDateTimeMethodToolCallbackBuilder() {
        ChatClient chatClient = builder
                .defaultTools(getCurrentDateTimeMethodToolCallback())
                .build();
        String content = chatClient.prompt()
                .user("明天是多少号？")
                .call()
                .content();
        System.out.println(content);
    }

    /**
     * 使用MethodToolCallback添加到ChatModel。
     * 我们要获取明天多少号就要知道今天的时间，大模型就会调用DateTimeTools的getCurrentTime方法获取当前时间
     */
    @Test
    public void getCurrentDateTimeMethodToolCallbackChatModel() {
        ToolCallingChatOptions toolCallingChatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(getCurrentDateTimeMethodToolCallback())
                .build();
        Prompt prompt = new Prompt("明天是多少号？", toolCallingChatOptions);
        String content = chatModel.call(prompt).getResult().getOutput().getText();
        System.out.println(content);
    }


    /**
     * 构建 MethodToolCallback 对象
     */
    private MethodToolCallback getCurrentDateTimeMethodToolCallback() {
        // 反射获取工具方法
        Method getCurrentTime = ReflectionUtils.findMethod(DateTimeTools.class, "getCurrentTime");
        return MethodToolCallback.builder()
                // 定义工具名称、描述和输入schema的ToolDefinition实例。您可以使用ToolDefinition.Builder类构建它。
                // 必需的。
                .toolDefinition(ToolDefinition.builder(getCurrentTime)
                        // 描述信息
                        .description("获取用户所在时区的当前日期和时间。")
                        // 工具的输入参数的JSON schema。如果未提供，schema将根据方法参数自动生成。您可以使用@ToolParam注释提供有关输入参数的附加信息，例如描述或参数是必需的还是可选的。
                        // 格式例子，不使用与这个，这个没有
                        .inputSchema(JsonSchemaGenerator.generateForMethodInput(getCurrentTime))
                        //.inputSchema("""
                        //            {
                        //                "type": "object",
                        //                "properties": {
                        //                    "location": {
                        //                        "type": "string"
                        //                    },
                        //                    "unit": {
                        //                        "type": "string",
                        //                        "enum": ["C", "F"]
                        //                    }
                        //                },
                        //                "required": ["location", "unit"]
                        //            }
                        //        """)
                        // 工具名称
                        .name("DateTimeTools")
                        .build())
                // ToolMetadata实例，用于定义其他设置
                .toolMetadata(ToolMetadata.builder()
                        // 工具结果是应该直接返回给调用客户端还是传回模型。
                        // ture：工具调用的结果将直接返回给调用客户端。
                        // false：工具调用的结果返回给模型
                        .returnDirect(false)
                        .build())
                // 表示工具方法的Method。
                // 必需的。
                .toolMethod(getCurrentTime)
                // 包含工具方法的对象实例。
                // 如果方法是静态的，可以省略此参数。
                .toolObject(new DateTimeTools())
                // 用于将工具调用的结果转换为ToolCallResultConverter对象以打回AI模型的String实例。如果未提供，将使用默认转换器（DefaultToolCallResultConverter）。
                .toolCallResultConverter(new DefaultToolCallResultConverter())
                .build();
    }

    @Test
    public void useToolCallbackProvider() {
        ToolCallbackProvider provider = MethodToolCallbackProvider.builder().toolObjects(new DateTimeTools()).build();
        ChatClient chatClient = builder.defaultTools(provider).build();
        String content = chatClient.prompt()
                .tools(provider)
                .user("明天是多少号")
                .call()
                .content();
        System.out.println(content);
    }

}