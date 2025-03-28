# 核心概念

## 一、模型（Model）

AI 模型是旨在处理和生成信息的算法，通常模仿人类的认知功能。通过从大型数据集中学习模式和见解，这些模型可以做出预测、文本、图像或其他输出，从而增强各个行业的各种应用。

有许多不同类型的人工智能模型，每种都适合特定的使用案例。虽然ChatGPT及其生成性人工智能能力通过文本输入和输出吸引了用户，但许多模型和公司提供了不同的输入和输出。在ChatGPT之前，许多人对文本到图像生成模型着迷，如中途和稳定扩散。

下表根据输入和输出类型对几个模型进行了分类：

![spring-ai-concepts-model-types](/images/O1CN01otCVsl22MbQzFKYzJ.png){v-zoom}{loading="lazy"}

像GPT这样的模型与众不同的是它们的预训练性质，如GPT-聊天生成预训练转换器中的“P”所示。这种预训练特征将人工智能转变为不需要广泛机器学习或模型训练背景的通用开发工具。

### 1.1 聊天模型(Chat Models)

聊天模型应用编程接口为开发人员提供了将人工智能驱动的聊天完成功能集成到他们的应用程序中的能力。它利用预训练语言模型，如GPT（生成预训练转换器），以自然语言对用户输入生成类似人类的响应。

API通常通过向AI模型发送提示或部分对话来工作，然后AI模型根据其训练数据和对自然语言模式的理解生成对话的完成或继续。完成的响应然后返回给应用程序，应用程序可以将其呈现给用户或用于进一步处理。

### 1.2 嵌入模型(Embedding Models)

嵌入是捕获输入之间关系的文本、图像或视频的数字表示。

嵌入通过将文本、图像和视频转换成浮点数数组来工作，称为向量。这些向量旨在捕获文本、图像和视频的含义。嵌入数组的长度称为向量的维数。

通过计算两条文本的向量表示之间的数值距离，应用程序可以确定用于生成嵌入向量的对象之间的相似性。

而EmbeddingModel接口设计用于直接接入AI和机器学习中的嵌入模型，其主要函数是将文本转换为数值向量，通常称为嵌入，这些嵌入对于语义分析和文本分类等各种任务至关重要。

### 1.3 图像模型(Image Models)

图像模型（Image Models）是用于处理、分析和生成图像的人工智能模型，通常基于深度学习技术，尤其是卷积神经网络（CNN）、视觉变换器（Vision Transformer, ViT）等架构。图像模型可以用于多种计算机视觉任务，如图像分类、目标检测、图像分割、图像生成等。

### 1.4 音频模型(Audio Models)

音频模型（Audio Models）是用于处理和分析音频数据的人工智能模型，通常基于深度学习技术，如卷积神经网络（CNN）、循环神经网络（RNN）、变换器（Transformer）等。它们可以执行语音识别、音频分类、音乐生成、声源分离等任务。

### 1.5 审核模型(Moderation Models)

审核模型（Moderation Models）是一类专门用于内容审核（Content Moderation）的人工智能模型，主要用于检测、过滤和分类不适当的文本、图像、音频或视频内容。这些模型通常被用于社交媒体、在线论坛、即时通讯应用、电子商务平台等，以确保用户生成内容（UGC）符合平台规范，避免违规内容传播。

## 二、提示（Prompt）{#prompt}

Prompt作为语言基础输入的基础，指导AI模型生成特定的输出。对于熟悉ChatGPT的人来说，Prompt似乎只是输入到对话框中的文本，然后发送到API。然而，它的内涵远不止于此。在许多AI模型中，Prompt的文本不仅仅是一个简单的字符串。

ChatGPT的API包含多个文本输入，每个文本输入都有其角色。例如，系统角色用于告知模型如何行为并设定交互的背景。还有用户角色，通常是来自用户的输入。

撰写有效的Prompt既是一门艺术，也是一门科学。ChatGPT旨在模拟人类对话，这与使用SQL“提问”有很大的区别。与AI模型的交流就像与另外一个人对话一样。

这种互动风格的重要性使得“Prompt工程”这一学科应运而生。现在有越来越多的技术被提出，以提高Prompt的有效性。投入时间去精心设计Prompt可以显著改善生成的输出。

分享Prompt已成为一种共同的实践，且正在进行积极的学术研究。例如，[最近的一篇研究论文](https://arxiv.org/abs/2205.11916)发现，最有效的Prompt之一可以以“深呼吸一下，分步进行此任务”开头。这表明语言的重要性之高。我们尚未完全了解如何充分利用这一技术的前几代版本，例如ChatGPT 3.5，更不用说正在开发的新版本了。

Prompt 是引导 AI 模型生成特定输出的输入格式，Prompt 的设计和措辞会显著影响模型的响应。

Prompt 最开始只是简单的字符串，随着时间的推移，prompt 逐渐开始包含特定的占位符，例如 AI 模型可以识别的 “USER:”、“SYSTEM:” 等。阿里云通义模型可通过将多个消息字符串分类为不同的角色，然后再由 AI 模型处理，为 prompt 引入了更多结构。每条消息都分配有特定的角色，这些角色对消息进行分类，明确 AI 模型提示的每个部分的上下文和目的。这种结构化方法增强了与 AI 沟通的细微差别和有效性，因为 prompt 的每个部分在交互中都扮演着独特且明确的角色。

Prompt 中的主要角色（Role）包括：
- 系统角色（System Role）：指导 AI 的行为和响应方式，设置 AI 如何解释和回复输入的参数或规则。这类似于在发起对话之前向 AI 提供说明。
- 用户角色（User Role）：代表用户的输入 - 他们向 AI 提出的问题、命令或陈述。这个角色至关重要，因为它构成了 AI 响应的基础。
- 助手角色（Assistant Role）：AI 对用户输入的响应。这不仅仅是一个答案或反应，它对于保持对话的流畅性至关重要。通过跟踪 AI 之前的响应（其“助手角色”消息），系统可确保连贯且上下文相关的交互。助手消息也可能包含功能工具调用请求信息。它就像 AI 中的一个特殊功能，在需要执行特定功能（例如计算、获取数据或不仅仅是说话）时使用。
- 工具/功能角色（Tool/Function Role）：工具/功能角色专注于响应工具调用助手消息返回附加信息。

## 三、提示词模板（Prompt Template）

创建有效的Prompt涉及建立请求的上下文，并用用户输入的特定值替换请求的部分内容。

这个过程使用传统的基于文本的模板引擎来进行Prompt的创建和管理。Spring AI采用开源库[StringTemplate](https://www.stringtemplate.org)来实现这一目的。

例如，考虑以下简单的Prompt模板：

```text
给我讲一个关于{content}的{adjective}笑话。
```

在Spring AI中，Prompt模板可以类比于Spring MVC架构中的“视图”。一个模型对象，通常是 `java.util.Map`，提供给Template，以填充模板中的占位符。渲染后的字符串成为传递给AI模型的Prompt的内容。

传递给模型的Prompt在具体数据格式上有相当大的变化。从最初的简单字符串开始，Prompt逐渐演变为包含多条消息的格式，其中每条消息中的每个字符串代表模型的不同角色。

## 四、嵌入（Embedding）{#embedding}

嵌入（**Embedding**）是文本、图像或视频的数值表示，能够捕捉输入之间的关系，Embedding通过将文本、图像和视频转换为称为向量（**Vector**）的浮点数数组来工作。这些向量旨在捕捉文本、图像和视频的含义，Embedding数组的长度称为向量的维度。

通过计算两个文本片段的向量表示之间的数值距离，应用程序可以确定用于生成嵌入向量的对象之间的相似性。

![spring-ai-embeddings](/images/O1CN01EnE3i61j2vin5eTGV.png){v-zoom}{loading="lazy"}

作为一名探索人工智能的Java开发者，理解这些向量表示背后的复杂数学理论或具体实现并不是必需的。对它们在人工智能系统中的作用和功能有基本的了解就足够了，尤其是在将人工智能功能集成到您的应用程序中时。

Embedding在实际应用中，特别是在检索增强生成（RAG）模式中，具有重要意义。它们使数据能够在语义空间中表示为点，这类似于欧几里得几何的二维空间，但在更高的维度中。这意味着，就像欧几里得几何中平面上的点可以根据其坐标的远近关系而接近或远离一样，在语义空间中，点的接近程度反映了意义的相似性。关于相似主题的句子在这个多维空间中的位置较近，就像图表上彼此靠近的点。这种接近性有助于文本分类、语义搜索，甚至产品推荐等任务，因为它允许人工智能根据这些点在扩展的语义空间中的“位置”来辨别和分组相关概念。

您可以将这个语义空间视为一个`向量(Vector)`。

> 例如：“续航时间”和”电池容量”会被编码为相似向量

## 五、向量存储(Vector Store)

向量存储（VectorStore）是一种用于存储和检索高维向量数据的数据库或存储解决方案，它特别适用于处理那些经过嵌入模型转化后的数据。在 VectorStore 中，查询与传统关系数据库不同。它们执行相似性搜索，而不是精确匹配。当给定一个向量作为查询时，VectorStore 返回与查询向量“相似”的向量。

VectorStore 用于将您的数据与 AI 模型集成。在使用它们时的第一步是将您的数据加载到矢量数据库中。然后，当要将用户查询发送到 AI 模型时，首先检索一组相似文档。然后，这些文档作为用户问题的上下文，并与用户的查询一起发送到 AI 模型。这种技术被称为检索增强生成（Retrieval Augmented Generation，RAG）。

## 六、Token

token是 AI 模型工作原理的基石。输入时，模型将单词转换为token。输出时，它们将token转换回单词。

在英语中，一个token大约对应一个单词的 75%。作为参考，莎士比亚的全集总共约 90 万个单词，翻译过来大约有 120 万个token。

![spring-ai-concepts-tokens](/images/O1CN01ciNztT1nJCFhQodzH.png){v-zoom}{loading="lazy"}

也许更重要的是 “token = 金钱”。在托管 AI 模型的背景下，您的费用由使用的token数量决定。输入和输出都会影响总token数量。

此外，模型还受到 token 限制，这会限制单个 API 调用中处理的文本量。此阈值通常称为“上下文窗口”。模型不会处理超出此限制的任何文本。

例如，ChatGPT3 的token限制为 4K，而 GPT4 则提供不同的选项，例如 8K、16K 和 32K。Anthropic 的 Claude AI 模型的token限制为 100K，而 Meta 的最新研究则产生了 1M token限制模型。

要使用 GPT4 总结莎士比亚全集，您需要制定软件工程策略来切分数据并在模型的上下文窗口限制内呈现数据。Spring AI 项目可以帮助您完成此任务。

## 七、结构化输出（Structured Output）

即使您要求回复为 `JSON` ，AI 模型的输出通常也会以 `java.lang.String` 的形式出现。它可能是正确的 JSON，但它可能并不是你想要的 JSON 数据结构，它只是一个字符串。此外，在提示词 Prompt 中要求 “返回JSON” 并非 100% 准确。

这种复杂性导致了一个专门领域的出现，涉及创建 Prompt 以产生预期的输出，然后将生成的简单字符串转换为可用于应用程序集成的数据结构。

![结构化输出转换器架构](/images/O1CN01lqCPAC1Xbwc1MfYv7.png){v-zoom}{loading="lazy"}

结构化输出转换采用精心设计的提示，通常需要与模型进行多次交互才能实现所需的格式。如果您想从 LLM 接收结构化输出，Structured Output 可以将返回类型从 String 更改为其他类型。

LLM 生成结构化输出的能力对于依赖可靠解析输出值的下游应用程序非常重要。开发人员希望快速将 AI 模型的结果转换为可以传递给其他应用程序函数和方法的数据类型，例如 JSON、XML 或 Java 类。结构化输出转换器有助于将 LLM 输出转换为结构化格式。

## 八、将您的数据和 API 引入 AI 模型

如何让人工智能模型与不在训练集中的数据一同工作？

请注意，GPT 3.5/4.0 数据集仅支持截止到 2021 年 9 月之前的数据。因此，该模型表示它不知道该日期之后的知识，因此它无法很好的应对需要用最新知识才能回答的问题。一个有趣的小知识是，这个数据集大约有 650GB。

有四种技术可以定制 AI 模型以整合您的数据：

- `Fine Tuning` 微调：这种传统的机器学习技术涉及定制模型并更改其内部权重。然而，即使对于机器学习专家来说，这是一个具有挑战性的过程，而且由于 GPT 等模型的大小，它极其耗费资源。此外，有些模型可能不提供此选项。
- `Prompt Stuffing` 提示词填充：一种更实用的替代方案是将您的数据嵌入到提供给模型的提示中。考虑到模型的令牌限制，我们需要具备过滤相关数据的能力，并将过滤出的数据填充到在模型交互的上下文窗口中，这种方法俗称“提示词填充”，也称为 `检索增强生成 (RAG)`实现解决方案。

![prompt-stuffing](/images/O1CN01hRUT291k1O09cdQEU.png){v-zoom}{loading="lazy"}

- `Function Calling` 函数调用：此技术允许注册自定义的用户函数，将大型语言模型连接到外部系统的 API。允许大型语言模型（LLM）在必要时调用一个或多个可用的工具，这些工具通常由开发者定义。工具可以是任何东西：网页搜索、对外部 API 的调用，或特定代码的执行等。LLM 本身不能实际调用工具；相反，它们会在响应中表达调用特定工具的意图（而不是以纯文本回应）。然后，我们应用程序应该执行这个工具，并报告工具执行的结果给模型。
- `Model Context Protocol(MCP)` [模型上下文协议](https://modelcontextprotocol.io)：MCP是一个开放协议，它规范了应用程序如何向大型语言模型（LLM）提供上下文。

### 8.1 模型微调（Fine Tuning）

- `监督微调(Supervised Fine-Tuning,SFT)`
  - **原理**:在预训练模型的基础上，使用人工标注的高质量数据集对模型进行有监督的微调。这些标注数据通常包含输入和对应的期望输出，模型通过学习这些数据来调整自身的参数，以更好地完成特定任务。应用场景:广泛应用干各种自然语言处理任务的定制化，如文本生成、机器翻译、情感分析等。例如，对于一个特定领域的文本生成任务，可以使用该领域的标汗数据对通用预训练模型进行 SFT，使其更适应该领域的语言风格和任务要求。
  - **优势**:可以快速让模型适应特定任务，提升模型在该任务上的性能。
  - **局限性**:需要大量的人工标注数据，标注成本较高;可能会出现过拟合问题，导致模型在泛化能力上有所下降。
- `直接偏好优化(Direct Preference Optimization，DPO)`
  - **原理**:DPO 是一种基于偏好学习的优化方法，通过直接优化模型的策略以最大化与人类偏好的一致性。它不需要像传统的基于强化学习的方法那样引入额外的奖励模型，而是直接从人类的偏好数据中学习。应用场景:主要用于优化模型生成的文本质量，使其更符合人类的偏好。例如，在对话系统中，通过让模型学习人类对不同回复的偏好，生成更符合用户期望的回答。
  - **优势**:避免了奖励模型的训练和优化过程，降低了训练成本和复杂度;能够更直接地优化模型以符合人类偏好。
  - **局限性**:偏好数据的收集可能存在主观性和偏差，影响型的学习效果。
- `因果预训练(Causal Pretraining，CPT)`
  - **原理**:CPT侧重于学习文本中的因果关系，在预训练过程中，模型会关注文本中事件之间的因果联系，从而在生成文本时能够更好地理解和表达因果逻辑。
  - **应用场景**:适用于需要处理因果推理的任务，如事件预测、风险评估等。例如，在金融领域，CPT 可以帮助分析市场事件之间的因果关系，预测股票价格的走势。
  - **优势**:能够增强模型的因果推理能力，使其生成的文本更具逻辑性和合理性。
  - **局限性**:因果关系的定义和识别在某些情况下较为困难，可能会影响模型的学习效果;需要专门设计的预训练任务和数据集。

### 8.2 检索增强生成（RAG）{#rag}

RAG（Retrieval Augmented Generation，检索增强生成）是一种结合信息检索和文本生成的技术范式。旨在解决为 AI 模型提供额外的知识输入，以辅助模型更好的回答问题。RAG技术就像给AI装上了「实时百科大脑」，通过**先查资料后回答**的机制，让AI摆脱传统模型的"知识遗忘"困境。

该方法涉及批处理式的编程模型，其中涉及到：从文档中读取非结构化数据、对其进行转换、然后将其写入矢量数据库。从高层次上讲，这是一个 ETL（提取、转换和加载）管道。矢量数据库则用于 RAG 技术的检索部分。

在将非结构化数据加载到矢量数据库的过程中，最重要的转换之一是将原始文档拆分成较小的部分。将原始文档拆分成较小部分的过程有两个重要步骤：

1. 将文档拆分成几部分，同时保留内容的语义边界。例如，对于包含段落和表格的文档，应避免在段落或表格中间拆分文档；对于代码，应避免在方法实现的中间拆分代码。
2. 将文档的各部分进一步拆分成大小仅为 AI 模型令牌 token 限制的一小部分的部分。


RAG 的下一个阶段是处理用户输入。当用户的问题需要由 AI 模型回答时，问题和所有“类似”的文档片段都会被放入发送给 AI 模型的提示中。这就是使用矢量数据库的原因，它非常擅长查找具有一定相似度的“类似”内容。

![Spring AI RAG](/images/O1CN01zEQSHu1sQ8KTQSA2E.png){v-zoom}{loading="lazy"}

* <a target="_blank" href="https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html">ETL 管道</a> 提供了有关协调从数据源提取数据并将其存储在结构化向量存储中的流程的更多信息，确保在将数据传递给 AI 模型时数据具有最佳的检索格式。

#### 8.2.1 四大核心步骤

**1. 文档切割 → 建立智能档案库**
- **核心任务**: 将海量文档转化为易检索的知识碎片
- **实现方式**:
  - 就像把厚重词典拆解成单词卡片
  - 采用智能分块算法保持语义连贯性
  - 给每个知识碎片打标签（如"技术规格"、"操作指南"）

> 📌 关键价值：优质的知识切割如同图书馆分类系统，决定了后续检索效率

**2. 向量编码 → 构建语义地图**
- **核心转换**:
  - 用AI模型将文字转化为数学向量
  - 使语义相近的内容产生相似数学特征
- **数据存储**:
  - 所有向量存入专用数据库
  - 建立快速检索索引（类似图书馆书目检索系统）

>🎯 示例效果："续航时间"和"电池容量"会被编码为相似向量

**3. 相似检索 → 智能资料猎人**
**应答触发流程**：
- 将用户问题转为"问题向量"
-  通过多维度匹配策略搜索知识库：
  - 语义相似度
  - 关键词匹配度
  - 时效性权重
- 输出指定个数最相关文档片段

**4. 生成增强 → 专业报告撰写**
**应答构建过程**：
- 将检索结果作为指定参考资料
- AI生成时自动关联相关知识片段。
- 输出形式可以包含：
  - 自然语言回答
  - 附参考资料溯源路径

📝 输出示例：
> "根据《产品手册v2.3》第5章内容：该设备续航时间为..."

#### 8.2.2 RAG高级特性

##### 8.2.2.1 多查询扩展(Multi Query Expansion){#multi-query-expansion}

多查询扩展是提高RAG系统检索效果的关键技术。在实际应用中，用户的查询往往是简短且不完整的，这可能导致检索结果不够准确或完整。多查询扩展机制，能够自动生成多个相关的查询变体，从而提高检索的准确性和召回率。

当用户查询 `请提供几种推荐的装修风格?` 时，系统会生成多个不同角度的查询。这种方式不仅提高了检索的全面性，还能捕获用户潜在的查询意图。

```text
扩展后的查询内容:
1. 哪些装修风格最受欢迎？请推荐一些。
2. 能否推荐一些流行的家居装修风格？
3. 想了解不同的装修风格，有哪些是值得推荐的？
```

多查询扩展的主要优势：
1. 提高召回率：通过多个查询变体，增加相关文档的检索机会
2. 覆盖不同角度：从不同维度理解和扩展用户的原始查询
3. 增强语义理解：捕获查询的多种可能含义和相关概念
4. 提升检索质量：综合多个查询结果，获得更全面的信息

##### 8.2.2.2 查询重写(Query Rewrite){#query-rewrite}

查询重写是RAG系统中的一个重要优化技术，它能够将用户的原始查询转换成更加结构化和明确的形式。这种转换可以提高检索的准确性，并帮助系统更好地理解用户的真实意图。

当用户查询 `这个房子有点贵啊`，将模糊的问题转换为具体的查询点

```text
扩展后的查询内容: 这个房子太贵，请帮我推荐一些性价比更高并且相似的房子吧
```

主要优势：
1. 查询明确化：将模糊的问题转换为具体的查询点
2. 有助于系统检索到更相关的文档，还能帮助生成更全面和专业的回答。

##### 8.2.2.3 查询翻译(Query Translation){#query-translation}

查询翻译是RAG系统中的一个实用功能，它能够将用户的查询从一种语言翻译成另一种语言。这对于多语言支持和跨语言检索特别有用。

当用户查询 `What is LLM?`，将输入语言为中文

```text
翻译后查询内容: 什么是大语言模型？
```

查询翻译的主要优势：
1. 多语言支持：支持不同语言之间的查询转换
2. 本地化处理：将查询转换为目标语言的自然表达方式
3. 跨语言检索：支持在不同语言的文档中进行检索
4. 用户友好：允许用户使用自己熟悉的语言进行查询

##### 8.2.2.4 上下文感知查询(Context-aware Queries){#context-aware-queries}

在实际对话中，用户的问题往往依赖于之前的对话上下文。下面通过一个房地产咨询的场景来说明上下文感知查询的实现：

1. 用户首先询问了碧海湾小区的位置（历史对话）
2. 系统回答了小区的具体位置信息（历史回答）
3. 用户接着问”那这个小区的二手房均价是多少?”（当前查询）

如果不考虑上下文，系统将无法理解”这个小区”具体指的是哪个小区。为了解决这个问题，我们使用上下文感知来处理上下文信息：

转换后的查询会变成更明确的形式，比如：“深圳市南山区碧海湾小区的二手房均价是多少?”。

这种转换有以下优势：
- 消除歧义：明确指定了查询目标（碧海湾小区）
- 保留上下文：包含了地理位置信息（深圳市南山区）
- 提高准确性：使系统能够更精确地检索相关信息

##### 8.2.2.5 文档合并器(DocumentJoiner){#document-joiner}

在实际应用中，我们经常需要从多个查询或多个数据源获取文档。为了有效地管理和整合这些文档，文档合并器可以将多个来源的文档智能地合并成一个统一的文档集合。

文档合并器的主要特点：
1. 智能去重：当存在重复文档时，只保留第一次出现的文档
2. 分数保持：合并过程中保持每个文档的原始相关性分数
3. 多源支持：支持同时处理来自不同查询和不同数据源的文档
4. 顺序维护：保持文档的原始检索顺序

这种合并机制在以下场景特别有用：

1. 多轮查询：需要合并多个查询返回的文档结果
2. 跨源检索：从不同的数据源（如数据库、文件系统）获取文档
3. 查询扩展：使用查询扩展生成多个相关查询时，需要合并所有结果
4. 增量更新：在现有文档集合中添加新的检索结果


##### 8.2.2.6 检索增强顾问(Retrieval Augmentation Advisor){#retrieval-augmentation-advisor}

能够自动化地处理文档检索和查询增强过程，对文档中模糊不清的内容进行增强，从而提高模型的准确度。这个顾问组件将文档检索与查询处理无缝集成，使得AI助手能够基于检索到的相关文档提供更准确的回答。

- 动态知识更新：避免传统 LLM 只能依赖静态数据的问题，可以接入实时数据源，如数据库、API、文档等。
- 增强生成质量：减少模型胡编乱造（幻觉），确保答案基于可验证的信息。
- 可定制化：可以根据业务需求，调整检索数据源，提高特定领域的适用性，如医疗、法律、金融、教育等。
- 提高可解释性：生成答案时，可以附带数据来源，提高可信度。

主要配置选项包括：

1. 查询增强器配置：
   - 上下文处理策略：定义如何处理对话历史和上下文信息，包括上下文窗口大小、历史消息权重等
   - 空值处理方式：指定当查询缺少某些参数时的处理策略，如使用默认值或抛出异常
   - 查询转换规则：设置如何将原始查询转换为更有效的检索形式，包括同义词扩展、关键词提取等
2. 文档检索器配置：
   - 相似度阈值设置：确定文档匹配的最低相似度要求，低于此阈值的文档将被过滤掉
   - 返回结果数量限制：控制每次检索返回的最大文档数量，避免返回过多不相关的结果
   - 文档过滤规则：定义基于元数据的过滤条件，如时间范围、文档类型、标签等

##### 8.2.2.7 文档选择(Document Selection){#document-selection}

文档选择是RAG系统的核心组件之一，它决定了系统能够为用户提供多么准确和相关的信息。

对于复杂的文档信息，LLM搜索起来可能不能更好的提取关键信息，这时候我们通过文档选择对文档进行增强。

优秀的文档包含两个主要部分：
1. 文档内容：结构化的文本描述，包含项目编号、概述、详细信息等

```text
案例编号：LR-2023-001
项目概述：180平米大平层现代简约风格客厅改造
设计要点：
1. 采用5.2米挑高的落地窗，最大化自然采光
2. 主色调：云雾白(哑光，NCS S0500-N)配合莫兰迪灰
3. 家具选择：意大利B&B品牌真皮沙发，北欧白橡木茶几
4. 照明设计：嵌入式筒灯搭配意大利Flos吊灯
5. 软装配饰：进口黑胡桃木电视墙，几何图案地毯
6. 空间效果：通透大气，适合商务接待和家庭日常起居
```

2. 元数据：用于快速筛选和分类的键值对，如类型、年份、位置等

```text
"type", "interior",    // 文档类型
"year", "2023",        // 年份
"month", "06",         // 月份
"location", "indoor",   // 位置类型
"style", "modern",      // 装修风格
"room", "living_room"   // 房间类型
```

当我筛选时传递元数据过滤信息
```text
b.eq("year", "2023"),         // 筛选2023年的案例
b.eq("location", "indoor")),   // 仅选择室内案例
```

优秀的搜索特性设置
1. 元数据过滤：
   - 使用FilterExpression构建复杂的过滤条件
   - 支持精确匹配（eq）、范围查询（in）等多种过滤方式
   - 可以组合多个条件（and/or）实现精确筛选
2. 相似度控制：
   - 通过similarityThreshold设置相似度阈值（0.3）
   - 使用topK限制返回结果数量（3）
   - 确保只返回最相关的文档
3. 上下文感知：
   - 集成ContextualQueryAugmenter实现上下文感知
   - 允许空上下文查询（allowEmptyContext）
   - 自动关联相关文档和查询上下文
4. 智能顾问集成：
   - 使用RetrievalAugmentationAdvisor增强查询效果
   - 自动整合文档检索和查询处理
   - 提供更智能的响应生成

通过这种多层次的文档选择机制，系统能够：
1. 快速定位相关文档
2. 准确评估文档相关性
3. 智能组合多个信息源
4. 生成高质量的回答

##### 8.2.2.8 错误处理和边界情况(Error Handling and Edge Cases){#error-handling-and-edge-cases}

在生产环境中，RAG系统需要优雅地处理各种边界情况，特别是文档检索失败或相关文档未找到的情况。我们更友好的实现错误处理机制

#### 8.2.3 最佳实践

**文档结构设计**
- **结构化内容**：文档应包含清晰的结构，如案例编号、项目概述、设计要点等
- **元数据标注**：为每个文档添加丰富的元数据，如：
```text
    "type", "interior",    // 文档类型
    "year", "2023",        // 年份
    "style", "modern"      // 风格类型
```

**文档切割策略**
- 采用智能分块算法保持语义连贯性
- 给每个知识碎片打标签
- 保持合适的文档大小，避免过长或过短

**检索增强策略**

多查询扩展
- 启用多查询扩展机制，提高检索准确性
- 设置合适的查询数量（建议3-5个）
- 保留原始查询的核心语义

查询重写和翻译
- 使用`RewriteQueryTransformer`优化查询结构
- 配置`TranslationQueryTransformer`支持多语言
- 保持查询的语义完整性

**系统配置最佳实践**

向量存储配置
- 选择合适的向量存储方案
- 根据数据规模选择存储方式（内存/Redis/MongoDB）

检索器配置
```java
DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
    .vectorStore(vectorStore)
    .similarityThreshold(0.5)    // 相似度阈值
    .topK(3)                     // 返回文档数量
    .build();
```
- 设置合理的相似度阈值
- 控制返回文档数量
- 配置文档过滤规则

**错误处理机制**

异常处理
- 允许空上下文查询
- 提供友好的错误提示
- 引导用户提供必要信息

边界情况处理

- 处理文档未找到情况
- 处理相似度过低情况
- 处理查询超时情况

**系统角色设定**

AI助手配置
```text
你是一位专业的顾问，请注意
1. 准确理解用户需求
2. 结合参考资料
3. 提供专业解释
4. 考虑实用性
5. 提供替代方案
```
- 设定清晰的角色定位
- 定义回答规范
- 确保专业性和实用性

**性能优化建议**

查询优化
- 使用文档过滤表达式
- 设置合理的检索阈值
- 优化查询扩展数量

资源管理
- 控制文档加载数量
- 优化内存使用
- 合理设置缓存策略

通过遵循以上最佳实践，可以构建一个高效、可靠的RAG系统，为用户提供准确和专业的回答。这些实践涵盖了从文档处理到系统配置的各个方面，能够帮助开发者构建更好的RAG应用。

### 8.3 函数调用（Function Calling）

大型语言模型 (LLM) 在训练后即被冻结，导致知识陈旧，并且无法访问或修改外部数据。

Function Calling机制解决了这些缺点，它允许您注册自己的函数，以将大型语言模型连接到外部系统的 API。这些系统可以为 LLM 提供实时数据并代表它们执行数据处理操作。

![Spring AI Function Calling](/images/O1CN01kiQh6L1hnWmm5gCAW.png){v-zoom}{loading="lazy"}

* （1）执行聊天请求并发送函数定义信息。后者提供`name`（`description`例如，解释模型何时应调用该函数）和`input parameters`（例如，函数的输入参数模式）。
* （2）当模型决定调用该函数时，它将使用输入参数调用该函数，并将输出返回给模型。
* （3）处理此对话。它将函数调用分派给适当的函数，并将结果返回给模型。
* （4）模型可以执行多个函数调用来检索所需的所有信息。
* （5）一旦获取了所有需要的信息，模型就会生成响应。

如下是一个简单的函数调用示例，录入用户的时候需要获取用户的姓名和年龄，我们定义一个 `function addUser(String name, Integer age)`
> 问：录入用户
> 答：请提供一下你的姓名和年龄信息吧
> 问：张三 25
> function：这时候大模型会调用addUser(String name, Integer age)函数并传入参数 name = "张三"，age = 25

请关注不同大模型中的函数调用文档以获取有关如何在不同 AI 模型中使用此功能的更多信息。

### 8.4 模型上下文协议(Model Context Protocol)

[模型上下文协议](https://modelcontextprotocol.io)：MCP是一个开放协议，它规范了应用程序如何向大型语言模型（LLM）提供上下文。MCP 提供了一种统一的方式将 AI 模型连接到不同的数据源和工具，它定义了统一的集成方式。在开发智能体（Agent）的过程中，我们经常需要将将智能体与数据和工具集成，MCP 以标准的方式规范了智能体与数据及工具的集成方式，可以帮助您在LLM之上构建智能体（Agent）和复杂的工作流。

在其核心，MCP遵循客户端-服务器架构，其中主机应用程序可以连接到多个服务器：

![mcp.png](/images/dsadsdadwdwqsadsdas.png){v-zoom}{loading="lazy"}

![java-mcp.png](/images/jdioasjfwqfncasdwasf.png){v-zoom}{loading="lazy"}

- **MCP主机**：希望通过MCP访问数据的Claude Desktop、IDE或AI工具等程序
- **MCP客户端**：与服务器保持1:1连接的协议客户端
- **MCP服务器**：通过标准化模型上下文协议公开特定功能的轻量级程序
- **本地数据源**：MCP服务器可以安全访问的计算机文件、数据库和服务
- **远程服务**：通过公网提供的外部系统（例如，通过API），MCP服务器可以连接到

目前已经有大量的服务接入并提供了 MCP server 实现，当前这个生态正在以非常快的速度不断的丰富中，具体可参见：[MCP Servers](https://github.com/modelcontextprotocol/servers)。

这个相当牛逼，可以让你的LLM不仅能回复一个string，而是能操作各种东西。

## 九、对话记忆(Chat Memory)

”大模型的对话记忆”这一概念，根植于人工智能与自然语言处理领域，特别是针对具有深度学习能力的大型语言模型而言，它指的是模型在与用户进行交互式对话过程中，能够追踪、理解并利用先前对话上下文的能力。 此机制使得大模型不仅能够响应即时的输入请求，还能基于之前的交流内容能够在对话中记住先前的对话内容，并根据这些信息进行后续的响应。这种记忆机制使得模型能够在对话中持续跟踪和理解用户的意图和上下文，从而实现更自然和连贯的对话。

大模型不会记录用户的上下文数据，所有如果我们要实现连贯的对话，就需要维护对话记录，请求的大模型的时候，需要将上下文数据传递给模型，模型根据上下文数据生成回答。

> 例如：  
> 问：我想去长春旅行  
> 答：长春是个牛逼的地方，快来吧。  
> 问：帮我推荐一下当地美食  
> 这个时候如果没有对话记忆功能，大模型就不知道推荐长春的美食，所以我们将对话记录传入，大模型就知道推荐的是长春的当地美食

## 十、评估人工智能的回答（Evaluation）

有效评估人工智能系统回答的正确性，对于确保最终应用程序的准确性和实用性非常重要，一些新兴技术使得预训练模型本身能够用于此目的。

Evaluation 评估过程涉及分析响应是否符合用户的意图、与查询的上下文强相关，一些指标如相关性、连贯性和事实正确性等都被用于衡量 AI 生成的响应的质量。

一种方法是把用户的请求、模型的响应一同作为输入给到模型服务，对比模型给的响应或回答是否与提供的响应数据一致。

此外，利用矢量数据库（Vector Database）中存储的信息作为补充数据可以增强评估过程，有助于确定响应的相关性。