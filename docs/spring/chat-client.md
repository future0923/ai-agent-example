# Chat Client

示例代码[源码](https://github.com/future0923/ai-agent-example/tree/main/java/chat-client)

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

**ChatClientBuilderTest**

```java

/**
 * ChatClientBuilder构建的ChatClient可以设置公共参数，每次请求都会生效
 */
public class ChatClientBuilderTest extends AbstractChatClientApplicationTest {

    @Autowired
    private ChatClient.Builder builder;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private VectorStore vectorStore;

    @Test
    public void chat() {
        // 可以使用Bean管理ChatClient
        ChatClient client = builder
                // 携带的默认系统角色信息
                .defaultSystem("""
                        # 角色
                        你是一个旅游小助手，可以帮助取消机票预订。
                        ## 技能
                        今天的日期是 {current_date}。
                        ### 技能 1：快捷取消预订
                        1. 您必须始终从用户处获取以下信息：预订号、客户姓名。
                        ## 限制:
                        - 仅处理与旅游信息相关的内容，拒绝回答与AI选房无关的任何话题。
                        """)
                // 填充 PromptTemplate 参数
                .defaultSystem(promptSystemSpec -> promptSystemSpec.param("current_date", LocalDate.now().toString()))
                // 添加 Advisor
                .defaultAdvisors(
                        // 内存聊天记忆
                        new PromptChatMemoryAdvisor(chatMemory),
                        // 日志打印
                        new SimpleLoggerAdvisor(),
                        // 向量信息
                        new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().build())
                )
                // 设置参数
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .withTopP(0.7)
                                .build()
                )
                // 传递 Tool 只简单用一下，后面有详细的 Tools 使用.
                // BookingTools#cancelBooking()
                .defaultTools("cancelBooking")
                .build();
        Flux<String> flux = client
                .prompt()
                .user("帮我取消预订，张三 101，并告诉我费用是多少")
                .advisors(advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, 1))
                .advisors(advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, AbstractChatMemoryAdvisor.DEFAULT_CHAT_MEMORY_RESPONSE_SIZE))
                // 向量查询需要对接向量模型才能更好搜索
                //.advisors(advisorSpec -> new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().query(message).build()))
                .stream()
                .content();
        StepVerifier.create(flux)
                .thenConsumeWhile(res -> {
                    System.out.print(res);
                    return true;
                })
                .verifyComplete();
    }

}
```

**ChatClientTest**

```java
/**
 * ChatClient请求时设置参数
 *
 * @author future0923
 */
public class ChatClientTest extends AbstractChatClientApplicationTest {

    @Autowired
    private ChatClient.Builder builder;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private VectorStore vectorStore;

    @Test
    public void chat() {
        ChatClient client = builder.build();
        Flux<String> flux = client
                .prompt()
                // 携带的默认系统角色信息
                .system("""
                        # 角色
                        你是一个旅游小助手，可以帮助取消机票预订。
                        ## 技能
                        今天的日期是 {current_date}。
                        ### 技能 1：快捷取消预订
                        1. 您必须始终从用户处获取以下信息：预订号、客户姓名。
                        ## 限制:
                        - 仅处理与旅游信息相关的内容，拒绝回答与AI选房无关的任何话题。
                        """)
                // 填充 PromptTemplate 参数
                .system(promptSystemSpec -> promptSystemSpec.param("current_date", LocalDate.now().toString()))
                .user("帮我取消预订，张三 101，并告诉我费用是多少")
                // 添加 Advisor
                .advisors(
                        // 内存聊天记忆
                        new PromptChatMemoryAdvisor(chatMemory),
                        // 日志打印
                        new SimpleLoggerAdvisor(),
                        // 向量信息
                        new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().build())
                )
                // 配置内存聊天记录的参数
                .advisors(advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, 1))
                .advisors(advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, AbstractChatMemoryAdvisor.DEFAULT_CHAT_MEMORY_RESPONSE_SIZE))
                // 向量查询需要对接向量模型才能更好搜索
                //.advisors(advisorSpec -> new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().query(message).build()))
                .options(DashScopeChatOptions.builder()
                        .withTopP(0.7)
                        .build())
                // 传递 Tool 只简单用一下，后面有详细的 Tools 使用.
                // BookingTools#cancelBooking()
                .tools("cancelBooking")
                .stream()
                .content();
        StepVerifier.create(flux)
                .thenConsumeWhile(res -> {
                    System.out.print(res);
                    return true;
                })
                .verifyComplete();
    }

}
```

## Advisors

Spring AI Advisors API提供了一种灵活而强大的方式来拦截、修改和增强Spring应用程序中的AI驱动交互。通过利用Advisors API，开发人员可以创建更复杂、可重用和可维护的AI组件。

主要优势包括封装重复生成性AI模式、转换发送到大型语言模型（LLM）和从大型语言模型（LLM）发送的数据，以及提供跨各种模型和用例的可移植性。

如：
- `MessageChatMemoryAdvisor(chatMemory)` 实现聊天记忆
- `QuestionAnswerAdvisor(vectorStore)` RAG向量
- 等等

### 概念

**核心组件**

API由 `CallAroundAdvisor` 和 `CallAroundAdvisorChain`（用于非流场景）以及 `StreamAroundAdvisor` 和 `StreamAroundAdvisorChain`（用于流场景）组成。它还包括表示未密封的提示请求的 `AdvisedRequest`、表示聊天完成响应的 `AdvisedResponse`。两者都持有一个advise-context，用于在顾问链中共享状态。

![wjfiqsvfjsadas.png](/images/wjfiqsvfjsadas.png)

`nextAroundCall()` 和 `nextAroundStream()` 是关键的顾问方法，通常执行诸如检查未密封的提示数据、自定义和扩充提示数据、调用顾问链中的下一个实体、可选地阻止请求、检查聊天完成响应以及抛出异常以指示处理错误等操作。

此外，`getOrder()` 方法确定链中的顾问顺序，而 `getName()`提供唯一的顾问名称。

由Spring AI框架创建的顾问链允许按getOrder()值顺序调用多个顾问。较低的值首先执行。自动添加的最后一个顾问将请求发送到LLM。

**AdvisorChain 与 LLM 交互**：

![hwfiqbwdhqwodas.png](/images/hwfiqbwdhqwodas.png)

1. Spring AI框架从用户的Prompt创建一个AdvisedRequest以及一个空的AdvisorContext对象。
2. 链中的每个顾问处理请求，可能会修改它。或者，它可以选择通过不调用来屏蔽请求以调起下一个实体。在后一种情况下，顾问负责填写响应。
3. 由框架提供的最终顾问将请求发送到Chat Model。
4. 然后聊天模型的响应通过顾问链传回并转换为AdvisedResponse。稍后包括共享的AdvisorContext实例。
5. 每个顾问都可以处理或修改响应。
6. 通过提取ChatCompletion，将最终的AdvisedResponse返回给客户端。

**顺序**：链中顾问的执行顺序由getOrder()方法决定，需要了解的要点：
- 具有较低顺序值的顾问首先执行。
- 顾问链作为堆栈运行：
    - 链中的第一个顾问是第一个处理请求的顾问。
    - 它也是最后处理响应的。
- 控制执行顺序：
    - 将顺序设置为接近Ordered.HIGHEST_PRECEDENCE以确保在链中首先执行顾问（首先用于请求处理，最后用于响应处理）。
    - 将顺序设置为接近Ordered.LOWEST_PRECEDENCE以确保顾问在链中最后执行（请求处理最后执行，响应处理首先执行）。
- 较高的值被解释为较低的优先级。
- 如果多个顾问具有相同的顺序值，则不能保证它们的执行顺序。

### 定义

基础类 **Advisor**

```java
public interface Ordered {

    int getOrder();

}

package org.springframework.ai.chat.client.advisor.api;

public interface Advisor extends Ordered {

	String getName();

}
```

同步的 **CallAroundAdvisor**

```java
package org.springframework.ai.chat.client.advisor.api;

public interface CallAroundAdvisor extends Advisor {

    /**
     * 围绕ChatModel#call(Prompt)方法的advice。
     */
	AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain);

}
```

流式的 **StreamAroundAdvisor**

```java
package org.springframework.ai.chat.client.advisor.api;

import reactor.core.publisher.Flux;

public interface StreamAroundAdvisor extends Advisor {

    /**
     * 围绕ChatModel#stream(Prompt)方法的advice。
     */
	Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain);

}
```

同步的Advisor责任链 **CallAroundAdvisorChain**

```java
package org.springframework.ai.chat.client.advisor.api;

public interface CallAroundAdvisorChain {

	/**
	 * 使用给定的请求调用 CallAroundAdvisorChain 中的下一个 Around Advisor。
	 */
	AdvisedResponse nextAroundCall(AdvisedRequest advisedRequest);

}
```

流式的Advisor责任链 **StreamAroundAdvisorChain**

```java
package org.springframework.ai.chat.client.advisor.api;

import reactor.core.publisher.Flux;

public interface StreamAroundAdvisorChain {

	/**
	 * 这个方法将调用委托给链中的下一个 StreamAroundAdvisor，并用于流式传输响应。
	 */
	Flux<AdvisedResponse> nextAroundStream(AdvisedRequest advisedRequest);

}
```

要创建顾问，请实现CallAroundAdvisor或StreamAroundAdvisor（或两者）。实现的关键方法是非流的nextAroundCall()或流顾问的nextAroundStream()。

### 内置的Advisor

**聊天记忆Advisor**
- `MessageChatMemoryAdvisor` 检索记忆并将其作为消息集合添加到提示中。这种方法维护对话历史的结构。请注意，并非所有AI模型都支持这种方法。
- `PromptChatMemoryAdvisor` 检索记忆并将其合并到提示的系统文本中。
- `VectorStoreChatMemoryAdvisor` 从VectorStore中检索记忆并将其添加到提示的系统文本中。此顾问可用于有效地从大型数据集中搜索和检索相关信息。
  
**问答顾问**
- `QuestionAnswerAdvisor` 该顾问使用向量存储来提供问答功能，实现RAG（检索-增强生成）模式。

**内容安全顾问**
- `SafeGuardAdvisor` 一个简单的顾问，旨在防止模型生成有害或不当内容。

可以查看这些源码来了解如何实现自己的Advisor。

**示例：**

实现一个简单的日志记录顾问 SimpleLoggerAdvisor，它在调用链中的下一个顾问之前记录AdvisedRequest，在调用之后记录AdvisedResponse。
```java

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

/**
 * 增加日志打印SimpleLoggerAdvisor
 *
 * @author future0923
 */
public class SimpleLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

  private static final Logger logger = LoggerFactory.getLogger(SimpleLoggerAdvisor.class);

  @NotNull
  @Override
  public AdvisedResponse aroundCall(@NotNull AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
    logger.info("before: {}", advisedRequest);
    // 调用下一个Advisor
    AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
    logger.info("AFTER: {}", advisedResponse);
    return advisedResponse;
  }

  @NotNull
  @Override
  public Flux<AdvisedResponse> aroundStream(@NotNull AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
    logger.info("before: {}", advisedRequest);
    // 调用下一个Advisor
    Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);
    // MessageAggregator是一个实用程序类，它将Flux响应聚合成一个AdvisedResponse。这对于日志记录或其他观察整个响应而不是流中单个项目的处理很有用。
    // 请注意，您不能更改MessageAggregator中的响应，因为它是只读操作。
    return new MessageAggregator().aggregateAdvisedResponse(advisedResponses, advisedResponse -> logger.debug("AFTER: {}", advisedResponse));
  }

  /**
   * Advisor名称
   */
  @NotNull
  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  /**
   * 执行顺序，越小越先执行
   */
  @Override
  public int getOrder() {
    return 0;
  }
}
```

重读顾问 ReReadingAdvisor。《[Re-Read改进大型语言模型中的推理](https://arxiv.org/pdf/2309.06275)》一文介绍了一种名为Re-Read（Re2）的技术，它提高了大型语言模型的推理能力，Re2技术需要像这样增强输入提示：

```text
{re_input_query}
Read the question again: {re_input_query}
```

实现：
```java
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * ReReadingAdvisor 的作用是——在发送用户问题给 AI 之前，自动把问题“重复一遍”，作为 prompt 的一部分，以期获得更认真或更准确的回答。
 * 目的：
 * <li>强调问题：通过重复问题，可以提示模型“认真阅读”或“更准确理解”。
 * <li>触发更好的回答：有些模型在 prompt 被强调、重复时，表现得更准确、稳定。
 * <li>用于链式调用的中间步骤：可能是更大 Advisor 链的一环，比如用来标记、记录或后续引用 {re2_input_query}。
 *
 * @author future0923
 */
public class ReReadingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    @NotNull
    @Override
    public AdvisedResponse aroundCall(@NotNull AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        return chain.nextAroundCall(before(advisedRequest));
    }

    @NotNull
    @Override
    public Flux<AdvisedResponse> aroundStream(@NotNull AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        return chain.nextAroundStream(before(advisedRequest));
    }

    @NotNull
    @Override
    public String getName() {
        return ReReadingAdvisor.class.getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 改写 Prompt 请求。在发送用户问题给 AI 之前，自动把问题“重复一遍”，作为 prompt 的一部分，以期获得更认真或更准确的回答。
     */
    private AdvisedRequest before(AdvisedRequest advisedRequest) {
        Map<String, Object> advisedUserParams = new HashMap<>(advisedRequest.userParams());
        // 将原始用户输入保存为参数 re_input_query。
        advisedUserParams.put("re_input_query", advisedRequest.userText());
        // 改写 Prompt 请求如下：
        // [原始问题]
        //Read the question again: [原始问题]
        return AdvisedRequest.from(advisedRequest)
                .userText("""
                        {re_input_query}
                        Read the question again: {re_input_query}
                        """)
                .userParams(advisedUserParams)
                .build();
    }

}
```

使用上面的两个Advisor

```java
public class ReReadingAdvisorTest extends AbstractChatClientApplicationTest {

    @Autowired
    private ChatClient.Builder builder;

    @Test
    public void reReadingAdvisor() {
        ChatClient chatClient = builder.build();
        Flux<String> flux = chatClient.prompt()
                .advisors(new ReReadingAdvisor())
                .advisors(new SimpleLoggerAdvisor())
                .user("绿茶的功效是什么？")
                .stream()
                .content();
        StepVerifier
                .create(flux)
                .thenConsumeWhile(res -> {
                    System.out.print(res);
                    return true;
                })
                .verifyComplete();
    }

}
```