# 多模态(Multimodality)

大模型的数据可能不单单是只有文本，我们可以传入多种类型的输入让大模型更改的生成响应。

多模态是指模型同时理解和处理来自各种来源的信息的能力，包括文本、图像、音频和其他数据格式。

## Spring AI 多模态

Spring AI消息API提供了所有必要的抽象来支持多模态LLM。

![img.png](/images/hiwoqfjiajdoiasjdioajsda.png){v-zoom}{loading="lazy"}

拿 `UserMessage` 来说，`content` 字段主要用于文本输入，而可选的 `media` 字段允许添加一个或多个不同模式的附加内容，如图像、音频和视频。`MimeType` 指定模态类型。根据使用的LLM，Media数据字段可以是作为Resource对象的原始媒体内容，也可以是内容的URI。

::: tip

媒体字段目前仅适用于用户输入消息（例如，UserMessage）。它对系统消息没有意义。包含LLM响应的AssistantMessage仅提供文本内容。要生成非文本媒体输出，您应该使用专用的单模态模型之一。

:::

## 示例

我们将文字和下面的图片(findFood.png)作为输入，要求LLM解释图片中看到的什么。

![findFood.png](/images/sdadasdsdasdsafwqdw.png)

使用 [ChatModel](chat-model) 和 [ChatClient](chat-client) 来调用。

```java
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
```

输出

```text
这张图片展示了一个简单的图标，图标中包含一个盘子、一把叉子和一把刀。盘子是黄色的，叉子和刀交叉放置在盘子上。整个图标被一个圆形的边框包围，边框的颜色是浅棕色。这个图标通常用于表示餐饮或食物相关的主题，可能用于餐厅、食谱应用或食品服务行业的标志。
```

- [源码](https://github.com/future0923/ai-agent-example/tree/main/java/multimodality)