package io.github.future0923.ai.agent.example.chat.models.service;

import com.alibaba.cloud.ai.dashscope.api.DashScopeResponseFormat;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import io.github.future0923.ai.agent.example.chat.models.AbstractChatModelsApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * 聊天选项{@link ChatOptions}示例
 *
 * @author future0923
 */
public class ChatOptionsTest extends AbstractChatModelsApplicationTest {

    /**
     * 聊天模型
     * SpringAiAlibaba自动注入了{@link DashScopeChatModel}
     */
    @Autowired
    private ChatModel chatModel;

    /**
     * 请求大模型时通过{@link ChatOptions}传递参数。
     * 演示环境使用的 Spring Ai Alibaba 使用的是{@link DashScopeChatOptions}和{@link ChatOptions}参数有差异，所以这里都注释了。
     * 不同提供商实现的{@link ChatOptions}参数有差异有差异
     */
    @Test
    public void chatOptionsSpring() throws InterruptedException {
        String prompt = "长春怎么样";
        // 创建一个ChatOptions对象
        ChatOptions chatOptions = ChatOptions.builder()
                // 使用的模型
                //.model("qwen-max")
                // 频率惩罚，用于减少 AI 生成重复内容的可能性。正数的惩罚值，可以让模型生成更多元化的数据
                //.frequencyPenalty(0.2)
                // 限制 AI 生成的最大 token 数，控制响应的长度
                //.maxTokens(512)
                // 存在惩罚，提高 AI 生成新内容的倾向，减少已出现过的内容。正数值 AI 更倾向于生成未提及过的内容
                //.presencePenalty(0.3)
                // 停止序列，AI 遇到这些字符串时会停止生成。
                //.stopSequences(Collections.singletonList("</stop>"))
                // 控制随机性，影响输出的多样性。值越高，生成越随机；值越低，生成越确定
                // 如：0.2（更确定的回答，适合代码生成）
                // 如：1.0（更随机的回答，适合创意写作）
                //.temperature(0.9)
                // 限制候选 token 选择范围（即只从前 K 个最可能的 token 里采样）。
                // 值越低，AI 生成的内容越确定
                // 示例值: 50（从前 50 个最有可能的词中采样）
                //.topK(50)
                // 核采样（Top-p 采样），只从概率质量总和超过 P 的 token 中选择。
                // 较低值可减少随机性
                // 如：0.9（采样前 90% 的概率质量）
                // 如：0.3（更确定的回答）
                //.topP(0.9)
                .build();
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(prompt, chatOptions));
        StepVerifier.create(flux)
                .thenConsumeWhile(chatResponse -> {
                    // ChatResponse [metadata={ id: 9c6ad8ea-51bc-9036-8dc1-d0009ba506e6, usage: TokenUsage[outputTokens=1, inputTokens=10, totalTokens=11], rateLimit: org.springframework.ai.chat.metadata.EmptyRateLimit@5f63a078 }, generations=[Generation[assistantMessage=AssistantMessage [messageType=ASSISTANT, toolCalls=[], textContent=长春, metadata={finishReason=NULL, id=9c6ad8ea-51bc-9036-8dc1-d0009ba506e6, role=ASSISTANT, messageType=ASSISTANT, reasoningContent=}], chatGenerationMetadata=DefaultChatGenerationMetadata[finishReason='NULL', filters=0, metadata=0]]]]
                    System.out.print(chatResponse.getResult().getOutput().getText());
                    return true;
                })
                .verifyComplete();
    }

    /**
     * Spring Ai Alibaba {@link DashScopeChatOptions} 的参数
     * 不同提供商实现的{@link ChatOptions}参数有差异有差异
     */
    @Test
    public void chatOptionsAlibaba() {
        String prompt = "长春怎么样";
        // 创建一个ChatOptions对象
        ChatOptions chatOptions = DashScopeChatOptions.builder()
                // 使用的模型
                .withModel("qwen-max")
                // 限制 AI 生成的最大 token 数，控制响应的长度。
                .withMaxToken(1024)
                // 用于控制随机性和多样性的程度。具体来说，temperature值控制了生成文本时对每个候选词的概率分布进行平滑的程度。较高的temperature值会降低概率分布的峰值，使得更多的低概率词被选择，生成结果更加多样化；而较低的temperature值则会增强概率分布的峰值，使得高概率词更容易被选择，生成结果更加确定。 取值范围：[0, 2)，系统默认值0.85。不建议取值为0，无意义。
                .withTemperature(0.85)
                // 生成时，核采样方法的概率阈值。例如，取值为0.8时，仅保留累计概率之和大于等于0.8的概率分布中的token，作为随机采样的候选集。取值范围为（0,1.0)，取值越大，生成的随机性越高；取值越低，生成的随机性越低。默认值为0.8。注意，取值不要大于等于1
                .withTopP(0.8)
                // 生成时，采样候选集的大小。例如，取值为50时，仅将单次生成中得分最高的50个token组成随机采样的候选集。取值越大，生成的随机性越高；取值越小，生成的确定性越高。注意：如果top_k参数为空或者top_k的值大于100，表示不启用top_k策略，此时仅有top_p策略生效，默认是空。
                .withTopK(null)
                // stop参数用于实现内容生成过程的精确控制，在生成内容即将包含指定的字符串或token_ids时自动停止，生成内容不包含指定的内容。
                //例如，如果指定stop为"你好"，表示将要生成"你好"时停止；如果指定stop为[37763, 367]，表示将要生成"Observation"时停止。
                //stop参数支持以list方式传入字符串数组或者token_ids数组，支持使用多个stop的场景。
                // 说明 list模式下不支持字符串和token_ids混用，list模式下元素类型要相同。
                .withStop(null)
                // 格式化大模型的返回。可选值为 TEXT 、JSON
                .withResponseFormat(new DashScopeResponseFormat(DashScopeResponseFormat.Type.TEXT))
                // 模型内置了互联网搜索服务，该参数控制模型在生成文本时是否参考使用互联网搜索结果。取值如下：
                //true：启用互联网搜索，模型会将搜索结果作为文本生成过程中的参考信息，但模型会基于其内部逻辑"自行判断"是否使用互联网搜索结果。
                //false（默认）：关闭互联网搜索。
                .withEnableSearch(false)
                // 用于控制模型生成时的重复度。提高repetition_penalty时可以降低模型生成的重复度。1.0表示不做惩罚。默认为1.1。
                .withRepetitionPenalty(1.1)
                // 流式返回
                .withStream(false)
                // 生成时使用的随机数种子，用户控制模型生成内容的随机性。seed支持无符号64位整数。在使用seed时，模型将尽可能生成相同或相似的结果，但目前不保证每次生成的结果完全相同。
                .withSeed(null)
                // 控制在流式输出模式下是否开启增量输出，即后续输出内容是否包含已输出的内容。设置为True时，将开启增量输出模式，后面输出不会包含已经输出的内容，您需要自行拼接整体输出；设置为False则会包含已输出的内容。
                .withIncrementalOutput(true)
                .build();
        Flux<ChatResponse> flux = chatModel.stream(new Prompt(prompt, chatOptions));
        StepVerifier.create(flux)
                .thenConsumeWhile(chatResponse -> {
                    // ChatResponse [metadata={ id: 9c6ad8ea-51bc-9036-8dc1-d0009ba506e6, usage: TokenUsage[outputTokens=1, inputTokens=10, totalTokens=11], rateLimit: org.springframework.ai.chat.metadata.EmptyRateLimit@5f63a078 }, generations=[Generation[assistantMessage=AssistantMessage [messageType=ASSISTANT, toolCalls=[], textContent=长春, metadata={finishReason=NULL, id=9c6ad8ea-51bc-9036-8dc1-d0009ba506e6, role=ASSISTANT, messageType=ASSISTANT, reasoningContent=}], chatGenerationMetadata=DefaultChatGenerationMetadata[finishReason='NULL', filters=0, metadata=0]]]]
                    System.out.print(chatResponse.getResult().getOutput().getText());
                    return true;
                })
                .verifyComplete();
    }
}
