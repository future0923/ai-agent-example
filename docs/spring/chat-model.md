# 聊天模型(Chat Model)

聊天模型应用编程接口为开发人员提供了将人工智能驱动的聊天完成功能集成到他们的应用程序中的能力。它利用预训练语言模型，如GPT（生成预训练转换器），以自然语言对用户输入生成类似人类的响应。

API通常通过向AI模型发送提示或部分对话来工作，然后AI模型根据其训练数据和对自然语言模式的理解生成对话的完成或继续。完成的响应然后返回给应用程序，应用程序可以将其呈现给用户或用于进一步处理。

演示[代码](https://github.com/future0923/ai-agent-example/tree/main/java/chat-models/src/test/java/io/github/future0923/ai/agent/example/chat/models/service)为 [SpringAiAlibaba](https://github.com/alibaba/spring-ai-alibaba) 对 SpringAi 的实现.

## ChatModel

### 定义

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

### 示例

```java
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import io.github.future0923.ai.agent.example.chat.models.AbstractChatModelsApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 聊天模型{@link ChatModel}示例
 *
 * @author future0923
 */
public class ChatModelTest extends AbstractChatModelsApplicationTest {

    /**
     * 聊天模型
     * SpringAiAlibaba自动注入了{@link DashScopeChatModel}
     */
    @Autowired
    private ChatModel chatModel;

    /**
     * 传入String类型
     */
    @Test
    public void chatCallString() {
        String string = "您好";
        String call = chatModel.call(string);
        System.out.println(call);
    }

    /**
     * 传入{@link Message}类型，可以传入多种消息数据，消息类型为{@link MessageType}，对应的实现如下
     * <ul>
     * <li>{@link UserMessage}用户消息</li>
     * <li>{@link SystemMessage}系统消息</li>
     * <li>{@link AssistantMessage}辅助消息</li>
     * <li>{@link ToolResponseMessage}工具详细</li>
     * </ul>
     */
    @Test
    public void chatCallMessage() {
        String message = "您好";
        String call = chatModel.call(new UserMessage(message));
        System.out.println(call);
    }

    /**
     * 传入{@link Prompt}类型，可以将多个消息类型格式化为Prompt对象，具体查看构造函数
     */
    @Test
    public void chatCallPrompt() {
        String prompt = "您好";
        ChatResponse response = chatModel.call(new Prompt(prompt));
        // ChatResponse [metadata={ id: d0bd345a-6c6a-9d6b-b3bb-2921e66600d2, usage: TokenUsage[outputTokens=7, inputTokens=9, totalTokens=16], rateLimit: org.springframework.ai.chat.metadata.EmptyRateLimit@2a6fd30a }, generations=[Generation[assistantMessage=AssistantMessage [messageType=ASSISTANT, toolCalls=[], textContent=您好！有什么可以帮助您的吗？, metadata={finishReason=STOP, id=d0bd345a-6c6a-9d6b-b3bb-2921e66600d2, role=ASSISTANT, messageType=ASSISTANT, reasoningContent=}], chatGenerationMetadata=DefaultChatGenerationMetadata[finishReason='STOP', filters=0, metadata=0]]]]
        System.out.println(response);
        System.out.println(response.getResult().getOutput().getText());;
    }
}
```

## StreamingChatModel

### 定义

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

### 示例

```java
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import io.github.future0923.ai.agent.example.chat.models.AbstractChatModelsApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * 流式聊天模型{@link StreamingChatModel}示例
 *
 * @author future0923
 */
public class StreamChatModelTest extends AbstractChatModelsApplicationTest {

    /**
     * 聊天模型，ChatModel 继承了 StreamingChatModel，所以可以直接使用
     * SpringAiAlibaba自动注入了{@link DashScopeChatModel}
     */
    @Autowired
    private ChatModel chatModel;

    /**
     * 传入String类型
     */
    @Test
    public void chatStreamString() {
        String string = "长春怎么样";
        Flux<String> flux = chatModel.stream(string);
        // 如果流式返回，则不能订阅，需要方法直接返回Flux<String>
        StepVerifier.create(flux)
                .thenConsumeWhile(res -> {
                    System.out.print(res);
                    return true;
                })
                .verifyComplete();
    }

    /**
     * 传入{@link Message}类型，可以传入多种消息数据，消息类型为{@link MessageType}，对应的实现如下
     * <ul>
     * <li>{@link UserMessage}用户消息</li>
     * <li>{@link SystemMessage}系统消息</li>
     * <li>{@link AssistantMessage}辅助消息</li>
     * <li>{@link ToolResponseMessage}工具详细</li>
     * </ul>
     */
    @Test
    public void chatStreamMessage() {
        String message = "长春怎么样";
        Flux<String> flux = chatModel.stream(new UserMessage(message));
        StepVerifier.create(flux)
                .thenConsumeWhile(res -> {
                    System.out.print(res);
                    return true;
                })
                .verifyComplete();
    }

    /**
     * 传入{@link Prompt}类型，可以将多个消息类型格式化为Prompt对象，具体查看构造函数
     */
    @Test
    public void chatStreamPrompt() {
        String prompt = "长春怎么样";
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(prompt));
        StepVerifier.create(flux)
                .thenConsumeWhile(chatResponse -> {
                    // ChatResponse [metadata={ id: 9c6ad8ea-51bc-9036-8dc1-d0009ba506e6, usage: TokenUsage[outputTokens=1, inputTokens=10, totalTokens=11], rateLimit: org.springframework.ai.chat.metadata.EmptyRateLimit@5f63a078 }, generations=[Generation[assistantMessage=AssistantMessage [messageType=ASSISTANT, toolCalls=[], textContent=长春, metadata={finishReason=NULL, id=9c6ad8ea-51bc-9036-8dc1-d0009ba506e6, role=ASSISTANT, messageType=ASSISTANT, reasoningContent=}], chatGenerationMetadata=DefaultChatGenerationMetadata[finishReason='NULL', filters=0, metadata=0]]]]
                    System.out.print(chatResponse.getResult().getOutput().getText());
                    return true;
                })
                .verifyComplete();
    }
}
```

## Prompt

[Prompt](../guide/concepts#prompt)本质上是一个 ModelRequest，它封装了 `Message` 列表和可选的模型请求选项。下面的清单显示了Prompt类的截断版本，不包括构造函数和其他实用方法：

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

## Message {#message}

### 定义

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

### 示例

```java
import io.github.future0923.ai.agent.example.chat.models.AbstractChatModelsApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

/**
 * 消息{@link Message}示例
 *
 * @author future0923
 */
public class MessageTest extends AbstractChatModelsApplicationTest {

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private ChatClient chatClient;

    /**
     * 使用{@link ChatModel}方式通过Message传递
     * 传多种类型{@link org.springframework.ai.chat.messages.MessageType}的消息数据，可以打到记忆功能
     */
    @Test
    public void chatModelMessage() {
        List<Message> messageList = Arrays.asList(
                new SystemMessage("你是一个旅游小助手，可以推荐旅游相关的信息"),
                new UserMessage("长春怎么样"),
                new AssistantMessage("长春是一个美丽的城市"),
                new UserMessage("推荐一下当地的美食吧")
        );
        Flux<String> flux = chatModel.stream(messageList.toArray(new Message[0]));
        StepVerifier.create(flux)
                .thenConsumeWhile(res -> {
                    System.out.print(res);
                    return true;
                })
                .verifyComplete();
    }

    /**
     * 使用{@link ChatClient}方式通过Message传递
     * 传多种类型{@link org.springframework.ai.chat.messages.MessageType}的消息数据，可以打到记忆功能
     */
    @Test
    public void chatClientMessage() {
        List<Message> messageList = Arrays.asList(
                new SystemMessage("你是一个旅游小助手，可以推荐旅游相关的信息"),
                new UserMessage("长春怎么样"),
                new AssistantMessage("长春是一个美丽的城市"),
                new UserMessage("推荐一下当地的美食吧")
        );
        Flux<String> flux = chatClient.prompt()
                .messages(messageList)
                .stream()
                .content();
        StepVerifier.create(flux)
                .thenConsumeWhile(res -> {
                    System.out.print(res);
                    return true;
                })
                .verifyComplete();
    }


    /**
     * 使用{@link ChatClient}方式通过Prompt传递
     * 传多种类型{@link org.springframework.ai.chat.messages.MessageType}的消息数据，可以打到记忆功能
     */
    @Test
    public void chatClientPrompt() {
        List<Message> messageList = Arrays.asList(
                new SystemMessage("你是一个旅游小助手，可以推荐旅游相关的信息"),
                new UserMessage("长春怎么样"),
                new AssistantMessage("长春是一个美丽的城市"),
                new UserMessage("推荐一下当地的美食吧")
        );
        Flux<String> flux = chatClient.prompt(new Prompt(messageList))
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

## ChatOptions {#chatoptions}

### 定义

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

### 示例

```java
import com.alibaba.cloud.ai.dashscope.api.DashScopeResponseFormat;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import io.github.future0923.ai.agent.example.chat.models.AbstractChatModelsApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * 聊天选项{@link ChatOptions}示例
 *
 * @author future0923
 */
public class ChatOptionsTest extends AbstractChatModelsApplicationTest {

    /**
     * 聊天模型
     * SpringAiAlibaba自动注入了{@link DashScopeChatModel}
     */
    @Autowired
    private ChatModel chatModel;

    /**
     * 请求大模型时通过{@link ChatOptions}传递参数。
     * 演示环境使用的 Spring Ai Alibaba 使用的是{@link DashScopeChatOptions}和{@link ChatOptions}参数有差异，所以这里都注释了。
     * 不同提供商实现的{@link ChatOptions}参数有差异有差异
     */
    @Test
    public void chatOptionsSpring() throws InterruptedException {
        String prompt = "长春怎么样";
        // 创建一个ChatOptions对象
        ChatOptions chatOptions = ChatOptions.builder()
                // 使用的模型
                //.model("qwen-max")
                // 频率惩罚，用于减少 AI 生成重复内容的可能性。正数的惩罚值，可以让模型生成更多元化的数据
                //.frequencyPenalty(0.2)
                // 限制 AI 生成的最大 token 数，控制响应的长度
                //.maxTokens(512)
                // 存在惩罚，提高 AI 生成新内容的倾向，减少已出现过的内容。正数值 AI 更倾向于生成未提及过的内容
                //.presencePenalty(0.3)
                // 停止序列，AI 遇到这些字符串时会停止生成。
                //.stopSequences(Collections.singletonList("</stop>"))
                // 控制随机性，影响输出的多样性。值越高，生成越随机；值越低，生成越确定
                // 如：0.2（更确定的回答，适合代码生成）
                // 如：1.0（更随机的回答，适合创意写作）
                //.temperature(0.9)
                // 限制候选 token 选择范围（即只从前 K 个最可能的 token 里采样）。
                // 值越低，AI 生成的内容越确定
                // 示例值: 50（从前 50 个最有可能的词中采样）
                //.topK(50)
                // 核采样（Top-p 采样），只从概率质量总和超过 P 的 token 中选择。
                // 较低值可减少随机性
                // 如：0.9（采样前 90% 的概率质量）
                // 如：0.3（更确定的回答）
                //.topP(0.9)
                .build();
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(prompt, chatOptions));
        StepVerifier.create(flux)
                .thenConsumeWhile(chatResponse -> {
                    // ChatResponse [metadata={ id: 9c6ad8ea-51bc-9036-8dc1-d0009ba506e6, usage: TokenUsage[outputTokens=1, inputTokens=10, totalTokens=11], rateLimit: org.springframework.ai.chat.metadata.EmptyRateLimit@5f63a078 }, generations=[Generation[assistantMessage=AssistantMessage [messageType=ASSISTANT, toolCalls=[], textContent=长春, metadata={finishReason=NULL, id=9c6ad8ea-51bc-9036-8dc1-d0009ba506e6, role=ASSISTANT, messageType=ASSISTANT, reasoningContent=}], chatGenerationMetadata=DefaultChatGenerationMetadata[finishReason='NULL', filters=0, metadata=0]]]]
                    System.out.print(chatResponse.getResult().getOutput().getText());
                    return true;
                })
                .verifyComplete();
    }

    /**
     * Spring Ai Alibaba {@link DashScopeChatOptions} 的参数
     * 不同提供商实现的{@link ChatOptions}参数有差异有差异
     */
    @Test
    public void chatOptionsAlibaba() {
        String prompt = "长春怎么样";
        // 创建一个ChatOptions对象
        ChatOptions chatOptions = DashScopeChatOptions.builder()
                // 使用的模型
                .withModel("qwen-max")
                // 限制 AI 生成的最大 token 数，控制响应的长度。
                .withMaxToken(1024)
                // 用于控制随机性和多样性的程度。具体来说，temperature值控制了生成文本时对每个候选词的概率分布进行平滑的程度。较高的temperature值会降低概率分布的峰值，使得更多的低概率词被选择，生成结果更加多样化；而较低的temperature值则会增强概率分布的峰值，使得高概率词更容易被选择，生成结果更加确定。 取值范围：[0, 2)，系统默认值0.85。不建议取值为0，无意义。
                .withTemperature(0.85)
                // 生成时，核采样方法的概率阈值。例如，取值为0.8时，仅保留累计概率之和大于等于0.8的概率分布中的token，作为随机采样的候选集。取值范围为（0,1.0)，取值越大，生成的随机性越高；取值越低，生成的随机性越低。默认值为0.8。注意，取值不要大于等于1
                .withTopP(0.8)
                // 生成时，采样候选集的大小。例如，取值为50时，仅将单次生成中得分最高的50个token组成随机采样的候选集。取值越大，生成的随机性越高；取值越小，生成的确定性越高。注意：如果top_k参数为空或者top_k的值大于100，表示不启用top_k策略，此时仅有top_p策略生效，默认是空。
                .withTopK(null)
                // stop参数用于实现内容生成过程的精确控制，在生成内容即将包含指定的字符串或token_ids时自动停止，生成内容不包含指定的内容。
                //例如，如果指定stop为"你好"，表示将要生成"你好"时停止；如果指定stop为[37763, 367]，表示将要生成"Observation"时停止。
                //stop参数支持以list方式传入字符串数组或者token_ids数组，支持使用多个stop的场景。
                // 说明 list模式下不支持字符串和token_ids混用，list模式下元素类型要相同。
                .withStop(null)
                // 格式化大模型的返回。可选值为 TEXT 、JSON
                .withResponseFormat(new DashScopeResponseFormat(DashScopeResponseFormat.Type.TEXT))
                // 模型内置了互联网搜索服务，该参数控制模型在生成文本时是否参考使用互联网搜索结果。取值如下：
                //true：启用互联网搜索，模型会将搜索结果作为文本生成过程中的参考信息，但模型会基于其内部逻辑"自行判断"是否使用互联网搜索结果。
                //false（默认）：关闭互联网搜索。
                .withEnableSearch(false)
                // 用于控制模型生成时的重复度。提高repetition_penalty时可以降低模型生成的重复度。1.0表示不做惩罚。默认为1.1。
                .withRepetitionPenalty(1.1)
                // 流式返回
                .withStream(false)
                // 生成时使用的随机数种子，用户控制模型生成内容的随机性。seed支持无符号64位整数。在使用seed时，模型将尽可能生成相同或相似的结果，但目前不保证每次生成的结果完全相同。
                .withSeed(null)
                // 控制在流式输出模式下是否开启增量输出，即后续输出内容是否包含已输出的内容。设置为True时，将开启增量输出模式，后面输出不会包含已经输出的内容，您需要自行拼接整体输出；设置为False则会包含已输出的内容。
                .withIncrementalOutput(true)
                .build();
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(prompt, chatOptions));
        StepVerifier.create(flux)
                .thenConsumeWhile(chatResponse -> {
                    // ChatResponse [metadata={ id: 9c6ad8ea-51bc-9036-8dc1-d0009ba506e6, usage: TokenUsage[outputTokens=1, inputTokens=10, totalTokens=11], rateLimit: org.springframework.ai.chat.metadata.EmptyRateLimit@5f63a078 }, generations=[Generation[assistantMessage=AssistantMessage [messageType=ASSISTANT, toolCalls=[], textContent=长春, metadata={finishReason=NULL, id=9c6ad8ea-51bc-9036-8dc1-d0009ba506e6, role=ASSISTANT, messageType=ASSISTANT, reasoningContent=}], chatGenerationMetadata=DefaultChatGenerationMetadata[finishReason='NULL', filters=0, metadata=0]]]]
                    System.out.print(chatResponse.getResult().getOutput().getText());
                    return true;
                })
                .verifyComplete();
    }
}
```

## ChatResponse

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

## Generation

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

## 可用实现

该图说明了统一接口ChatModel和StreamingChatModel，用于与来自不同提供商的各种AI聊天模型进行交互，允许在不同AI服务之间轻松接入和切换，同时为客户端应用程序保持一致的API。

![jwoqdwafiwdwda.png](/images/jwoqdwafiwdwda.png){v-zoom}{loading="lazy"}

## 聊天模型API

Spring AI Chat模型API建立在Spring AIGeneric Model API之上，提供Chat特定的抽象和实现。这允许在不同AI服务之间轻松接入和切换，同时为客户端应用程序保持一致的API。以下类图说明了Spring AI Chat模型API的主要类和接口。

![dwasdsadasdwqqds.png](/images/dwasdsadasdwqqds.png){v-zoom}{loading="lazy"}