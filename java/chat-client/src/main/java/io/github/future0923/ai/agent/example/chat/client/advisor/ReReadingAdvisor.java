package io.github.future0923.ai.agent.example.chat.client.advisor;

import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * ReReadingAdvisor 的作用是——在发送用户问题给 AI 之前，自动把问题“重复一遍”，作为 prompt 的一部分，以期获得更认真或更准确的回答。
 * 目的：
 * <li>强调问题：通过重复问题，可以提示模型“认真阅读”或“更准确理解”。
 * <li>触发更好的回答：有些模型在 prompt 被强调、重复时，表现得更准确、稳定。
 * <li>用于链式调用的中间步骤：可能是更大 Advisor 链的一环，比如用来标记、记录或后续引用 {re2_input_query}。
 *
 * @author future0923
 */
public class ReReadingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    @NotNull
    @Override
    public AdvisedResponse aroundCall(@NotNull AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        return chain.nextAroundCall(before(advisedRequest));
    }

    @NotNull
    @Override
    public Flux<AdvisedResponse> aroundStream(@NotNull AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        return chain.nextAroundStream(before(advisedRequest));
    }

    @NotNull
    @Override
    public String getName() {
        return ReReadingAdvisor.class.getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 改写 Prompt 请求。在发送用户问题给 AI 之前，自动把问题“重复一遍”，作为 prompt 的一部分，以期获得更认真或更准确的回答。
     */
    private AdvisedRequest before(AdvisedRequest advisedRequest) {
        Map<String, Object> advisedUserParams = new HashMap<>(advisedRequest.userParams());
        // 将原始用户输入保存为参数 re_input_query。
        advisedUserParams.put("re_input_query", advisedRequest.userText());
        // 改写 Prompt 请求如下：
        // [原始问题]
        //Read the question again: [原始问题]
        return AdvisedRequest.from(advisedRequest)
                .userText("""
                        {re_input_query}
                        Read the question again: {re_input_query}
                        """)
                .userParams(advisedUserParams)
                .build();
    }

}
