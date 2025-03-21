package io.github.future0923.ai.agent.example.chat.models.service;

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
