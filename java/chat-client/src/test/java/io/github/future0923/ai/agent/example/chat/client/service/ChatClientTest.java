package io.github.future0923.ai.agent.example.chat.client.service;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import io.github.future0923.ai.agent.example.chat.client.AbstractChatClientApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;

/**
 * ChatClient请求时设置参数
 *
 * @author future0923
 */
public class ChatClientTest extends AbstractChatClientApplicationTest {

    @Autowired
    private ChatClient.Builder builder;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private VectorStore vectorStore;

    @Test
    public void chat() {
        ChatClient client = builder.build();
        Flux<String> flux = client
                .prompt()
                // 携带的默认系统角色信息
                .system("""
                        # 角色
                        你是一个旅游小助手，可以帮助取消机票预订。
                        ## 技能
                        今天的日期是 {current_date}。
                        ### 技能 1：快捷取消预订
                        1. 您必须始终从用户处获取以下信息：预订号、客户姓名。
                        ## 限制:
                        - 仅处理与旅游信息相关的内容，拒绝回答与AI选房无关的任何话题。
                        """)
                // 填充 PromptTemplate 参数
                .system(promptSystemSpec -> promptSystemSpec.param("current_date", LocalDate.now().toString()))
                .user("帮我取消预订，张三 101，并告诉我费用是多少")
                // 添加 Advisor
                .advisors(
                        // 内存聊天记忆
                        new PromptChatMemoryAdvisor(chatMemory),
                        // 日志打印
                        new SimpleLoggerAdvisor(),
                        // 向量信息
                        new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().build())
                )
                // 配置内存聊天记录的参数
                .advisors(advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, 1))
                .advisors(advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, AbstractChatMemoryAdvisor.DEFAULT_CHAT_MEMORY_RESPONSE_SIZE))
                // 向量查询需要对接向量模型才能更好搜索
                //.advisors(advisorSpec -> new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().query(message).build()))
                .options(DashScopeChatOptions.builder()
                        .withTopP(0.7)
                        .build())
                // 传递 Tool 只简单用一下，后面有详细的 Tools 使用.
                // BookingTools#cancelBooking()
                .tools("cancelBooking")
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
