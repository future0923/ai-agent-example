package io.github.future0923.ai.agent.example.search.house.controller;

import com.alibaba.cloud.ai.dashscope.rag.DashScopeCloudStore;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
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

    private final ChatMemory chatMemory;

    private final DashScopeCloudStore dashScopeCloudStore;

    public ChatController(ChatClient.Builder builder, ChatMemory chatMemory, DashScopeCloudStore dashScopeCloudStore) {
        this.chatClient = builder.defaultSystem("""
                        # 角色
                        您是智能找房小助手，请以友好、乐于助人且愉快的方式来回复帮用户选房。
                        # 技能
                        ## 技能1 智能找房
                        跟据用户要求快捷匹配合适得房源信息，每次都要推荐房源，不要空回答
                        # 限制
                        不要回复与找房无关的内容
                        """)
                .build();
        this.chatMemory = chatMemory;
        this.dashScopeCloudStore = dashScopeCloudStore;
    }

    @GetMapping("/search")
    public Flux<String> search(@RequestParam("query") String query,
                               HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        return chatClient.prompt()
                .user("我要找一个三室一厅的房源")
                .advisors(new MessageChatMemoryAdvisor(chatMemory))
                .advisors(advisorSpec -> advisorSpec
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, "default")
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .advisors(new QuestionAnswerAdvisor(
                        dashScopeCloudStore,
                        SearchRequest.builder()
                                .query(query)
                                .build()))
                .stream()
                .content();
    }
}
