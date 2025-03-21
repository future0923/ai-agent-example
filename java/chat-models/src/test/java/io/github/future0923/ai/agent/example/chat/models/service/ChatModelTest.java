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
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

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
    @RequestMapping("/message")
    public void chatCallMessage() {
        String message = "您好";
        String call = chatModel.call(new UserMessage(message));
        System.out.println(call);
    }

    /**
     * 传入{@link Prompt}类型，可以将多个消息类型格式化为Prompt对象，具体查看构造函数
     */
    @RequestMapping("/prompt")
    public void chatCallPrompt() {
        String prompt = "您好";
        ChatResponse response = chatModel.call(new Prompt(prompt));
        // ChatResponse [metadata={ id: d0bd345a-6c6a-9d6b-b3bb-2921e66600d2, usage: TokenUsage[outputTokens=7, inputTokens=9, totalTokens=16], rateLimit: org.springframework.ai.chat.metadata.EmptyRateLimit@2a6fd30a }, generations=[Generation[assistantMessage=AssistantMessage [messageType=ASSISTANT, toolCalls=[], textContent=您好！有什么可以帮助您的吗？, metadata={finishReason=STOP, id=d0bd345a-6c6a-9d6b-b3bb-2921e66600d2, role=ASSISTANT, messageType=ASSISTANT, reasoningContent=}], chatGenerationMetadata=DefaultChatGenerationMetadata[finishReason='STOP', filters=0, metadata=0]]]]
        System.out.println(response);
        System.out.println(response.getResult().getOutput().getText());;
    }
}
