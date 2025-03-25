# 结构化输出

演示[代码](https://github.com/future0923/ai-agent-example/tree/main/java/structured-output) .

## 概念

LLM 生成结构化输出的能力对于依赖可靠解析输出值的下游应用程序非常重要。开发人员希望快速将 AI 模型的结果转换为可以传递给其他应用程序函数和方法的数据类型，例如 JSON、XML 或 Java 类。Spring AI 结构化输出转换器有助于将 LLM 输出转换为结构化格式。

![jwfoinadjqwpkdasdas.png](/images/jwfoinadjqwpkdasdas.png){v-zoom}{loading="lazy"}

在 LLM 调用之前，转换器会将期望的输出格式（output format instruction）附加到 prompt 中，为模型提供生成所需输出结构的明确指导，这些指令充当蓝图，塑造模型的响应以符合指定的格式。

## API

下图显示了使用结构化输出API时的数据流。

![jofwnjecssdjwdsa.png](/images/jofwnjecssdjwdsa.png){v-zoom}{loading="lazy"}

目前，Spring AI提供AbstractConversionServiceOutputConverter、AbstractMessageOutputConverter、BeanOutputConverter、MapOutputConverter和ListOutputConverter实现：

![jdoiqwfhiqajdsadada.png](/images/jdoiqwfhiqajdsadada.png){v-zoom}{loading="lazy"}

- `AbstractConversionServiceOutputConverter`：提供用于将LLM输出转换为所需格式的预配置GenericConversionService。没有提供默认的FormatProvider实现。
- `AbstractMessageOutputConverter`：提供预配置的MessageConverter，用于将LLM输出转换为所需的格式。没有提供默认的FormatProvider实现。
- `BeanOutputConverter`：使用指定的Java类（例如Bean）或 `ParameterizedTypeReference` 进行配置，此转换器采用FormatProvider实现，指导AI模型生成符合从指定Java类派生的DRAFT_2020_12、JSON Schema的JSON响应。随后，它利用ObjectMapper将JSON输出反序列化为目标类的Java对象实例。
- `MapOutputConverter`：通过AbstractMessageOutputConverter实现扩展FormatProvider的功能，指导AI模型生成符合RFC8259的JSON响应。此外，它还包含一个转换器实现，利用提供的MessageConverter将JSON有效负载转换为java.util.Map<String, Object>实例。
- `ListOutputConverter`：扩展AbstractConversionServiceOutputConverter并包含为逗号分隔列表输出量身定制的FormatProvider实现。转换器实现使用提供的ConversionService将模型文本输出转换为java.util.List。

## 使用

### BeanOutputConverter

使用 `@JsonPropertyOrder` 可以对参数排序

```java
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({"actor", "movies"})
public record ActorsFilms(String actor, List<String> movies) {

}
```

chatClient 方式使用 BeanOutputConverter 将 LLM 输出转换为 java bean 实例。

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;

import java.util.List;
import java.util.Map;

class BeanOutputConverterTest {

    @Autowired
    private ChatClient.Builder builder;

    @Autowired
    private ChatModel chatModel;

    /**
     * chatClient 方式使用 BeanOutputConverter
     */
    @Test
    public void chatClient() {
        ChatClient chatClient = builder.build();
        ActorsFilms actorsFilms = chatClient.prompt()
                .user(u -> u.text("说出{actor}出演的5部电影.")
                        .param("actor", "成龙"))
                .call()
                // 使用 BeanOutputConverter 进行转换
                .entity(ActorsFilms.class);
        System.out.println(actorsFilms);
    }
}
```

ChatModel 方式使用 BeanOutputConverter 将 LLM 输出转换为 java bean 实例。

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;

import java.util.List;
import java.util.Map;

class BeanOutputConverterTest {

    @Autowired
    private ChatClient.Builder builder;

    @Autowired
    private ChatModel chatModel;

    /**
     * chatModel 方式使用 BeanOutputConverter
     */
    @Test
    public void chatModel() {
        // 创建 BeanOutputConverter 格式化 ActorsFilms
        BeanOutputConverter<ActorsFilms> beanOutputConverter = new BeanOutputConverter<>(ActorsFilms.class);
        // 生成 template 的 Format 信息
        String format = beanOutputConverter.getFormat();
        PromptTemplate promptTemplate = new PromptTemplate("""
                说出{actor}出演的5部电影。
                {format}
                """,
                Map.of("actor", "成龙", "format", format));
        // 获取返回的内容
        String text = chatModel.call(promptTemplate.create())
                .getResult()
                .getOutput()
                .getText();
        // 使用 BeanOutputConverter 将结果格式化 ActorsFilms
        ActorsFilms actorsFilms = beanOutputConverter.convert(text);
        System.out.println(actorsFilms);
    }
}
```

### ParameterizedTypeReference

ChatClient 使用 ParameterizedTypeReference 将 LLM 输出转换为 **复杂** 的类型。

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;

import java.util.List;
import java.util.Map;

class BeanOutputConverterTest {

    @Autowired
    private ChatClient.Builder builder;

    @Autowired
    private ChatModel chatModel;

    /**
     * chatClient 方式使用 ParameterizedTypeReference 处理复杂类型
     */
    @Test
    public void chatClientParameterizedTypeReference() {
        ChatClient chatClient = builder.build();
        List<ActorsFilms> actorsFilms = chatClient.prompt()
                .user(u -> u.text("说出{actor}出演的5部电影.")
                        .param("actor", "成龙"))
                .call()
                // 使用 ParameterizedTypeReference 进行复杂类型转换
                .entity(new ParameterizedTypeReference<List<ActorsFilms>>() {
                });
        actorsFilms.forEach(System.out::println);
    }
}
```

ChatModel 使用 ParameterizedTypeReference 将 LLM 输出转换为 **复杂** 的类型。

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;

import java.util.List;
import java.util.Map;

class BeanOutputConverterTest {

    @Autowired
    private ChatClient.Builder builder;

    @Autowired
    private ChatModel chatModel;

    /**
     * chatModel 方式使用 ParameterizedTypeReference 处理复杂类型
     */
    @Test
    public void chatModeParameterizedTypeReference() {
        // 创建 BeanOutputConverter 格式化 ActorsFilms
        BeanOutputConverter<List<ActorsFilms>> beanOutputConverter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<ActorsFilms>>() {
        });
        // 生成 template 的 Format 信息
        String format = beanOutputConverter.getFormat();
        PromptTemplate promptTemplate = new PromptTemplate("""
                说出{actor}出演的5部电影。
                {format}
                """,
                Map.of("actor", "成龙", "format", format));
        // 获取返回的内容
        String text = chatModel.call(promptTemplate.create())
                .getResult()
                .getOutput()
                .getText();
        // 使用 BeanOutputConverter 将结果格式化 ActorsFilms
        List<ActorsFilms> actorsFilms = beanOutputConverter.convert(text);
        actorsFilms.forEach(System.out::println);
    }
}
```

### MapOutPutConverter

ChatClient 使用 MapOutPutConverter 将 LLM 输出转换为 **Map** 的类型。

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;

import java.util.List;
import java.util.Map;

class BeanOutputConverterTest {

    @Autowired
    private ChatClient.Builder builder;

    @Autowired
    private ChatModel chatModel;

    /**
     * chatClient 方式使用 MapOutPutConverter
     */
    @Test
    public void chatClientMapOutPutConverter() {
        Map<String, Object> result = builder.build().prompt()
                .user(u -> u.text("给我一个{subject}的列表")
                        .param("subject", "在number键名下有一个从1到9的数字数组。"))
                .call()
                .entity(new ParameterizedTypeReference<Map<String, Object>>() {
                });
        result.forEach((k, v) -> System.out.println(k + ":" + v));
    }
}
```

ChatModel 使用 MapOutPutConverter 将 LLM 输出转换为 **Map** 的类型。

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;

import java.util.List;
import java.util.Map;

class BeanOutputConverterTest {

    @Autowired
    private ChatClient.Builder builder;

    @Autowired
    private ChatModel chatModel;

    /**
     * chatModel 方式使用 MapOutPutConverter
     */
    @Test
    public void chatModelMapOutPutConverter() {
        MapOutputConverter mapOutputConverter = new MapOutputConverter();
        String format = mapOutputConverter.getFormat();
        PromptTemplate promptTemplate = new PromptTemplate("""
                给我一个{subject}的列表。
                {format}
                """,
                Map.of("subject", "在number键名下有一个从1到9的数字数组", "format", format));

        // 获取返回的内容
        String text = chatModel.call(promptTemplate.create())
                .getResult()
                .getOutput()
                .getText();
        // 使用 BeanOutputConverter 将结果格式化 ActorsFilms
        Map<String, Object> result = mapOutputConverter.convert(text);
        result.forEach((k, v) -> System.out.println(k + ":" + v));
    }
}
```

### ListOutPutConverter

ChatClient 使用 ListOutPutConverter 将 LLM 输出转换为 **List** 的类型。

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;

import java.util.List;
import java.util.Map;

class BeanOutputConverterTest {

    @Autowired
    private ChatClient.Builder builder;

    @Autowired
    private ChatModel chatModel;

    /**
     * chatClient 方式使用 ListOutPutConverter
     */
    @Test
    public void chatClientListOutPutConverter() {
        List<String> result = builder.build().prompt()
                .user(u -> u.text("给我一个{subject}的列表")
                        .param("subject", "从1到9的数字"))
                .call()
                .entity(new ListOutputConverter(new DefaultConversionService()));
        result.forEach(System.out::println);
    }
}
```

ChatModel 使用 ListOutPutConverter 将 LLM 输出转换为 **List** 的类型。

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;

import java.util.List;
import java.util.Map;

class BeanOutputConverterTest {

    @Autowired
    private ChatClient.Builder builder;

    @Autowired
    private ChatModel chatModel;

    /**
     * chatModel 方式使用 ListOutPutConverter
     */
    @Test
    public void chatModelListOutPutConverter() {
        ListOutputConverter listOutputConverter = new ListOutputConverter(new DefaultConversionService());
        String format = listOutputConverter.getFormat();
        PromptTemplate promptTemplate = new PromptTemplate("""
                给我一个{subject}的列表。
                {format}
                """,
                Map.of("subject", "从1到9的数字", "format", format));

        // 获取返回的内容
        String text = chatModel.call(promptTemplate.create())
                .getResult()
                .getOutput()
                .getText();
        // 使用 ListOutputConverter 将结果格式化 ActorsFilms
        List<String> result = listOutputConverter.convert(text);
        result.forEach(System.out::println);
    }
}
```