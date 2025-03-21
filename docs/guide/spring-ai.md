
# Spring Ai

::: tip 注意
- 了解Spring AI之前，您需要已经了解[AI核心概念](concepts.md)。
- 此文章只介绍Spring AI的对AI开发的抽象定义，让开发者对SpringAI的核心功能有个大体的了解，详细功能及具体代码会在SpringAi代码示例中进行详细介绍。
:::
## 一、什么是 Spring AI？

[Spring AI](https://github.com/spring-projects/spring-ai)是AI工程的应用框架，其目标是将Spring生态系统设计原则（如可移植性和模块化设计）应用于AI领域，并将POJO作为应用程序的构建块推广到AI领域。

该项目从著名的Python项目中汲取灵感，如 [LangChain](https://github.com/langchain-ai)和[LlamaIndex](https://github.com/run-llama/llama_index)，但Spring AI不是这些项目的直接复制。该项目的创建是基于这样一种信念，即下一波生成性AI应用程序不仅面向Python开发人员，而且将在许多编程语言中无处不在。

langchain不只有python也有很多其他语言的项目
- Java：[LangChain4J](https://github.com/langchain4j/langchain4j)
- Js：[LangChainJs](https://github.com/langchain-ai/langchainjs)
- Go：[LangChainGo](https://github.com/tmc/langchaingo)
- 等等

Spring AI项目旨在简化应用程序的开发，这些应用程序包含人工智能功能，而没有不必要的复杂性。Spring AI提供了作为开发AI应用程序基础的抽象，这些抽象具有多种实现，能够以最少的代码更改轻松地进行组件交换。

![dsadasdwqfvdsds.png](/images/dsadasdwqfvdsds.png){v-zoom}{loading="lazy"}

## 二、Spring 对 AI 的抽象

### 2.1 聊天模型(Chat Models)

#### 2.1.1 ChatModel

ChatModel对聊天模型进行定义，可以通过call方法调用大模型并接受响应信息。

```java
public interface ChatModel extends Model<Prompt, ChatResponse>, StreamingChatModel {

    default String call(String message) {
  
    }

    default String call(Message... messages) {
       
    }

    @Override
    ChatResponse call(Prompt prompt);

    default ChatOptions getDefaultOptions() {
        return ChatOptions.builder().build();
    }
}
```

通过请求 `call()` 方法可以直接将信息发送给大模型，可以传入 `String` 或 `Message` 或 `Prompt` 类型的参数。接收到大模型的响应，可以返回 `String` 或 `ChatResponse` 类型。

**call 方式调用会阻塞等待大模型完全返回完整数据**，如果大模型返回的数据量比较大，可能会导致阻塞时间过长，甚至导致请求超时。

如果需要流式返回大模型的数据，可以通过 `StreamingChatModel` 方法调用大模型。

#### 2.1.2 StreamingChatModel

ChatModel对流式聊天模型进行定义，可以通过stream方法调用大模型并接受响应信息。

```java
public interface StreamingChatModel extends StreamingModel<Prompt, ChatResponse> {

	default Flux<String> stream(String message) {
		
	}

	default Flux<String> stream(Message... messages) {
      
	}

	@Override
	Flux<ChatResponse> stream(Prompt prompt);

}
```

通过请求 `stream()` 方法可以直接将信息发送给大模型，可以传入 `String` 或 `Message` 或 `Prompt` 类型的参数。接收到大模型的响应，可以返回 `Flux<String>` 或 `Flux<ChatResponse>` 类型。

Flux 是 [reactor](https://github.com/reactor/reactor-core) 的一个响应式流，不熟悉可以访问[官网](https://projectreactor.io/).

#### 2.1.3 Prompt

[Prompt](concepts#prompt)本质上是一个 ModelRequest，它封装了 `Message` 列表和可选的模型请求选项。下面的清单显示了Prompt类的截断版本，不包括构造函数和其他实用方法：

```java
public class Prompt implements ModelRequest<List<Message>> {

	private final List<Message> messages;

	@Nullable
	private ChatOptions chatOptions;

	public Prompt(String contents) {
		this(new UserMessage(contents));
	}

	public Prompt(Message message) {
		this(Collections.singletonList(message));
	}

	public Prompt(List<Message> messages) {
		this(messages, null);
	}

	public Prompt(Message... messages) {
		this(Arrays.asList(messages), null);
	}

	public Prompt(String contents, ChatOptions chatOptions) {
		this(new UserMessage(contents), chatOptions);
	}

	public Prompt(Message message, ChatOptions chatOptions) {
		this(Collections.singletonList(message), chatOptions);
	}

	public Prompt(List<Message> messages, ChatOptions chatOptions) {
		this.messages = messages;
		this.chatOptions = chatOptions;
	}
}
```

通过各种构造函数可以创造各种类型的 Prompt，还可以通过 ChatOptions 指定聊参数信息。

#### 2.1.4 Message

Message 接口封装了一个提示文本、一组元数据属性以及一个称为 MessageType 的分类。

```java
public interface Content {

    String getText();

    Map<String, Object> getMetadata();

}

public interface Message extends Content {

	MessageType getMessageType();

}
```

Message 接口的各种实现对应 AI 模型可以处理的不同类别的消息。模型根据对话角色区分消息类别。该Message接口具有与AI模型可以处理的消息类别相对应的各种实现：

![huwdqwihdsfwsda.png](/images/huwdqwihdsfwsda.png){v-zoom}{loading="lazy"}

对于实例，OpenAI识别不同会话角色的消息类别

```java
public enum MessageType {

    /**
     * 用户角色（User Role）：代表用户的输入 - 他们向 AI 提出的问题、命令或陈述。这个角色至关重要，因为它构成了 AI 响应的基础。
     */
    USER("user"),

    /**
     * 助手角色（Assistant Role）：AI 对用户输入的响应。这不仅仅是一个答案或反应，它对于保持对话的流畅性至关重要。通过跟踪 AI 之前的响应（其“助手角色”消息），系统可确保连贯且上下文相关的交互。助手消息也可能包含功能工具调用请求信息。它就像 AI 中的一个特殊功能，在需要执行特定功能（例如计算、获取数据或不仅仅是说话）时使用。
     */
	  ASSISTANT("assistant"),

    /**
     * 系统角色（System Role）：指导 AI 的行为和响应方式，设置 AI 如何解释和回复输入的参数或规则。这类似于在发起对话之前向 AI 提供说明。
     */
    SYSTEM("system"),

    /**
     * 工具/功能角色（Tool/Function Role）：工具/功能角色专注于响应工具调用助手消息返回附加信息。
     */
	  TOOL("tool");

}
```

#### 2.1.5 ChatOptions

模型可以传递给大模型的一些参数，这些参数可以控制大模型的行为。这是一个强大的特征，允许开发人员在启动应用程序时使用特定于模型的选项，然后在运行时使用Prompt请求覆盖它们。

请仔细查看每个配置的作用，这对于ai生成回答十分有用。

```java
public interface ChatOptions extends ModelOptions {

    /**
     * 使用的模型
     */
    String getModel();

    /**
     * 频率惩罚，用于减少 AI 生成重复内容的可能性。
     * 正数的惩罚值，可以让模型生成更多元化的数据
     */
    Float getFrequencyPenalty();

    /**
     * 限制 AI 生成的最大 token 数，控制响应的长度。
     */
    Integer getMaxTokens();

    /**
     * 存在惩罚，提高 AI 生成新内容的倾向，减少已出现过的内容。
     * 正数值 AI 更倾向于生成未提及过的内容
     */
    Float getPresencePenalty();

    /**
     * 停止序列，AI 遇到这些字符串时会停止生成。
     */
    List<String> getStopSequences();

    /**
     * 控制随机性，影响输出的多样性。
     * 值越高，生成越随机；值越低，生成越确定
     * 如：0.2（更确定的回答，适合代码生成）
     * 如：1.0（更随机的回答，适合创意写作）
     */
    Float getTemperature();

    /**
     * 限制候选 token 选择范围（即只从前 K 个最可能的 token 里采样）。
     * 值越低，AI 生成的内容越确定
     * 示例值: 50（从前 50 个最有可能的词中采样）
     */
    Integer getTopK();

    /**
     * 核采样（Top-p 采样），只从概率质量总和超过 P 的 token 中选择。
     * 较低值可减少随机性
     * 如：0.9（采样前 90% 的概率质量）
     * 如：0.3（更确定的回答）
     */
    Float getTopP();
}
```

Spring AI提供了一个用于配置和使用聊天模型的复杂系统。它允许在启动时设置默认配置，同时还提供了在每个请求的基础上覆盖这些设置的灵活性。这种方法使开发人员能够轻松地使用不同的AI模型并根据需要调整参数，所有这些都在Spring AI框架提供的一致界面中进行。

以下流程图说明了Spring AI如何处理聊天模型的配置和执行，结合了启动和运行时选项：

![jwofnqfkasmfekfs.png](/images/jwofnqfkasmfekfs.png){v-zoom}{loading="lazy"}

#### 2.1.6 ChatResponse

保存AI模型的输出，每个Generation实例包含由单个提示产生的潜在多个输出之一。还携带了AI模型响应的ChatResponseMetadata元信息。

```java
public class ChatResponse implements ModelResponse<Generation> {

    /**
     * 模型响应的元数据信息
     */
    private final ChatResponseMetadata chatResponseMetadata;

    /**
     * 存储 AI 生成的多个候选回复。
     */
    private final List<Generation> generations;
    
    // other methods omitted
}
```

#### 2.1.7 Generation

Generation 继承 ModelResult 表示模型输出（assistant 消息）和相关元信息 （metadata 信息）：

```java
public class Generation implements ModelResult<AssistantMessage> {

    /**
     * 辅助信息
     */
	  private final AssistantMessage assistantMessage;

    /**
     * 元数据信息
     */
	  private ChatGenerationMetadata chatGenerationMetadata;

    // other methods omitted
}
```

#### 2.1.8 可用实现

该图说明了统一接口ChatModel和StreamingChatModel，用于与来自不同提供商的各种AI聊天模型进行交互，允许在不同AI服务之间轻松接入和切换，同时为客户端应用程序保持一致的API。

![jwoqdwafiwdwda.png](/images/jwoqdwafiwdwda.png){v-zoom}{loading="lazy"}

#### 2.1.9 聊天模型API

Spring AI Chat模型API建立在Spring AIGeneric Model API之上，提供Chat特定的抽象和实现。这允许在不同AI服务之间轻松接入和切换，同时为客户端应用程序保持一致的API。以下类图说明了Spring AI Chat模型API的主要类和接口。

![dwasdsadasdwqqds.png](/images/dwasdsadasdwqqds.png){v-zoom}{loading="lazy"}