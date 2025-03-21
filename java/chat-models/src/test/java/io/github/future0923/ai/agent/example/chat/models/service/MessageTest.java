package io.github.future0923.ai.agent.example.chat.models.service;

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
