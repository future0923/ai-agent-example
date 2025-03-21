
## 项目环境

1. Jdk 17+
2. SpringBoot 3.4.3
3. SpringAI 1.0.0-M6
4. SpringAi Alibaba 1.0.0-M6.1

`spring ai` 基于 `springboot 3.+` 开发，所以要求 `JDK 17+`。

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>3.4.3</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>1.0.0-M6</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud.ai</groupId>
            <artifactId>spring-ai-alibaba-starter</artifactId>
            <version>1.0.0-M6.1</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Spring AI 目前没有发布正式版本，中央仓库没有，需要添加仓库配置

```xml
<repositories>
 <repository>
  <id>spring-milestones</id>
  <name>Spring Milestones</name>
  <url>https://repo.spring.io/milestone</url>
  <snapshots>
   <enabled>false</enabled>
  </snapshots>
 </repository>
</repositories>
```

补充：如果您的本地 maven settings.xml 中的 mirrorOf 标签配置了通配符 * ，请根据以下示例修改排除spring-milestones。

```xml
<mirror>
  <id>xxxx</id>
  <mirrorOf>*,!spring-milestones</mirrorOf>
  <name>xxxx</name>
  <url>xxxx</url>
</mirror>
```