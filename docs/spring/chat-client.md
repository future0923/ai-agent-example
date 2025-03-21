# Chat Client

## 简介

该ChatClient为与AI模型通信提供了流畅的API。它支持同步和反应式（Reactive）编程模型。

ChatModel、Message、ChatMemory 等原子 API 相比，使用 ChatClient 可以将与 LLM 及其他组件交互的复杂性隐藏在背后，因为基于 LLM 的应用程序通常要多个组件协同工作（例如，提示词模板、聊天记忆、LLM Model、输出解析器、RAG 组件：嵌入模型和存储），并且通常涉及多个交互，因此协调它们会让编码变得繁琐。当然使用 ChatModel 等原子 API 可以为应用程序带来更多的灵活性，成本就是您需要编写大量样板代码。

ChatClient 类似于应用程序开发中的服务层，它为应用程序直接提供 AI 服务，开发者可以使用 ChatClient Fluent API 快速完成一整套 AI 交互流程的组装。

包括一些基础功能，如：
- 定制和组装模型的输入（Prompt）
- 格式化解析模型的输出（Structured Output）
- 调整模型交互参数（ChatOptions）
还支持更多高级功能：
- 聊天记忆（Chat Memory）
- 工具/函数调用（Function Calling）
- RAG

## 创建ChatClient

使用 `ChatClient.Builder` 对象创建 `ChatClient`。

- 使用 `Spring Boot` 自动配置的 `ChatClient.Builder`

```java
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        // 可以指定很多参数
        return builder.build();
    }
}
```

- 您可以禁用ChatClient.Builder自动配置，方法是将属性设置为 `spring.ai.chat.client.enabled=false`。如果同时使用多个聊天模型，这将非常有用。然后，为您需要的每个ChatModel以编程方式创建一个ChatClient.Builder实例：

```java
public void chatClient(ChatModel chatModel) {
    // 创建 ChatClient.Builder
    ChatClient.Builder builder = ChatClient.builder(chatModel);
    // 创建 ChatClient
    ChatClient chatClient = ChatClient.create(chatModel);
}
```

## ChatClient.Builder

通过`ChatClient.Builder`创建`ChatClient`，它提供了许多配置选项。这些配置项当你使用的ChatClient时候自动携带（通常配置公共参数），使用ChatClient的API也可以对每个请求单独设置这些配置。

```java
public interface ChatClient {
    
    interface Builder {

        /**
         * 设置Advisors
         */
        Builder defaultAdvisors(Advisor... advisor);

        /**
         * 设置Advisors
         */
        Builder defaultAdvisors(Consumer<AdvisorSpec> advisorSpecConsumer);

        /**
         * 设置Advisors
         */
        Builder defaultAdvisors(List<Advisor> advisors);

        /**
         * 设置聊天参数
         */
        Builder defaultOptions(ChatOptions chatOptions);

        /**
         * 设置用户消息
         */
        Builder defaultUser(String text);

        /**
         * 设置用户消息
         */
        Builder defaultUser(Resource text, Charset charset);

        /**
         * 设置用户消息
         */
        Builder defaultUser(Resource text);

        /**
         * 设置用户消息
         */
        Builder defaultUser(Consumer<PromptUserSpec> userSpecConsumer);

        /**
         * 设置系统消息
         */
        Builder defaultSystem(String text);

        /**
         * 设置系统消息
         */
        Builder defaultSystem(Resource text, Charset charset);

        /**
         * 设置系统消息
         */
        Builder defaultSystem(Resource text);

        /**
         * 设置系统消息
         */
        Builder defaultSystem(Consumer<PromptSystemSpec> systemSpecConsumer);

        /**
         * 设置function calling
         */
        Builder defaultTools(String... toolNames);

        /**
         * 设置function calling
         */
        Builder defaultTools(FunctionCallback... toolCallbacks);

        /**
         * 设置function calling
         */
        Builder defaultTools(List<ToolCallback> toolCallbacks);

        /**
         * 设置function calling
         */
        Builder defaultTools(Object... toolObjects);

        /**
         * 设置function calling
         */
        Builder defaultTools(ToolCallbackProvider... toolCallbackProviders);

        /**
         * 设置function calling
         */
        Builder defaultToolContext(Map<String, Object> toolContext);

        /**
         * 创建ChatClient实例
         */
        ChatClient build();

    }
}
```

作用

| 属性       | 功能                                                                                                              |
|----------|-----------------------------------------------------------------------------------------------------------------|
| Advisors | **非常重要**。Advisors API提供了一种灵活而强大的方式来拦截、修改和增强Spring应用程序中的AI驱动交互。通过利用Advisors API，开发人员可以创建更复杂、可重用和可维护的AI组件。下面会详细介绍 |
| Options  | **聊天选项**。通过 [ChatOptions](chat-model#chatoptions) 可以设置大模型请求的参数。                                                 |
| User     | 设置携带的用户类型的消息。具体可以查看 [UserMessage](chat-model#message) 了解。                                                       |
| System   | 设置携带的系统类型的消息。具体可以查看 [SystemMessage](chat-model#message) 了解。                                                     |
| Tools    | 设置请求需要添加的工具。具体可以查看 [Function calling](function-calling) 了解。 之前叫 `FunctionCallback`，现在改为 `ToolCallback`。         |
