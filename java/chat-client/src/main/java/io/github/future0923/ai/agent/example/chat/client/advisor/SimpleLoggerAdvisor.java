package io.github.future0923.ai.agent.example.chat.client.advisor;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

/**
 * 增加日志打印SimpleLoggerAdvisor
 *
 * @author future0923
 */
public class SimpleLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(SimpleLoggerAdvisor.class);

    @NotNull
    @Override
    public AdvisedResponse aroundCall(@NotNull AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        logger.info("before: {}", advisedRequest);
        // 调用下一个Advisor
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
        logger.info("AFTER: {}", advisedResponse);
        return advisedResponse;
    }

    @NotNull
    @Override
    public Flux<AdvisedResponse> aroundStream(@NotNull AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        logger.info("before: {}", advisedRequest);
        // 调用下一个Advisor
        Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);
        // MessageAggregator是一个实用程序类，它将Flux响应聚合成一个AdvisedResponse。这对于日志记录或其他观察整个响应而不是流中单个项目的处理很有用。
        // 请注意，您不能更改MessageAggregator中的响应，因为它是只读操作。
        return new MessageAggregator().aggregateAdvisedResponse(advisedResponses, advisedResponse -> logger.debug("AFTER: {}", advisedResponse));
    }

    /**
     * Advisor名称
     */
    @NotNull
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 执行顺序，越小越先执行
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
