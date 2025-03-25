package io.github.future0923.ai.agent.example.structured.output.converter;

import io.github.future0923.ai.agent.example.structured.output.StructuredOutputApplicationTest;
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

/**
 * @author future0923
 */
class BeanOutputConverterTest extends StructuredOutputApplicationTest {

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