package io.github.future0923.ai.agent.example.web.search.advisor;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * 返回思考内容，返回格式<think>%s</think>为内容
 *
 * @author future0923
 */
public class ThinkingContentAdvisor implements BaseAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(ThinkingContentAdvisor.class);

    private final int order;

    public ThinkingContentAdvisor(Integer order) {
        this.order = order != null ? order : 0;
    }

    @NotNull
    @Override
    public AdvisedRequest before(@NotNull AdvisedRequest request) {
        return request;
    }

    @NotNull
    @Override
    public AdvisedResponse after(AdvisedResponse advisedResponse) {
        ChatResponse resp = advisedResponse.response();
        if (Objects.isNull(resp)) {
            return advisedResponse;
        }
        logger.debug(String.valueOf(resp.getResults().get(0).getOutput().getMetadata()));
        String reasoningContent = String.valueOf(resp.getResults().get(0).getOutput().getMetadata().get("reasoningContent"));
        if (StringUtils.hasText(reasoningContent)) {
            List<Generation> thinkGenerations = resp.getResults().stream()
                    .map(generation -> {
                        AssistantMessage output = generation.getOutput();
                        AssistantMessage thinkAssistantMessage = new AssistantMessage(
                                String.format("<think>%s</think>", reasoningContent) + output.getText(),
                                output.getMetadata(),
                                output.getToolCalls(),
                                output.getMedia()
                        );
                        return new Generation(thinkAssistantMessage, generation.getMetadata());
                    }).toList();
            ChatResponse thinkChatResp = ChatResponse.builder().from(resp).generations(thinkGenerations).build();
            return AdvisedResponse.from(advisedResponse).response(thinkChatResp).build();
        }
        return advisedResponse;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

}
