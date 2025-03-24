package io.github.future0923.ai.agent.example.chat.memory.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chatMemory")
public class ChatMemoryController {

    private final ChatMemory memoryChatMemory;

    private final ChatMemory mysqlChatMemory;

    private final ChatMemory redisChatMemory;

    private final ChatClient chatClient;


    public ChatMemoryController(@Qualifier("memoryChatMemory") ChatMemory memoryChatMemory, @Qualifier("mysqlChatMemory") ChatMemory mysqlChatMemory, @Qualifier("redisChatMemory") ChatMemory redisChatMemory, ChatClient.Builder builder) {
        this.memoryChatMemory = memoryChatMemory;
        this.mysqlChatMemory = mysqlChatMemory;
        this.redisChatMemory = redisChatMemory;
        this.chatClient = builder.build();
    }

    @GetMapping("/memory")
    public Flux<String> memoryChatMemory(
            @RequestParam("prompt") String prompt,
            @RequestParam("chatId") String chatId,
            HttpServletResponse response
    ) {
        response.setCharacterEncoding("UTF-8");
        return chatClient.prompt()
                .user(prompt)
                .advisors(new MessageChatMemoryAdvisor(memoryChatMemory))
                .advisors(advisorSpec -> advisorSpec
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .stream()
                .content();
    }

    @GetMapping("/mysql")
    public Flux<String> mysqlChatMemory(
            @RequestParam("prompt") String prompt,
            @RequestParam("chatId") String chatId,
            HttpServletResponse response
    ) {
        response.setCharacterEncoding("UTF-8");
        return chatClient.prompt()
                .user(prompt)
                .advisors(new MessageChatMemoryAdvisor(mysqlChatMemory))
                .advisors(advisorSpec -> advisorSpec
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .stream()
                .content();
    }

    @GetMapping("/redis")
    public Flux<String> redisChatMemory(
            @RequestParam("prompt") String prompt,
            @RequestParam("chatId") String chatId,
            HttpServletResponse response
    ) {
        response.setCharacterEncoding("UTF-8");
        return chatClient.prompt()
                .user(prompt)
                .advisors(new MessageChatMemoryAdvisor(redisChatMemory))
                .advisors(advisorSpec -> advisorSpec
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .stream()
                .content();
    }

}
