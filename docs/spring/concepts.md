
# Spring Ai

::: tip 注意
- 了解Spring AI之前，您需要已经了解[AI核心概念](../guide/concepts.md)。
- 此文章介绍Spring AI的对AI开发的抽象定义，让开发者对SpringAI的核心功能有个大体的了解。
  :::
## 一、什么是 Spring AI？

[Spring AI](https://github.com/spring-projects/spring-ai)是AI工程的应用框架，其目标是将Spring生态系统设计原则（如可移植性和模块化设计）应用于AI领域，并将POJO作为应用程序的构建块推广到AI领域。

该项目从著名的Python项目中汲取灵感，如 [LangChain](https://github.com/langchain-ai)和[LlamaIndex](https://github.com/run-llama/llama_index)，但Spring AI不是这些项目的直接复制。该项目的创建是基于这样一种信念，即下一波生成性AI应用程序不仅面向Python开发人员，而且将在许多编程语言中无处不在。

langchain不只有python也有很多其他语言的项目
- Java：[LangChain4J](https://github.com/langchain4j/langchain4j)
- Js：[LangChainJs](https://github.com/langchain-ai/langchainjs)
- Go：[LangChainGo](https://github.com/tmc/langchaingo)
- 等等

Spring AI项目旨在简化应用程序的开发，这些应用程序包含人工智能功能，而没有不必要的复杂性。Spring AI提供了作为开发AI应用程序基础的抽象，这些抽象具有多种实现，能够以最少的代码更改轻松地进行组件交换。

![dsadasdwqfvdsds.png](/images/dsadasdwqfvdsds.png){v-zoom}{loading="lazy"}

## 二、Spring 对 AI 的抽象

### 2.1 聊天模型(Chat Models)

聊天模型应用编程接口为开发人员提供了将人工智能驱动的聊天完成功能集成到他们的应用程序中的能力。它利用预训练语言模型，如GPT（生成预训练转换器），以自然语言对用户输入生成类似人类的响应。

API通常通过向AI模型发送提示或部分对话来工作，然后AI模型根据其训练数据和对自然语言模式的理解生成对话的完成或继续。完成的响应然后返回给应用程序，应用程序可以将其呈现给用户或用于进一步处理。

[SpringAi聊天模型使用教程](chat-model)