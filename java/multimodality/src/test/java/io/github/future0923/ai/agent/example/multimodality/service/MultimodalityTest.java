package io.github.future0923.ai.agent.example.multimodality.service;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import io.github.future0923.ai.agent.example.multimodality.MultimodalityApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * @author future0923
 */
public class MultimodalityTest extends MultimodalityApplicationTest {

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private ChatClient.Builder builder;

    @Test
    public void chatModel() {
        // 指定图片资源
        ClassPathResource imageResource = new ClassPathResource("/findFood.png");
        // 构建多模态消息
        UserMessage userMessage = new UserMessage(
                "解释一下你在这张图片中看到了什么？",
                new Media(MimeTypeUtils.IMAGE_PNG, imageResource)
        );
        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .withModel("qwen-vl-max")
                // 开启多模态
                .withMultiModel(true)
                .build();
        String text = chatModel.call(new Prompt(userMessage, chatOptions)).getResult().getOutput().getText();
        System.out.println(text);
    }

    @Test
    public void chatClient() {
        ChatClient chatClient = builder.build();
        // 指定图片资源
        ClassPathResource imageResource = new ClassPathResource("/findFood.png");
        Flux<String> flux = chatClient.prompt()
                // 构建多模态消息
                .user(u -> u.text("解释一下你在这张图片中看到的什么？").media(MimeTypeUtils.IMAGE_PNG, imageResource))
                .options(DashScopeChatOptions.builder()
                        .withModel("qwen-vl-max")
                        // 开启多模态
                        .withMultiModel(true)
                        .build())
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
