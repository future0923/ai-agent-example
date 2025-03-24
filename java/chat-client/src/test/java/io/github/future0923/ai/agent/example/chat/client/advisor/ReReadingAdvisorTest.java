package io.github.future0923.ai.agent.example.chat.client.advisor;

import io.github.future0923.ai.agent.example.chat.client.AbstractChatClientApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * @author future0923
 */
public class ReReadingAdvisorTest extends AbstractChatClientApplicationTest {

    @Autowired
    private ChatClient.Builder builder;

    @Test
    public void reReadingAdvisor() {
        ChatClient chatClient = builder.build();
        Flux<String> flux = chatClient.prompt()
                .advisors(new ReReadingAdvisor())
                .advisors(new SimpleLoggerAdvisor())
                .user("绿茶的功效是什么？")
                .stream()
                .content();
        StepVerifier
                .create(flux)
                .thenConsumeWhile(res -> {
                    System.out.print(res);
                    return true;
                })
                .verifyComplete();
    }

}