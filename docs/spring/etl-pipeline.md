# ETL管道(etl-pipeline)

## 概述

ETL(Extract 抽取, Transform 转换, Load 加载) 是检索增强生成（RAG）使用案例中数据处理的主干。

ETL流水线编排从原始数据源到结构化向量存储的流程，确保数据处于AI模型检索的最佳格式。

`Document` 包含文本、元信息以及可选的所有媒体类型，如图像、音频和视频。

![img.png](/images/hwidhajdosadwdwqfwa.png){v-zoom}{loading="lazy"}

抽取、转换、加载流水线有三个主要组成部分，

- [DocumentReader](#document-reader)，实现 `Supplier<List<Document>>` 。文档读取。
- [DocumentTransformer](#document-transformer)，实现 `Function<List<Document>, List<Document>>` 。文档转换。
- [DocumentWriter](#document-writer)，实现 `Consumer<List<Document>>` 。文档写入。

要构建简单的抽取、转换、加载流水线，您可以将每种类型的实例链接在一起。

![hiwfuqadjsdas.png](/images/hiwfuqadjsdas.png){v-zoom}{loading="lazy"}

类图如下:

![hwifnasofkasfsadsa.png](/images/hwifnasofkasfsadsa.png){v-zoom}{loading="lazy"}

### 文档读取(DocumentReader){#document-reader}

将不同来源的文档读取为`Document`。

```java
public interface DocumentReader extends Supplier<List<Document>> {

    default List<Document> read() {
		return get();
	}
}
```

Spring AI 内置了文档读取器，如：
- [JsonReader](#json-reader)：处理JSON文档，将它们转换为Document对象列表。
- [TextReader](#text-reader)：处理文本文档，将它们转换为Document对象列表。
- [MarkdownDocumentReader](#markdown-document-reader)：处理Markdown文档，将它们转换为Document对象列表。
- [PagePdfDocumentReader](#page-pdf-document-reader)：使用 `Apache PdfBox` 库解析PDF文档。
- [ParagraphPdfDocumentReader](#paragraph-pdf-document-reader)：使用PDF目录（例如TOC）信息将输入的PDF拆分成文本段落，并为每个段落输出一个单独的文档。注意：并非所有的PDF文档都包含PDF目录。
- [TikaDocumentReader](#tika-document-reader)：使用Apache Tika从各种文档格式中提取文本，如PDF、DOC/DOCX、PPT/PPTX和超文本标记语言。有关支持格式的全面列表，详情可见[Tika](https://tika.apache.org/2.9.0/formats.html)文档。

Spring AI Alibaba 社区提供了很多文档读取器，[源码](https://github.com/alibaba/spring-ai-alibaba/tree/main/community/document-readers)。

#### JsonReader{#json-reader}

处理JSON文档，将它们转换为Document对象列表。

构造函数:

| 参数                    | 含义                                       |
|-----------------------|------------------------------------------|
| resource              | 指向JSON文件的SpringResource对象。               |
| jsonKeysToUse         | JSON中的键数组，可用作结果Document对象中的文本内容。         |
| jsonMetadataGenerator | 可选JsonMetadataGenerator为每个Document创建元信息。 |

指针：获取对应位置的json数据转为 Document 对象。

```java
// JSON指针字符串（在RFC 6901中定义），用于在JSON结构中定位所需元素。
public List<Document> get(String pointer);
```

示例：

**bikes.json**

```json
[
  {
    "id": 1,
    "brand": "Trek",
    "description": "A high-performance mountain bike for trail riding."
  },
  {
    "id": 2,
    "brand": "Cannondale",
    "description": "An aerodynamic road bike for racing enthusiasts."
  }
]
```

**JsonReaderTest.java**

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.JsonReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * @author future0923
 */
public class JsonReaderTest extends EtlPipelineApplicationTest {

    @Value("classpath:bikes.json")
    private Resource resource;

    @Test
    public void readJson() {
        // resource 资源文件
        // jsonKeysToUse JSON中的键数组，可用作结果Document对象中的文本内容。
        // jsonMetadataGenerator 可选JsonMetadataGenerator为每个Document创建元信息。
        JsonReader jsonReader = new JsonReader(this.resource, "description", "content");
        List<Document> documents = jsonReader.get();
        documents.forEach(System.out::println);
        // 基于上面提取之后的数据继续通过 pointer 获取
        List<Document> documentList = jsonReader.get("/0");
        documentList.forEach(System.out::println);
    }
}
```

#### TextReader{#text-reader}

处理文本文档，将它们转换为Document对象列表。

构造函数：

| 参数          | 含义                       |
|-------------|--------------------------|
| resourceUrl | 表示要读取的资源的URL的字符串。        |
| resource    | 指向文本文件的SpringResource对象。 |

配置：

| 参数                | 含义                          |
|-------------------|-----------------------------|
| setCharset        | 设置用于读取文本文件的字符集。默认为UTF-8。    |
| getCustomMetadata | 返回一个可变映射，您可以在其中为文档添加自定义元信息。 |

示例：

**text-source.txt**

```text
Spring AI是AI工程的应用框架，其目标是将Spring生态系统设计原则（如可移植性和模块化设计）应用于AI领域，并将POJO作为应用程序的构建块推广到AI领域。

该项目从著名的Python项目中汲取灵感，如 LangChain和LlamaIndex，但Spring AI不是这些项目的直接复制。该项目的创建是基于这样一种信念，即下一波生成性AI应用程序不仅面向Python开发人员，而且将在许多编程语言中无处不在。
```

**TextReaderTest.java**

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * @author future0923
 */
public class TextReaderTest extends EtlPipelineApplicationTest {

    @Value("classpath:text-source.txt")
    private Resource resource;

    @Test
    public void readText() {
        // 创建 TextReader
        TextReader textReader = new TextReader(this.resource);
        // 设置元数据
        textReader.getCustomMetadata().put("filename", "text-source.txt");
        // 读取文档
        List<Document> documents = textReader.read();
        // 切割文档
        List<Document> splitter = new TokenTextSplitter().apply(documents);
        splitter.forEach(System.out::println);
    }
}
```

#### MarkdownDocumentReader{#markdown-document-reader}

处理Markdown文档，将它们转换为Document对象列表。

需要引入依赖：
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-markdown-document-reader</artifactId>
</dependency>
```

使用 MarkdownDocumentReaderConfig 配置 Markdown 的读取.

| 参数 | 含义 |
|----|----|
| horizontalRuleCreateDocument   |  当设置为true时，Markdown中的水平规则将创建新的Document对象。  |
|  includeCodeBlock  |  当设置为true时，代码块将包含在与周围文本相同的Document中。当false时，代码块创建单独的Document对象。  |
|  includeBlockquote  |   当设置为true时，块引用将包含在与周围文本相同的Document中。当false时，块引用创建单独的Document对象。 |
|  additionalMetadata  |  允许您向所有创建的Document对象添加自定义元信息。  |
|    |    |

行为：MarkdownDocumentReader处理Markdown内容，并根据以下配置创建Document对象：
- 标题成为Document对象中的元信息。
- 段落成为Document对象的内容。
- 代码块可以分离为它们自己的Document对象或包含在周围的文本中。
- 块引号可以分离为它们自己的Document对象或包含在周围的文本中。
- 水平规则可用于将内容拆分为单独的Document对象。

::: details code.md
<pre>
This is a Java sample application:

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

Markdown also provides the possibility to `use inline code formatting throughout` the entire sentence.

---

Another possibility is to set block code without specific highlighting:

```
./mvnw spring-javaformat:apply
```
</pre>

:::

**MarkdownDocumentReaderTest.java**

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * @author future0923
 */
public class MarkdownDocumentReaderTest extends EtlPipelineApplicationTest {

    @Value("classpath:code.md")
    private Resource resource;

    @Test
    public void readText() {
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                // 当设置为true时，Markdown中的水平规则将创建新的Document对象。
                .withHorizontalRuleCreateDocument(true)
                // 当设置为true时，代码块将包含在与周围文本相同的Document中。当false时，代码块创建单独的Document对象。
                .withIncludeCodeBlock(false)
                // 当设置为true时，块引用将包含在与周围文本相同的Document中。当false时，块引用创建单独的Document对象。
                .withIncludeBlockquote(false)
                // 允许您向所有创建的Document对象添加自定义元信息。
                .withAdditionalMetadata("filename", "code.md")
                .build();
        MarkdownDocumentReader reader = new MarkdownDocumentReader(this.resource, config);
        List<Document> documents = reader.get();
        documents.forEach(System.out::println);
    }
}
```

#### PagePdfDocumentReader{#page-pdf-document-reader}

使用 `Apache PdfBox` 库解析PDF文档。

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-pdf-document-reader</artifactId>
</dependency>
```

例子

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * @author future0923
 */
public class PagePdfDocumentReaderTest extends EtlPipelineApplicationTest {

    @Value("classpath:sample.pdf")
    private Resource resource;

    @Test
    public void readText() {
        PagePdfDocumentReader pdfDocumentReader = new PagePdfDocumentReader(
                resource,
                PdfDocumentReaderConfig.builder()
                        //  设置 页面顶部边距（0，表示不留边）
                        .withPageTopMargin(0)
                        //  设置 页面底部边距（0，表示不留边）
                        .withPageBottomMargin(0)
                        // 配置 文本提取格式：
                        .withPageExtractedTextFormatter(
                                ExtractedTextFormatter.builder()
                                        // 不删除任何顶部文本行。
                                        .withNumberOfTopTextLinesToDelete(0)
                                        // 还有很多配置
                                        .build())
                        // 每个 Document 只包含 1 页。
                        .withPagesPerDocument(1)
                        .build());
        List<Document> documents = pdfDocumentReader.read();
        documents.forEach(System.out::println);
    }
}
```

#### ParagraphPdfDocumentReader{#paragraph-pdf-document-reader}

使用PDF目录（例如TOC）信息将输入的PDF拆分成文本段落，并为每个段落输出一个单独的文档。注意：并非所有的PDF文档都包含PDF目录。

与上面 PagePdfDocumentReader 使用一致。

#### TikaDocumentReader{#tika-document-reader}

使用Apache Tika从各种文档格式中提取文本，如PDF、DOC/DOCX、PPT/PPTX和超文本标记语言。有关支持格式的全面列表，详情可见[Tika](https://tika.apache.org/2.9.0/formats.html)文档。

maven

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-tika-document-reader</artifactId>
</dependency>
```

示例

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * @author future0923
 */
public class TikaDocumentReaderTest extends EtlPipelineApplicationTest {

    @Value("classpath:sample.docx")
    private Resource resource;

    @Test
    public void readText() {
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(this.resource);
        List<Document> documents = tikaDocumentReader.read();
        documents.forEach(System.out::println);
    }
}
```

### 文档转换(DocumentTransformer){#document-transformer}

对 `Document` 进行转换。

```java
public interface DocumentTransformer extends Function<List<Document>, List<Document>> {

    default List<Document> transform(List<Document> transform) {
		return apply(transform);
	}
}
```

Spring AI 内置了文档转换器，如：
- [TokenTextSplitter](#token-text-splitter)：是一个TextSplitter的实现，它使用CL100K_BASE编码根据Token计数将文本分割成块。
- [ContentFormatTransformer](#content-format-transformer): 确保所有文档的内容格式统一。
- [KeywordMetadataEnricher](#keyword-metadata-enricher)：使用生成式人工智能模型从文档内容中提取关键词并将其添加为元数据。
- [SummaryMetadataEnricher](#summary-metadata-enricher)：使用生成式人工智能模型为文档生成摘要并将其作为元数据添加。它可以为当前文档以及相邻文档（前一篇和下一篇）生成摘要。

Spring AI Alibaba 社区提供了很多文档转换器，[源码](https://github.com/alibaba/spring-ai-alibaba/tree/main/community/document-parsers)。

#### TokenTextSplitter{#token-text-splitter}

使用CL100K_BASE编码根据Token计数将文本分割成块。

**构造参数：**

| 参数                    | 含义                  | 默认值   |
|-----------------------|---------------------|:------|
| defaultChunkSize      | 标记中每个文本块的目标大小       | 800   |
| minChunkSizeChars     | 每个文本块的极小点大小（以字符为单位） | 350   |
| minChunkLengthToEmbed | 要包含的块的极小点长度         | 5     |
| maxNumChunks          | 从文本生成的最大块数          | 10000 |
| keepSeparator         | 是否在块中保留分隔符（如换行符）    | true  |

**该TokenTextSplitter处理文本内容如下：**
1. 它使用CL100K_BASE编码将输入文本编码为标记。
2. 它根据defaultChunkSize将编码文本拆分为对应大小的块。
3. 对于每个块：
   - 它将块解码回文本。
   - 它试图在minchunkSizeChars之后找到一个合适的minChunkSizeChars。
   - 如果找到断点，它会在该点截断块。
   - 它修剪块，并根据keepSeparator设置删除换行符。
   - 如果生成的块长于minChunkLengthToEmbed，则将其添加到输出中。
4. 此过程一直持续到处理完所有令牌或到达maxNumChunks。
5. 如果任何剩余文本的长度超过minChunkLengthToEmbed，则将其添加为最终块。

**例子：**

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;

import java.util.List;
import java.util.Map;

/**
 * @author future0923
 */
public class TokenTextSplitterTest extends EtlPipelineApplicationTest {

    @Test
    public void test() {
        Document doc1 = new Document("This is a long piece of text that needs to be split into smaller chunks for processing.",
                Map.of("source", "example.txt"));
        Document doc2 = new Document("Another document with content that will be split based on token count.",
                Map.of("source", "example2.txt"));
       TokenTextSplitter splitter = TokenTextSplitter.builder()
               // 标记中每个文本块的目标大小
               .withChunkSize(800)
               // 每个文本块的极小点大小（以字符为单位）
               .withMinChunkSizeChars(350)
               // 要包含的块的极小点长度
               .withMinChunkLengthToEmbed(5)
               // 从文本生成的最大块数
               .withMaxNumChunks(10000)
               // 是否在块中保留分隔符（如换行符）
               .withKeepSeparator(true)
               .build();
        List<Document> splitDocuments = splitter.apply(List.of(doc1, doc2));
        for (Document doc : splitDocuments) {
            System.out.println("Chunk: " + doc.getText());
            System.out.println("Metadata: " + doc.getMetadata());
        }
    }
}
```

#### ContentFormatTransformer{#content-format-transformer}

确保所有文档的内容格式统一。

```java
// DefaultContentFormatter 配置格式化内容 
ContentFormatTransformer transformer = new ContentFormatTransformer(DefaultContentFormatter.defaultConfig());
// splitDocuments 在上面
List<Document> documentList = transformer.apply(splitDocuments);
```

#### KeywordMetadataEnricher{#keyword-metadata-enricher}

使用生成式人工智能模型从文档内容中提取关键词并将其添加为元数据。

构造函数：

| 参数           | 含义            |
|--------------|---------------|
| chatModel    | 用于生成关键字的AI模型。 |
| keywordCount | 为每个文档提取的关键字数。 |


该KeywordMetadataEnricher处理文件如下：
1. 对于每个输入文档，它使用文档的内容创建一个提示。
2. 它将此提示发送到提供的ChatModel以生成关键字。
3. 生成的关键字被添加到文档的元信息中，在关键字“excerpt_keywords”下。
4. 丰富的文档被返回。

示例：

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 使用生成式人工智能模型从文档内容中提取关键词并将其添加为元数据
 *
 * @author future0923
 */
public class KeywordMetadataEnricherTest extends EtlPipelineApplicationTest {

   @Autowired
   private ChatModel chatModel;

   @Test
   public void test() {
      KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(
              // 聊天模型
              chatModel,
              // 为每个文档提取的关键字数。
              5);
      // 文档内容
      Document doc = new Document("This is a document about artificial intelligence and its applications in modern technology.");
      // 提取关键字
      List<Document> enrichedDocs = enricher.apply(List.of(doc));
      Document enrichedDoc = enrichedDocs.get(0);
      // 查看返回的内容
      String keywords = (String) enrichedDoc.getMetadata().get("excerpt_keywords");
      // Extracted keywords: artificial intelligence, machine learning, automation, data analysis, intelligent systems
      System.out.println("Extracted keywords: " + keywords);
   }

}
```

#### SummaryMetadataEnricher{#summary-metadata-enricher}

使用生成式人工智能模型为文档生成摘要并将其作为元数据添加。它可以为当前文档以及相邻文档（前一篇和下一篇）生成摘要。

| 参数              | 含义                                     |
|-----------------|----------------------------------------|
| chatModel       | 用于生成摘要的AI模型。                           |
| summaryTypes    | SummaryType值的列表，指示要生成哪些摘要（上一个、当前、下一个）。 |
| summaryTemplate | 用于摘要生成的自定义模板（可选）。                      |
| metadataMode    | 指定生成摘要时如何处理文档元信息（可选）。                  |

该SummaryMetadataEnricher处理文件如下：
1. 对于每个输入文档，它使用文档的内容和指定的摘要模板创建一个提示。
2. 它将此提示发送到提供的ChatModel以生成摘要。
3. 根据指定的summaryTypes，它向每个文档添加以下元信息：
   - `section_summary`：当前文件的摘要。
   - `prev_section_summary`：以前文件的摘要（如果有和要求）。
   - `next_section_summary`：下一份文件的摘要（如果有和要求）。
4. 丰富的文档被返回。

**例子：**

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.SummaryMetadataEnricher;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 使用生成式人工智能模型为文档生成摘要并将其作为元数据添加。它可以为当前文档以及相邻文档（前一篇和下一篇）生成摘要。
 *
 * @author future0923
 */
public class SummaryMetadataEnricherTest extends EtlPipelineApplicationTest {

    @Autowired
    private ChatModel chatModel;

    @Test
    public void test() {
        SummaryMetadataEnricher enricher = new SummaryMetadataEnricher(
                // 用于生成摘要的AI模型
                chatModel,
                // SummaryType值的列表，指示要生成哪些摘要（上一个、当前、下一个）
                List.of(SummaryMetadataEnricher.SummaryType.PREVIOUS, SummaryMetadataEnricher.SummaryType.CURRENT, SummaryMetadataEnricher.SummaryType.NEXT),
                // 用于摘要生成的自定义模板（可选）。
                null,
                // 指定生成摘要时如何处理文档元信息（可选）。
                null
        );
        Document doc1 = new Document("Content of document 1");
        Document doc2 = new Document("Content of document 2");
        List<Document> enrichedDocs = enricher.apply(List.of(doc1, doc2));
        for (Document doc : enrichedDocs) {
            System.out.println("当前文件的摘要: " + doc.getMetadata().get("section_summary"));
            System.out.println("上一份文件的摘要: " + doc.getMetadata().get("prev_section_summary"));
            System.out.println("下一份文件的摘要: " + doc.getMetadata().get("next_section_summary"));
        }
        // 当前文件的摘要: I'm happy to help summarize the key topics and entities, but I need the actual content of the section to do so. Could you please provide the text or details from "Content of document 1"?
        // 上一份文件的摘要: null
        // 下一份文件的摘要: I apologize, but you've mentioned "Content of document 2" without providing the actual content. Could you please share the text or details from the section so that I can summarize the key topics and entities for you?
        // 当前文件的摘要: I apologize, but you've mentioned "Content of document 2" without providing the actual content. Could you please share the text or details from the section so that I can summarize the key topics and entities for you?
        // 上一份文件的摘要: I'm happy to help summarize the key topics and entities, but I need the actual content of the section to do so. Could you please provide the text or details from "Content of document 1"?
        // 下一份文件的摘要: null
    }
}
```

### 文档写入(DocumentWriter){#document-writer}

将`Document`写入到目标存储中，一般为[向量数据库](vector-store)。

```java
public interface DocumentWriter extends Consumer<List<Document>> {

    default void write(List<Document> documents) {
		accept(documents);
	}
}
```

Spring AI 内置了文档写入，如：
- `FileDocumentWriter`：它将Document对象列表的内容写入文件。
- [Vector Store](vector-store)：将文档写入向量数据库。

#### FileDocumentWriter

它将Document对象列表的内容写入文件。

构造函数：

| 参数                  | 含义                                    |
|---------------------|---------------------------------------|
| fileName            | 要写入文档的文件的名称。                          |
| withDocumentMarkers | 是否在输出中包含文档标记（默认值：false）。              |
| metadataMode        | 指定要写入文件的文档内容（默认值：MetadataMode.NONE）。 |
| append              | 如果为true，数据将写入文件的末尾而不是开头（默认值：false）。   |

MetadataMode参数：

| 取值        | 含义                                        | 场景                                                     |
|-----------|-------------------------------------------|:-------------------------------------------------------|
| ALL       | 该模式表示所有元数据都会被存储，包括用户提供的元数据和从文档内容中推断出的元数据。 | 适用于需要完整信息以便后续分析或搜索的场景。                                 |
| EMBED     | 该模式表示元数据会被嵌入到文件或文档的内容中，而不是单独存储。           | 适用于希望元数据与文档一起传输或处理的情况，例如在 JSON、PDF、Markdown 等格式中嵌入元数据。 |
| INFERENCE | 该模式表示系统会根据文档内容自动推断元数据，而不会存储用户提供的元数据。      | 该模式表示不会存储任何元数据，既不保存用户提供的元数据，也不进行推理。                    |
| NONE      | 该模式表示不会存储任何元数据，既不保存用户提供的元数据，也不进行推理。       | 适用于对元数据不关心或希望减少存储空间的场景。                                |


如下方式处理文档：
1. 它为指定的文件名打开一个FileWriter。
2. 对于输入列表中的每个文档：
   - 如果withDocumentMarkers为真，则写入包含文档索引和页码的文档标记。
   - 它根据指定的metadataMode写入文档的格式化内容。
3. 写入所有文档后，该文件将关闭。

当withDocumentMarkers设置为true时，编写器以以下格式包含每个文档的标记：

```text
### Doc: [index], pages:[start_page_number,end_page_number]
```

**元信息处理**：使用两个特定的元信息键
- `start_page_number`：表示文档的起始页码。
- `end_page_number`：表示文档的关播页号。

这些在编写文档标记时使用。

```java
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.writer.FileDocumentWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * @author future0923
 */
public class FileDocumentWriterTest extends EtlPipelineApplicationTest {

    @Value("classpath:text-source.txt")
    private Resource resource;

    @Test
    public void test() {
        // 创建 TextReader
        TextReader textReader = new TextReader(this.resource);
        // 设置元数据
        textReader.getCustomMetadata().put("filename", "text-source.txt");
        // 读取文档
        List<Document> documents = textReader.read();
        // 切割文档
        List<Document> splitter = new TokenTextSplitter().apply(documents);
        FileDocumentWriter fileDocumentWriter = new FileDocumentWriter(
                // 要写入文档的文件的名称。
                "output.txt",
                // 是否在输出中包含文档标记（默认值：false）
                true,
                // 指定要写入文件的文档内容（默认值：MetadataMode. NONE）。
                // ALL（全部）
                //  该模式表示所有元数据都会被存储，包括用户提供的元数据和从文档内容中推断出的元数据。
                //  适用于需要完整信息以便后续分析或搜索的场景。
                // EMBED（嵌入）
                //  该模式表示元数据会被嵌入到文件或文档的内容中，而不是单独存储。
                //  适用于希望元数据与文档一起传输或处理的情况，例如在 JSON、PDF、Markdown 等格式中嵌入元数据。
                // INFERENCE（推理）
                //  该模式表示系统会根据文档内容自动推断元数据，而不会存储用户提供的元数据。
                //  适用于希望利用 AI 或 NLP 技术自动生成标签、分类、关键词等信息的场景，而不依赖手动提供的元数据。
                // NONE（无）
                //  该模式表示不会存储任何元数据，既不保存用户提供的元数据，也不进行推理。
                //  适用于对元数据不关心或希望减少存储空间的场景。
                MetadataMode.ALL,
                // 如果为true，数据将写入文件的末尾而不是开头（默认值：false）
                false
        );
        fileDocumentWriter.write(splitter);
    }
}
```

#### VectorStore

写入[向量数据库](vector-store)。