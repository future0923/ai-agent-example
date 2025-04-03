# 智能机票应用搭建

主要功能
- 基于 AI 大模型与用户对话，理解用户自然语言表达的需求
- 支持多轮连续对话，能在上下文中理解用户意图
- 理解机票操作相关的术语与规范并严格遵守，如航空法规、退改签规则等
- 在必要时可调用工具辅助完成机票操作任务

## Spring AI 功能点

- [ChatClient](../spring/chat-client)：使用流式 Fluent API 把多个组件组装起来，成为一个智能体 Agent。
- [Tool](../spring/function-calling)：使用 Tool 让模型可以调用工具完成机票操作功能。
- [ChatMemory](../spring/chat-memory)：通过聊天记忆使LLM可以理解上下文，从而理解用户的意图。
- [Rag](../spring/rag)：通过 Rag 让模型可以访问知识库，使LLM具有航空法规、退改签规则知识。
- [VectorStore](../spring/vector-store)：使用向量数据库存储知识。

## 示例

### ChatMemory

目前使用内存来存储聊天记忆，其他持久化方式可以查看[ChatMemory](../spring/chat-memory)自己修改。

```java
@Configuration
public class Config {

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }
}
```

### VectorStore

目前使用内存来存储向量，其他持久化方式可以查看[VectorStore](../spring/vector-store)自己修改。

```java
@Configuration
public class Config {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
```

### Rag

读取航空规则的txt文件，并存储到向量数据库。

```java
@Value("classpath:rag/terms-of-service.txt")
private Resource resource;

@PostConstruct
public void init() {
    vectorStore.add(new TokenTextSplitter().transform(new TextReader(resource).read()));
}
```

### Tool

FlightBookingTools 编写工具让大模型具有机票操作的能力。

```java
import io.github.future0923.ai.agent.example.flight.booking.entity.FlightBooking;
import io.github.future0923.ai.agent.example.flight.booking.service.FlightBookingService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * @author future0923
 */
@Component
public class FlightBookingTools {

    private final FlightBookingService service;

    public FlightBookingTools(FlightBookingService service) {
        this.service = service;
    }

    @Tool(description = "获取用户所有的机票信息")
    public List<FlightBooking> getUserBookings(@ToolParam(description = "用户名") String username) {
        return service.getUserBookings(username);
    }

    public record BookingRecordDTO(
            @ToolParam(description = "起飞日期") LocalDate bookingTo,
            @ToolParam(description = "用户名") String name,
            @ToolParam(description = "出发地") String from,
            @ToolParam(description = "目的地") String to) {

    }

    @Tool(description = "预订机票")
    public String bookings(@ToolParam(description = "预订机票必要参数") BookingRecordDTO dto) {
        return service.bookings(dto);
    }

    @Tool(description = "机票取消预订")
    public String cancelBookings(@ToolParam(description = "预订号") String bookingNumber) {
        return service.cancelBookings(bookingNumber);
    }

    @Tool(description = "根据预定号查旬机票信息")
    public FlightBooking bookingsInfo(@ToolParam(description = "预订号") String bookingNumber) {
        return service.bookingsInfo(bookingNumber);
    }
}
```

FlightBookingService service层

```java
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.future0923.ai.agent.example.flight.booking.dao.FlightBookingDao;
import io.github.future0923.ai.agent.example.flight.booking.entity.FlightBooking;
import io.github.future0923.ai.agent.example.flight.booking.enums.BookingClass;
import io.github.future0923.ai.agent.example.flight.booking.enums.BookingStatus;
import io.github.future0923.ai.agent.example.flight.booking.tools.FlightBookingTools;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

/**
 * @author future0923
 */
@Service
public class FlightBookingService {

    private final FlightBookingDao dao;

    public FlightBookingService(FlightBookingDao dao) {
        this.dao = dao;
    }

    public List<FlightBooking> getUserBookings(String username) {
        LambdaQueryWrapper<FlightBooking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FlightBooking::getName, username);
        return dao.selectList(queryWrapper);
    }

    public String bookings(FlightBookingTools.BookingRecordDTO dto) {
        LambdaQueryWrapper<FlightBooking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FlightBooking::getName, dto.name());
        queryWrapper.eq(FlightBooking::getFrom, dto.from());
        queryWrapper.eq(FlightBooking::getTo, dto.to());
        if (dao.exists(queryWrapper)) {
            return "对不起你已经预订过了";
        }
        FlightBooking booking = new FlightBooking();
        booking.setBookingNumber(new Random().nextInt(1000000) + "");
        booking.setDate(LocalDate.now());
        booking.setBookingTo(dto.bookingTo());
        booking.setName(dto.name());
        booking.setFrom(dto.from());
        booking.setTo(dto.to());
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setBookingClass(BookingClass.ECONOMY);
        dao.insert(booking);
        return "预订成功";
    }

    public String cancelBookings(String bookingNumber) {
        LambdaQueryWrapper<FlightBooking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FlightBooking::getBookingNumber, bookingNumber);
        List<FlightBooking> flightBookings = dao.selectList(queryWrapper);
        if (flightBookings.isEmpty()) {
            return "预定号不存在";
        }
        if (flightBookings.get(0).getBookingStatus() == BookingStatus.CANCELLED) {
            return "预定已经取消";
        }
        flightBookings.get(0).setBookingStatus(BookingStatus.CANCELLED);
        dao.updateById((flightBookings.get(0)));
        return "取消预订成功";
    }

    public FlightBooking bookingsInfo(String bookingNumber) {
        return dao.selectById(bookingNumber);
    }
}
```

FlightBookingDao dao层

```java
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.future0923.ai.agent.example.flight.booking.entity.FlightBooking;

/**
 * @author future0923
 */
public interface FlightBookingDao extends BaseMapper<FlightBooking> {
}
```

FlightBooking 实体

```java
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.future0923.ai.agent.example.flight.booking.enums.BookingClass;
import io.github.future0923.ai.agent.example.flight.booking.enums.BookingStatus;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDate;

@TableName("flight_booking")
public class FlightBooking {

    @ToolParam(description = "预定号")
    @TableId
    private String bookingNumber;
    @ToolParam(description = "预定日期")
    private LocalDate date;
    @ToolParam(description = "起飞日期")
    private LocalDate bookingTo;
    @ToolParam(description = "用户名")
    private String name;
    @ToolParam(description = "出发地")
    @TableField("`from`")
    private String from;
    @ToolParam(description = "目的地")
    @TableField("`to`")
    private String to;
    @ToolParam(description = "状态，取值为 CONFIRMED已确认, COMPLETED已完成, CANCELLED已取消")
    private BookingStatus bookingStatus;
    @ToolParam(description = "机票类型，ECONOMY经济舱, PREMIUM_ECONOMY豪华经济舱, BUSINESS头等舱")
    private BookingClass bookingClass;

    public String getBookingNumber() {
        return bookingNumber;
    }

    public void setBookingNumber(String bookingNumber) {
        this.bookingNumber = bookingNumber;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getBookingTo() {
        return bookingTo;
    }

    public void setBookingTo(LocalDate bookingTo) {
        this.bookingTo = bookingTo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public BookingClass getBookingClass() {
        return bookingClass;
    }

    public void setBookingClass(BookingClass bookingClass) {
        this.bookingClass = bookingClass;
    }
}
```

### ChatClient

通过 ChatClient 提供接口实现智能客服

```java
import io.github.future0923.ai.agent.example.flight.booking.tools.FlightBookingTools;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author future0923
 */
@RestController
public class ChatController {

    private final ChatClient chatClient;

    private final FlightBookingTools tools;

    private final ChatMemory chatMemory;

    private final VectorStore vectorStore;

    @Value("classpath:rag/terms-of-service.txt")
    private Resource resource;

    @PostConstruct
    public void init() {
        vectorStore.add(new TokenTextSplitter().transform(new TextReader(resource).read()));
    }

    public ChatController(ChatClient.Builder builder, FlightBookingTools tools, ChatMemory chatMemory, VectorStore vectorStore) {
        this.chatClient = builder
                .defaultSystem("""
                        # 角色
                        您是航空公司聊天小助手，请以友好、乐于助人且愉快的方式来回复，还可以帮用户选房。
                        # 技能
                        ## 技能1 客户机票信息查询
                        必须通过用户提供的用户名信息查询用户搜索的机票信息，如果用户之前已经提供过了，请检查消息历史记录以获取用户名信息。
                        ## 技能2 机票预订功能
                        友好的向用户搜集机票预订必要信息，帮助用户完成机票预订功能，用户输入要精准识别，读取失败是要耐心给出操作提示。
                        ## 技能3 机票取消预订功能
                        用户需要时需要提供预定号，可以查询用户的机票信息展示给用户机票信息，让用户选择取消那个预订，当用户输入预定号时取消预订。
                        ## 技能4 智能找房
                        跟据用户要求快捷匹配合适得房源信息，每次都要推荐房源，不要空回答
                        # 限制
                        不要回复与机票操作或找房无关的内容
                        """)
                .build();
        this.tools = tools;
        this.chatMemory = chatMemory;
        this.vectorStore = vectorStore;
    }

    @GetMapping("/chat")
    public Flux<String> chat(@RequestParam("query") String query,
                             HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        return chatClient.prompt()
                .user(query)
                .advisors(new MessageChatMemoryAdvisor(chatMemory))
                .advisors(advisorSpec -> advisorSpec
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, "default")
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .advisors(new QuestionAnswerAdvisor(
                        vectorStore,
                        SearchRequest.builder()
                                .query(query)
                                .build()))
                .tools(tools)
                .stream()
                .content();
    }
}
```

### 源码

[源码位置](https://github.com/future0923/ai-agent-example/tree/main/java/flight-booking)

### 效果

- 查看自己的机票信息
- 提供信息可以预订机票
- 提供预定号可以取消机票
- 可以询问退订政策
- 提供预定号可以查看这个预订取消可以扣多少钱