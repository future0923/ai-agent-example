package io.github.future0923.ai.agent.example.flight.booking.controller;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeCloudStore;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeStoreOptions;
import io.github.future0923.ai.agent.example.flight.booking.tools.FlightBookingTools;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author future0923
 */
@RestController
public class ChatController {

    private final ChatClient chatClient;

    private final ChatClient.Builder builder;

    private final FlightBookingTools tools;

    private final ChatMemory chatMemory;

    private final VectorStore vectorStore;

    private DashScopeCloudStore dashScopeCloudStore;

    @Value("classpath:rag/terms-of-service.txt")
    private Resource resource;

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @GetMapping("/add")
    public void add() {
        vectorStore.add(new TokenTextSplitter().transform(new TextReader(resource).read()));
        dashScopeCloudStore = new DashScopeCloudStore(
                new DashScopeApi(apiKey),
                new DashScopeStoreOptions("二手房信息")
        );
    }

    public ChatController(ChatClient.Builder builder, FlightBookingTools tools, ChatMemory chatMemory, VectorStore vectorStore) {
        this.builder = builder;
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
                .advisors(new QuestionAnswerAdvisor(
                        dashScopeCloudStore,
                        SearchRequest.builder()
                                .query(query)
                                .build()
                ))
                .tools(tools)
                .stream()
                .content();
    }

    @GetMapping("/search")
    public Flux<String> search(@RequestParam("query") String query,
                               HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        ChatClient client = builder.defaultSystem("""
                        # 角色
                        您是智能找房小助手，请以友好、乐于助人且愉快的方式来回复帮用户选房。
                        # 技能
                        ## 技能1 智能找房
                        跟据用户要求快捷匹配合适得房源信息，每次都要推荐房源，不要空回答
                        # 限制
                        不要回复与找房无关的内容
                        """)
                .build();
        return client.prompt()
                .user("我要找一个三室一厅的房源")
                .advisors(new QuestionAnswerAdvisor(
                        dashScopeCloudStore,
                        SearchRequest.builder()
                                .query(query)
                                .build()))
                .stream()
                .content();
    }
}
