# 工具(Tool)/功能调用(Function Calling)

演示[代码](https://github.com/future0923/ai-agent-example/tree/main/java/function-calling) .

## 简介

**功能调用(Function Calling)已经过时，使用方式改为工具调用(Tool)**。

工具调用（也被称为函数调用）是AI应用程序中的一种常见模式，允许模型与一组API或工具交互，从而增强其功能大型语言模型（LLM）在必要时调用一个或多个可用的工具，这些工具通常由开发者定义。。

工具主要用于：

- 信息检索。此类工具可用于从外部来源检索信息，例如数据库、Web服务、文件系统或Web搜索引擎。目标是增强模型的知识，使其能够回答否则无法回答的问题。因此，它们可用于检索增强生成（RAG）场景。例如，工具可用于检索给定位置的当前天气、检索最新新闻文章或查询数据库以获取特定记录。
- 触发动作。此类工具可用于在软件系统中触发动作，例如发送电子邮件、在数据库中创建新记录、提交表单或触发工作流。目标是自动化原本需要人工干预或显式编程的任务。例如，工具可用于为与聊天机器人交互的客户预订航班、在网页上填写表单或在代码生成场景中实现基于自动化测试（TDD）的Java类。

## 入门

### 信息检索

人工智能模型无法访问实时信息。任何假设意识到当前日期或天气预报等信息的问题都无法由模型回答。但是，我们可以提供一个可以检索这些信息的工具，并让模型在需要访问实时信息时调用这个工具。

大模型是不知道当前时间是什么的，我们在一个DateTimeTools类中实现一个工具来获取用户时区中的当前日期和时间。该工具将不带参数。Spring Framework的LocaleContextHolder可以提供用户时区。该工具将被定义为用@Tool注释的方法。
为了帮助模型理解是否以及何时调用该工具，我们将详细描述这些工具的作用，当大模型有需要获取当前时间的情况时就会调用这个工具

```java
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具
 */
public class DateTimeTools {

    /**
     * 获取当前时间工具
     */
    @Tool(description = "获取用户所在时区的当前日期和时间。")
    public String getCurrentTime() {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }
}
```

使用工具

```java
public class DateTimeToolsTest {

    @Autowired
    private ChatClient.Builder builder;

    /**
     * 我们要获取明天多少号就要知道今天的时间，大模型就会调用DateTimeTools的getCurrentTime方法获取当前时间
     */
    @Test
    public void getCurrentTime() {
        ChatClient chatClient = builder.build();
        String content = chatClient.prompt()
                .user("明天是多少号？")
                .tools(new DateTimeTools())
                .call()
                .content();
        System.out.println(content);
    }
}
```

### 触发动作

人工智能模型可用于生成实现某些目标的计划。例如，模型可以生成预订丹麦之旅的计划。然而，模型没有能力执行计划。这就是工具的用武之地：它们可以用来执行模型生成的计划。

在这个例子中，我们将定义第二个工具来设置特定时间的警报。目标是从现在开始设置10分钟的警报，所以我们需要向模型提供这两种工具来完成这项任务。

```java

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具
 *
 * @author future0923
 */
public class DateTimeTools {

    /**
     * 设置日期提醒工具
     */
    @Tool(description = "按照ISO-8601格式设置给定时间的用户提醒。")
    public void setAlarm(@ToolParam(required = true, description = "以ISO-8601格式的时间") String time) {
        LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        System.out.println("Alarm set for " + alarmTime);
    }
}
```

使用工具

```java
public class DateTimeToolsTest {

    @Autowired
    private ChatClient.Builder builder;
    
    /**
     * 当我们要设置提醒是，大模型就会调用DateTimeTools的setAlarm方法设置提醒
     */
    @Test
    public void setAlarm() {
        ChatClient chatClient = builder.build();
        String content = chatClient.prompt()
                .user("十分钟后提醒我")
                .tools(new DateTimeTools())
                .call()
                .content();
        System.out.println(content);
    }

}
```

## 流程概览

Spring AI通过一组灵活的抽象支持工具调用，这些抽象允许您以一致的方式定义、解析和执行工具。Spring AI中工具调用的主要概念和组件如下。

 ![hwidhwiuahduiwhfnasd.png](/images/hwidhwiuahduiwhfnasd.png){v-zoom}{loading="lazy"}

1. 当我们想让一个工具对模型可用时，我们在聊天请求中包含它的定义，每个工具定义包括一个名称、一个描述和输入参数的schema。
2. 当模型决定调用一个工具时，它会发送一个响应，其中包含工具名称和按照定义的schema建模的输入参数。
3. 应用程序负责使用工具名称来识别和执行具有提供的输入参数的工具。
4. 工具调用的结果由应用程序处理。
5. 应用程序将工具调用结果发送回模型。
6. 模型使用工具调用结果作为附加上下文生成最终响应。

## 具体使用形式

### 方法工具(Method Tool)

Spring AI 支持通过下面两种方法定义方法工具
- 声明方式：使用 `@Tool` 注解
- 编程方式：通过 `MethodToolCallback` 构建

当前不支持以下类型作为用作工具的方法的参数或返回类型：
- Optional
- 异步类型（例如CompletableFuture、Future）
- Reactive类型（例如Flow、Mono、Flux）
- Functional类型（例如Function、Supplier、Consumer）。

#### @Tool 注解

```java
package org.springframework.ai.tool.annotation;

import org.springframework.ai.tool.execution.DefaultToolCallResultConverter;
import org.springframework.ai.tool.execution.ToolCallResultConverter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Tool {

	/**
	 * 工具的名称。如果未提供，将使用方法名称。AI模型在调用工具时使用此名称来识别工具。
     * 因此，不允许在同一类中有两个同名的工具。对于特定的聊天请求，该名称必须在模型可用的所有工具中是唯一的。
	 */
	String name() default "";

	/**
	 * 工具的描述，模型可以使用它来理解何时以及如何调用工具。如果没有提供，方法名称将用作工具描述。
     * 强烈建议提供详细的描述，因为这对模型理解工具的目的以及如何使用它至关重要。未能提供良好的描述可能会导致模型在应该使用工具的时候没有使用工具，或者使用不正确。
	 */
	String description() default "";

	/**
	 * 工具结果是应该直接返回给调用客户端还是传回模型。
     * ture：工具调用的结果将直接返回给调用客户端。
     * false：工具调用的结果返回给模型
	 */
	boolean returnDirect() default false;

	/**
	 * 用于将工具调用的结果转换为String object以打回AI模型的ToolCallResultConverter实现。
     * 下面有
	 */
	Class<? extends ToolCallResultConverter> resultConverter() default DefaultToolCallResultConverter.class;

}
```

| 属性              | 描述                                                                                                                                 |
|-----------------|------------------------------------------------------------------------------------------------------------------------------------|
| name            | 工具的名称。如果未提供，将使用方法名称。AI模型在调用工具时使用此名称来识别工具。 <br/> 因此，不允许在同一类中有两个同名的工具。对于特定的聊天请求，该名称必须在模型可用的所有工具中是唯一的。                                |
| description     | 工具的描述，模型可以使用它来理解何时以及如何调用工具。如果没有提供，方法名称将用作工具描述。<br/> 强烈建议提供详细的描述，因为这对模型理解工具的目的以及如何使用它至关重要。未能提供良好的描述可能会导致模型在应该使用工具的时候没有使用工具，或者使用不正确。 |
| returnDirect    | 工具结果是应该直接返回给调用客户端还是传回模型。<br/> ture：工具调用的结果将直接返回给调用客户端。<br/> false：工具调用的结果返回给模型 。[详细介绍](#return-direct)                             |
| resultConverter | 用于将工具调用的结果转换为String object以打回AI模型的ToolCallResultConverter实现。[详细介绍](#result-converter) 。                                            |


方法可以是静态的，也可以是实例的，它可以具有任何可见性（公共的、受保护的、包私有的或私有的）。包含方法的类可以是顶级类，也可以是嵌套类，它也可以具有任何可见性。

使用 `@ToolParam` 注解提供参数的描述

| 属性          | 描述                              |
|-------------|---------------------------------|
| required    | 参数是必需的还是可选的。默认情况下，所有参数都被认为是必需的。 |
| description | 参数的描述，模型可以使用它来更好地理解如何使用它。       |

如上面工具定义的示例:

```java
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具
 */
public class DateTimeTools {

    /**
     * 获取当前时间工具
     */
    @Tool(description = "获取用户所在时区的当前日期和时间。")
    public String getCurrentTime() {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

    /**
     * 设置日期提醒工具
     */
    @Tool(description = "按照ISO-8601格式设置给定时间的用户提醒。")
    public void setAlarm(@ToolParam(required = true, description = "以ISO-8601格式的时间") String time) {
        LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        System.out.println("Alarm set for " + alarmTime);
    }
}
```

##### 添加到 ChatClient

将工具添加到 `ChatClient` 中使用 `tools()` 方法设置。可以是 new 对象，也可以是 Bean 对象。底层用过 `ToolCallbacks.from(toolObjects)` 解析。

```java
public void getCurrentTime() {
    ChatClient chatClient = builder.build();
    String content = chatClient.prompt()
            .user("明天是多少号？")
            // 可以 new ，也可以是 Bean
            .tools(new DateTimeTools())
            .call()
            .content();
    System.out.println(content);
}
```

##### 添加到 ChatClient.Builder

将工具添加到默认工具中使用 `defaultTools()` 方法将默认工具添加到ChatClient.Builder。如果同时提供了默认工具和运行时工具，运行时工具将完全覆盖默认工具。
可以是 new 对象，也可以是 Bean 对象。底层用过 `ToolCallbacks.from(toolObjects)` 解析。

```java
@Test
public void getCurrentDateTimeBuilder() {
    ChatClient chatClient = builder
            .defaultTools(new DateTimeTools())
            .build();
    String content = chatClient.prompt()
            .user("明天是多少号？")
            .call()
            .content();
    System.out.println(content);
}
```

##### 添加到 ChatModel

将工具类实例传递给用于创建ChatModel的ToolCallingChatOptions实例的toolCallbacks()方法。如果同时提供了默认和运行时工具，运行时工具将完全覆盖默认工具。
可以是 new 对象，也可以是 Bean 对象。底层用过 `ToolCallbacks.from(toolObjects)` 解析。

```java
public void getCurrentDateTimeChatModel() {
    // 解析tool
    ToolCallback[] tools = ToolCallbacks.from(new DateTimeTools());
    ToolCallingChatOptions toolCallingChatOptions = ToolCallingChatOptions.builder()
            // 设置到聊天参数
            .toolCallbacks(tools)
            .build();
    // 创建 Prompt
    Prompt prompt = new Prompt("明天是多少号？", toolCallingChatOptions);
    // 调用
    String content = chatModel.call(prompt).getResult().getOutput().getText();
    System.out.println(content);
}
```

#### MethodToolCallback

通过以编程方式构建 `MethodToolCallback` 将方法转换为工具。

以 **MethodToolCallback.Builder** 构建MethodToolCallback实例并提供有关该工具的关键信息

| 方法                      | 描述                                                                                                                                |
|-------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| toolDefinition          | 定义工具名称、描述和输入schema的ToolDefinition实例。您可以使用ToolDefinition.Builder类构建它。**必需的**。                                                      |
| toolMetadata            | ToolMetadata实例，用于定义其他设置，例如是否应将结果直接返回给客户端，以及要使用的结果转换器。您可以使用ToolMetadata.Builder类构建它。                                               |
| toolMethod              | 表示工具方法的Method实例。**必需的**。                                                                                                          |
| toolObject              | 包含工具方法的对象实例。如果方法是静态的，可以省略此参数。                                                                                                     |
| toolCallResultConverter | 用于将工具调用的结果转换为ToolCallResultConverter对象以打回AI模型的String实例。如果未提供，将使用默认转换器（DefaultToolCallResultConverter）。[详细介绍](#result-converter) 。 |

以 **ToolDefinition.Builder** 构建ToolDefinition实例

| 方法          | 描述                                                                                                                              |
|-------------|---------------------------------------------------------------------------------------------------------------------------------|
| name        | 工具的名称。如果未提供，将使用方法名称。AI模型在调用工具时使用此名称来识别工具。因此，不允许在同一类中有两个同名的工具。对于特定的聊天请求，该名称必须在模型可用的所有工具中是唯一的。                                    |
| description | 工具的描述，模型可以使用它来理解何时以及如何调用工具。如果没有提供，方法名称将用作工具描述。但是，强烈建议提供详细的描述，因为这对模型理解工具的目的以及如何使用它至关重要。未能提供良好的描述可能会导致模型在应该使用工具的时候没有使用工具，或者使用不正确。 |
| inputSchema | 工具的输入参数的JSON schema。如果未提供，schema将根据方法参数自动生成。您可以使用@ToolParam注释提供有关输入参数的附加信息，例如描述或参数是必需的还是可选的。[详细介绍](#input-schema)。              |

以 **ToolMetadata.Builder** 可以构建ToolMetadata实例并为该工具定义其他设置

| 方法           | 描述                                                                                                   |
|--------------|------------------------------------------------------------------------------------------------------|
| returnDirect | 工具结果是应该直接返回给客户端还是传回模型。<br/> ture：工具调用的结果将直接返回给调用客户端。<br/> false：工具调用的结果返回给模型。 [详细介绍](#return-direct) |

```java
private MethodToolCallback getCurrentDateTimeMethodToolCallback() {
    // 反射获取工具方法
    Method getCurrentTime = ReflectionUtils.findMethod(DateTimeTools.class, "getCurrentTime");
    return MethodToolCallback.builder()
            // 定义工具名称、描述和输入schema的ToolDefinition实例。您可以使用ToolDefinition.Builder类构建它。
            // 必需的。
            .toolDefinition(ToolDefinition.builder(getCurrentTime)
                    // 描述信息
                    .description("获取用户所在时区的当前日期和时间。")
                    // 工具的输入参数的JSON schema。如果未提供，schema将根据方法参数自动生成。您可以使用@ToolParam注释提供有关输入参数的附加信息，例如描述或参数是必需的还是可选的。
                    .inputSchema(JsonSchemaGenerator.generateForMethodInput(getCurrentTime))
                    // 工具名称
                    .name("DateTimeTools")
                    .build())
            // ToolMetadata实例，用于定义其他设置
            .toolMetadata(ToolMetadata.builder()
                    // 工具结果是应该直接返回给调用客户端还是传回模型。
                    // ture：工具调用的结果将直接返回给调用客户端。
                    // false：工具调用的结果返回给模型
                    .returnDirect(false)
                    .build())
            // 表示工具方法的Method。
            // 必需的。
            .toolMethod(getCurrentTime)
            // 包含工具方法的对象实例。
            // 如果方法是静态的，可以省略此参数。
            .toolObject(new DateTimeTools())
            // 用于将工具调用的结果转换为ToolCallResultConverter对象以打回AI模型的String实例。如果未提供，将使用默认转换器（DefaultToolCallResultConverter）。
            .toolCallResultConverter(new DefaultToolCallResultConverter())
            .build();
}
```

##### 添加到 ChatClient

将工具添加到 `ChatClient` 中使用 `tools()` 方法设置。

```java
public void getCurrentDateTimeMethodToolCallbackChatClient() {
    ChatClient chatClient = builder.build();
    String content = chatClient.prompt()
            .user("明天是多少号？")
            .tools(getCurrentDateTimeMethodToolCallback())
            .call()
            .content();
    System.out.println(content);
}
```

##### 添加到 ChatClient.Builder

将工具添加到默认工具中使用 `defaultTools()` 方法将默认工具添加到ChatClient.Builder。如果同时提供了默认工具和运行时工具，运行时工具将完全覆盖默认工具。

```java
public void getCurrentDateTimeMethodToolCallbackBuilder() {
    ChatClient chatClient = builder
            .defaultTools(getCurrentDateTimeMethodToolCallback())
            .build();
    String content = chatClient.prompt()
            .user("明天是多少号？")
            .call()
            .content();
    System.out.println(content);
}
```

##### 添加到 ChatModel

将工具类实例传递给用于创建ChatModel的ToolCallingChatOptions实例的toolCallbacks()方法。如果同时提供了默认和运行时工具，运行时工具将完全覆盖默认工具。

```java
public void getCurrentDateTimeMethodToolCallbackChatModel() {
    ToolCallingChatOptions toolCallingChatOptions = ToolCallingChatOptions.builder()
            .toolCallbacks(getCurrentDateTimeMethodToolCallback())
            .build();
    Prompt prompt = new Prompt("明天是多少号？", toolCallingChatOptions);
    String content = chatModel.call(prompt).getResult().getOutput().getText();
    System.out.println(content);
}
```
### 函数工具(Function Tool)

Spring AI 支持通过下面两种方法定义函数工具
- 声明方式：使用 `@Bean` 注解
- 编程方式：通过 `FunctionToolCallback` 构建

当前不支持以下类型作为用作工具的方法的参数或返回类型：
- 原始类型
- Optional
- 集合类型（例如List、Map、Array、Set）
- 异步类型（例如CompletableFuture、Future）
- Reactive类型（例如Flow、Mono、Flux）

#### @Bean

您可以将工具定义为Spring bean，并让Spring AI在运行时使用ToolCallbackResolver接口（通过SpringBeanToolCallbackResolver实现）动态解析它们，而不是以编程方式指定工具。

此选项使您可以使用任何 `Function`、`Supplier`、`Consumer` 或` BiFunction` bean作为工具。

bean名称将用作工具名称，通过 `@Description` 指定描述

```java
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration(proxyBeanMethods = false)
public class WeatherTools {

    /**
     * tool请求
     */
    public record WeatherRequest(@ToolParam(description = "城市信息") String city) {

    }

    /**
     * tool响应
     */
    public record WeatherResponse(@ToolParam(description = "城市信息") String city,
                                  @ToolParam(description = "天气情况") String condition,
                                  @ToolParam(description = "温度") int temperature) {

    }

    @Bean("currentWeather")
    @Description("获取城市的天气情况")
    public Function<WeatherRequest, WeatherResponse> currentWeather() {
        return request -> {
            // 模拟天气查询逻辑
            return new WeatherResponse(request.city(), "晴天", 25);
        };
    }
}
```

##### 添加到 ChatClient

使用动态规范方法时，您可以将工具名称（即函数bean名称）传递给ChatClient的tools()方法。该工具仅适用于添加到其中的特定聊天请求。

```java
 @Autowired
private ChatClient.Builder builder;

@Autowired
private ChatModel chatModel;

/**
 * 使用@Bean注解添加到ChatClient。
 */
@Test
public void currentWeatherBeanChatClient() {
    ChatClient chatClient = builder.build();
    String content = chatClient.prompt()
            // 传入Bean的名称
            .tools("currentWeather")
            .user("长春天气怎么样？")
            .call()
            .content();
    System.out.println(content);
}
```

##### 添加到 ChatClient.Builder

使用动态规范方法时，您可以通过将工具名称传递给defaultTools()方法将默认工具添加到ChatClient.Builder。如果同时提供了默认工具和运行时工具，运行时工具将完全覆盖默认工具。

```java
@Autowired
private ChatClient.Builder builder;

@Autowired
private ChatModel chatModel;

/**
 * 使用@Bean注解添加到ChatClient.Builder。
 */
@Test
public void currentWeatherBeanChatClientBuilder() {
    ChatClient chatClient = builder
            // 传入Bean的名称
            .defaultTools("currentWeather")
            .build();
    String content = chatClient.prompt()
            .user("长春天气怎么样？")
            .call()
            .content();
    System.out.println(content);
}
```

##### 添加到 ChatModel

使用动态规范方法时，您可以将工具名称传递给用于调用ChatModel的ToolCallingChatOptions的toolNames()方法。该工具仅适用于添加到其中的特定聊天请求。

```java
@Autowired
private ChatClient.Builder builder;

@Autowired
private ChatModel chatModel;

/**
 * 使用@Bean注解添加到ChatModel
 */
@Test
public void currentWeatherBeanChatModel() {
    ToolCallingChatOptions toolCallingChatOptions = ToolCallingChatOptions.builder()
            // 传入Bean的名称
            .toolNames("currentWeather")
            .build();
    Prompt prompt = new Prompt("长春天气怎么样？", toolCallingChatOptions);
    String content = chatModel.call(prompt).getResult().getOutput().getText();
    System.out.println(content);
}
```

#### FunctionToolCallback

通过以编程方式构建 `FunctionToolCallback` 将方法转换为工具。

以 **FunctionToolCallback.Builder** 允许您构建FunctionToolCallback实例并提供有关该工具的关键信息：

| 方法                      | 描述                                                                                                                                          |
|-------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| name                    | 工具的名称。AI模型在调用工具时使用此名称来识别工具。因此，不允许在同一上下文中有两个同名的工具。对于特定的聊天请求，该名称必须在模型可用的所有工具中是唯一的。**必需的**。                                                    |
| toolFunction            | 表示工具方法的泛函对象（Function、Supplier、Consumer或BiFunction）。**必需的**。                                                                                 |
| description             | 工具的描述，模型可以使用它来理解何时以及如何调用工具。如果没有提供，方法名称将用作工具描述。但是，强烈建议提供详细的描述，因为这对模型理解工具的目的以及如何使用它至关重要。未能提供良好的描述可能会导致模型在应该使用工具的时候没有使用工具，或者使用不正确。             |
| inputType               | 函数输入的类型。**必需的**。                                                                                                                            |
| inputSchema             | 工具的输入参数的JSONschema。如果未提供，schema将基于inputType自动生成。您可以使用@ToolParam注释提供有关输入参数的附加信息，例如描述或参数是必需的还是可选的。默认情况下，所有输入参数都被认为是必需的。[详细介绍](#input-schema)。 |
| toolMetadata            | ToolMetadata实例，用于定义其他设置，例如是否应将结果直接返回给客户端，以及要使用的结果转换器。您可以使用ToolMetadata.Builder类构建它。                                                         |
| toolCallResultConverter | 用于将工具调用的结果转换为String对象以打回AI模型的ToolCallResultConverter实例。如果未提供，将使用默认转换器（DefaultToolCallResultConverter）。[详细介绍](#result-converter) 。           |

使用 `ToolMetadata.Builder` 可以构建ToolMetadata实例并为该工具定义其他设置：

| 方法           | 描述                                                                                                     |
|--------------|--------------------------------------------------------------------------------------------------------|
| returnDirect | 工具结果是应该直接返回给调用客户端还是传回模型。<br/> ture：工具调用的结果将直接返回给调用客户端。<br/> false：工具调用的结果返回给模型。 [详细介绍](#return-direct) |

WeatherServiceTools

```java
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.function.Function;

public class WeatherServiceTools implements Function<WeatherServiceTools.WeatherRequest, WeatherServiceTools.WeatherResponse>{

    @Override
    public WeatherResponse apply(WeatherRequest weatherRequest) {
        // 模拟天气查询逻辑
        return new WeatherResponse(weatherRequest.city(), "晴天", 25);
    }

    /**
     * tool请求
     */
    public record WeatherRequest(@ToolParam(description = "城市信息") String city) {

    }

    /**
     * tool响应
     */
    public record WeatherResponse(@ToolParam(description = "城市信息") String city,
                                  @ToolParam(description = "天气情况") String condition,
                                  @ToolParam(description = "温度") int temperature) {

    }
}
```

通过FunctionToolCallback方式构建工具

```java
/**
 * 通过FunctionToolCallback方式构建工具
 */
private FunctionToolCallback<WeatherServiceTools.WeatherRequest, WeatherServiceTools.WeatherResponse> getCurrentWeatherFunctionToolCallback() {
    return FunctionToolCallback
            .builder("currentWeather", new WeatherServiceTools())
            .description("获取城市的天气情况")
            .inputType(WeatherServiceTools.WeatherRequest.class)
            .toolMetadata(ToolMetadata.builder()
                    .returnDirect(false)
                    .build())
            .build();
}
```

##### 添加到 ChatClient

```java
@Autowired
private ChatClient.Builder builder;

@Autowired
private ChatModel chatModel;

/**
 * 通过FunctionToolCallback方式将工具添加到ChatClient
 */
@Test
public void currentWeatherFunctionToolCallbackChatClient() {
    ChatClient chatClient = builder.build();
    String content = chatClient.prompt()
            .tools(getCurrentWeatherFunctionToolCallback())
            .user("长春天气怎么样？")
            .call()
            .content();
    System.out.println(content);
}
```

##### 添加到 ChatClient.Builder

```java
@Autowired
private ChatClient.Builder builder;

@Autowired
private ChatModel chatModel;

/**
 * 通过FunctionToolCallback方式将工具添加到ChatClient.Builder
 */
@Test
public void currentWeatherFunctionToolCallbackChatClientBuilder() {
    ChatClient chatClient = builder
            .defaultTools(getCurrentWeatherFunctionToolCallback())
            .build();
    String content = chatClient.prompt()
            .user("长春天气怎么样？")
            .call()
            .content();
    System.out.println(content);
}
```

##### 添加到 ChatModel

```java
@Autowired
private ChatClient.Builder builder;

@Autowired
private ChatModel chatModel;

/**
 * 使用WeatherFunctionToolCallback注解添加到ChatModel
 */
@Test
public void currentWeatherFunctionToolCallbackChatModel() {
    ToolCallingChatOptions toolCallingChatOptions = ToolCallingChatOptions.builder()
            .toolCallbacks(getCurrentWeatherFunctionToolCallback())
            .build();
    Prompt prompt = new Prompt("长春天气怎么样？", toolCallingChatOptions);
    String content = chatModel.call(prompt).getResult().getOutput().getText();
    System.out.println(content);
}
```

::: details 还可以通过 ToolCallbackProvider 来构建工具传入到ChatModel、ChatClient.Builder、ChatClient中。

```java
 public void useToolCallbackProvider() {
    // 创建ToolCallbackProvider
    ToolCallbackProvider provider = MethodToolCallbackProvider.builder().toolObjects(new DateTimeTools()).build();
    ChatClient chatClient = builder.defaultTools(provider).build();
    String content = chatClient.prompt()
            .tools(provider)
            .user("明天是多少号")
            .call()
            .content();
    System.out.println(content);
}
```

:::

## 输入协议(Input Schema){#input-schema}

向AI模型提供工具时，模型需要知道调用工具的输入类型的schema，schema用于了解如何调用工具和准备工具请求，Spring AI内置支持通过JsonSchemaGenerator类为工具生成输入类型的JSON Schema，schema作为ToolDefinition的一部分提供。

格式如下：

```json
{
    "type": "object",
    "properties": {
        "location": {
            "type": "string"
        },
        "unit": {
            "type": "string",
            "enum": ["C", "F"]
        }
    },
    "required": ["location", "unit"]
}
```

内置的生成工具类，他们都会解析方法上的注解为您构建 Input Schema：
- `ToolDefinition.from(Method method)` 可以生成 ToolDefinition 对象中包含 Input Schema 信息。
- `JsonSchemaGenerator.generateForMethodInput(Method method)` 可以生成 Input Schema 信息。

Spring AI内置支持使用以下注解之一生成输入参数的描述：
- `@ToolParam(description = "", required = true)` SpringAI
- `@JsonClassDescription(description = "")` Jackson
- `@JsonPropertyDescription(description = "")` Jackson
- `@Schema(description = "", required = true)` Swagger
- `@JsonProperty(required = false)` Jackson
- `@Nullable` 解析为是否必填

## 工具结果转换(Result Converter){#result-converter}

工具调用的结果使用ToolCallResultConverter序列化，然后发送回AI模型。ToolCallResultConverter接口提供了一种将工具调用结果转换为String对象的方法。

```java
@FunctionalInterface
public interface ToolCallResultConverter {

    /**
     * 将结果转换为String返回给大模型
     */
	String convert(@Nullable Object result, @Nullable Type returnType);

}
```

**结果必须是可序列化的类型**。默认情况下，结果使用Jackson（DefaultToolCallResultConverter）序列化为JSON，但您可以通过提供自己的ToolCallResultConverter实现来自定义序列化过程。

## 工具上下文(Tool Context)

Spring AI支持通过ToolContextAPI向工具传递额外的上下文信息。此特征允许您提供额外的、用户提供的数据，这些数据可以与AI模型传递的工具参数一起在工具执行中使用。

![jfoeaspsdwfasdas.png](/images/jfoeaspsdwfasdas.png){v-zoom}{loading="lazy"}

自定义 ToolContextTools 使用 ToolContext 来实现。

```java
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 工具中可以使用 ToolContext 接收程序传递的信息
 *
 * @author future0923
 */
public class ToolContextTools {

    public record Customer(Long id, String tenantId, String name) {

        public static Customer findById(Long id, String tenantId) {
            return new Customer(id, tenantId, "张三");
        }
    }

    @Tool(description = "获取用户信息")
    public Customer getCustomerInfo(@ToolParam(description = "用户id") Long id, ToolContext toolContext) {
        // 从上下文中获取
        return Customer.findById(id, (String) toolContext.getContext().get("tenantId"));
    }

}
```

通过 `toolContext()` 给工具传入上下文

```java
public class ToolContextToolsTest extends AbstractFunctionCallingApplicationTest {

    @Autowired
    private ChatClient.Builder builder;

    @Autowired
    private ChatModel chatModel;

    /**
     * 使用 ChatClient 的 toolContext 设置上下文信息
     */
    @Test
    public void contextChatClient() {
        ChatClient chatClient = builder.build();
        String content = chatClient.prompt("获取id为18的用户信息")
                .tools(new ToolContextTools())
                .toolContext(Map.of("tenantId", "acme"))
                .call()
                .content();
        System.out.println(content);
    }

    /**
     * 使用 ChatModel 的 toolContext 设置上下文信息
     */
    @Test
    public void contextChatModel() {
        ToolCallback[] customerTools = ToolCallbacks.from(new ToolContextTools());
        ChatOptions chatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(customerTools)
                .toolContext(Map.of("tenantId", "acme"))
                .build();
        Prompt prompt = new Prompt("获取id为18的用户信息", chatOptions);
        System.out.println(chatModel.call(prompt).getResult().getOutput().getText());
    }
}
```

## 直接返回(Return Direct){#return-direct}

默认情况下，工具调用的结果作为响应发送回模型。然后，模型可以使用该结果继续对话。

在某些情况下，您宁愿将结果直接返回给调用方，而不是将其发送回模型。例如，如果您构建了一个依赖于RAG工具的代理，您可能希望将结果直接返回给调用方，而不是将其发送回模型进行不必要的后处理。或者您有某些工具应该结束代理的推理循环。

每个ToolCallback实现可以定义工具调用的结果是应该直接返回给调用方还是发送回模型。默认情况下，结果被发送回模型。但是您可以每个工具更改这种行为。

![jowifqjnsfokwds.png](/images/jowifqjnsfokwds.png){v-zoom}{loading="lazy"}

1. 当我们想要使一个工具对模型可用时，我们将其定义包含在聊天请求中，如果我们希望工具执行的结果直接返回给调用方，我们将returnDirect属性设置为true。
2. 当模型决定调用一个工具时，它会发送一个响应，其中包含工具名称和按照定义的schema建模的输入参数。
3. 应用程序负责使用工具名称来识别和执行具有提供的输入参数的工具。
4. 工具调用的结果由应用程序处理。
5. 应用程序将工具调用结果直接发送给调用方，而不是将其发送回模型。

上面的 @Tool 和 ToolMetadata 都可以设置 returnDirect 属性。

## 工具异常处理

当工具调用失败时，异常作为ToolExecutionException传播，可以捕获它来处理错误。ToolExecutionExceptionProcessor可用于处理具有两种结果的ToolExecutionException：要么生成要发送回AI模型的错误消息，要么抛出要由调用方处理的异常。

```java
@FunctionalInterface
public interface ToolExecutionExceptionProcessor {

    /**
     * 处理异常，
     * 可以返回string结果给大模型。
     * 也可以直接报出异常
     */
	String process(ToolExecutionException exception);

}
```

默认实现 DefaultToolExecutionExceptionProcessor 方式为返回错误信息给大模型。

```java
public class DefaultToolExecutionExceptionProcessor implements ToolExecutionExceptionProcessor {
    
	@Override
	public String process(ToolExecutionException exception) {
		Assert.notNull(exception, "exception cannot be null");
		if (alwaysThrow) {
			throw exception;
		}
		logger.debug("Exception thrown by tool: {}. Message: {}", exception.getToolDefinition().name(),
				exception.getMessage());
		return exception.getMessage();
	}

}

```

默认ToolCallingManager（DefaultToolCallingManager）在内部使用该ToolExecutionExceptionProcessor来处理工具执行期间的异常。