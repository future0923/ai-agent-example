package io.github.future0923.ai.agent.example.chat.client.service;

import io.github.future0923.ai.agent.example.chat.client.AbstractChatClientApplicationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author future0923
 */
public class ChatClientBuilderTest extends AbstractChatClientApplicationTest {

    @Autowired
    private ChatClient.Builder builder;

    private ChatClient client;


    @Test
    public void a() {

    }

}
