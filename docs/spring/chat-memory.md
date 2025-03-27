# 聊天记忆(Chat Memory)

演示[代码](https://github.com/future0923/ai-agent-example/tree/main/java/chat-memory)

”大模型的对话记忆”这一概念，根植于人工智能与自然语言处理领域，特别是针对具有深度学习能力的大型语言模型而言，它指的是模型在与用户进行交互式对话过程中，能够追踪、理解并利用先前对话上下文的能力。 此机制使得大模型不仅能够响应即时的输入请求，还能基于之前的交流内容能够在对话中记住先前的对话内容，并根据这些信息进行后续的响应。这种记忆机制使得模型能够在对话中持续跟踪和理解用户的意图和上下文，从而实现更自然和连贯的对话。

大模型理解并利用先前对话上下文的能力两种
- 通过[上下文感知查询](#context-aware-queries)方式得出这次问题的真正含义。
- 使用 `聊天记忆` 功能将对话的上下文都传给大模型，大模型能够理解并利用这些信息，从而实现更自然和连贯的对话。

## API

```java
package org.springframework.ai.chat.memory;

import java.util.List;

import org.springframework.ai.chat.messages.Message;

public interface ChatMemory {

    // 给指定的conversationId添加消息
	default void add(String conversationId, Message message) {
		this.add(conversationId, List.of(message));
	}

    // 给指定的conversationId添加消息
	void add(String conversationId, List<Message> messages);

    // 根据conversationId获取指定数量的消息
	List<Message> get(String conversationId, int lastN);

    // 根据conversationId清除消息
	void clear(String conversationId);

}
```

## 实现

### 内存(InMemoryChatMemory) 

通过 `Map<String, List<Message>>` 实现

```java
package org.springframework.ai.chat.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.ai.chat.messages.Message;

public class InMemoryChatMemory implements ChatMemory {

	Map<String, List<Message>> conversationHistory = new ConcurrentHashMap<>();

	@Override
	public void add(String conversationId, List<Message> messages) {
		this.conversationHistory.putIfAbsent(conversationId, new ArrayList<>());
		this.conversationHistory.get(conversationId).addAll(messages);
	}

	@Override
	public List<Message> get(String conversationId, int lastN) {
		List<Message> all = this.conversationHistory.get(conversationId);
		return all != null ? all.stream().skip(Math.max(0, all.size() - lastN)).toList() : List.of();
	}

	@Override
	public void clear(String conversationId) {
		this.conversationHistory.remove(conversationId);
	}

}
```

使用

```java
@GetMapping("/memory")
public Flux<String> memoryChatMemory(
        @RequestParam("prompt") String prompt,
        @RequestParam("chatId") String chatId,
        HttpServletResponse response
) {
    response.setCharacterEncoding("UTF-8");
    return chatClient.prompt()
            .user(prompt)
            .advisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
            .advisors(advisorSpec -> advisorSpec
                    .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                    .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
            .stream()
            .content();
}
```

### 数据库(Jdbc)

**MessageDeserializer.java**

```java
/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.memory.jdbc.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.model.Media;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MessageDeserializer extends JsonDeserializer<Message> {

	private static final Logger logger = LoggerFactory.getLogger(MessageDeserializer.class);

	public Message deserialize(JsonParser p, DeserializationContext ctxt) {
		ObjectMapper mapper = (ObjectMapper) p.getCodec();
		JsonNode node = null;
		Message message = null;
		try {
			node = mapper.readTree(p);
			String messageType = node.get("messageType").asText();
			switch (messageType) {
				case "USER" -> message = new UserMessage(node.get("text").asText(),
						mapper.convertValue(node.get("media"), new TypeReference<Collection<Media>>() {
						}), mapper.convertValue(node.get("metadata"), new TypeReference<Map<String, Object>>() {
						}));
				case "ASSISTANT" -> message = new AssistantMessage(node.get("text").asText(),
						mapper.convertValue(node.get("metadata"), new TypeReference<Map<String, Object>>() {
						}), (List<AssistantMessage.ToolCall>) mapper.convertValue(node.get("toolCalls"),
								new TypeReference<Collection<AssistantMessage.ToolCall>>() {
								}),
						(List<Media>) mapper.convertValue(node.get("media"), new TypeReference<Collection<Media>>() {
						}));
				default -> throw new IllegalArgumentException("Unknown message type: " + messageType);
			}
			;
		}
		catch (IOException e) {
			logger.error("Error deserializing message", e);
		}
		return message;
	}

}
```

**JdbcChatMemory.java**

```java
/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.memory.jdbc;

import com.alibaba.cloud.ai.memory.jdbc.serializer.MessageDeserializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author future0923
 */
public abstract class JdbcChatMemory implements ChatMemory, AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(JdbcChatMemory.class);

	private static final String DEFAULT_TABLE_NAME = "chat_memory";

	private final Connection connection;

	private final String tableName;

	private final ObjectMapper objectMapper = new ObjectMapper();

	protected abstract String jdbcType();

	protected abstract String hasTableSql(String tableName);

	protected abstract String createTableSql(String tableName);

	protected JdbcChatMemory(String username, String password, String jdbcUrl) {
		this(username, password, jdbcUrl, DEFAULT_TABLE_NAME);
	}

	protected JdbcChatMemory(String username, String password, String jdbcUrl, String tableName) {
		// Configure ObjectMapper to support interface deserialization
		SimpleModule module = new SimpleModule();
		module.addDeserializer(Message.class, new MessageDeserializer());
		this.objectMapper.registerModule(module);
		this.tableName = tableName;
		try {
			this.connection = DriverManager.getConnection(jdbcUrl, username, password);
			checkAndCreateTable();
		}
		catch (SQLException e) {
			throw new RuntimeException("Error connecting to the database", e);
		}
	}

	protected JdbcChatMemory(Connection connection) {
		this(connection, DEFAULT_TABLE_NAME);
	}

	protected JdbcChatMemory(Connection connection, String tableName) {
		// Configure ObjectMapper to support interface deserialization
		SimpleModule module = new SimpleModule();
		module.addDeserializer(Message.class, new MessageDeserializer());
		this.objectMapper.registerModule(module);
		this.connection = connection;
		this.tableName = tableName;
		try {
			checkAndCreateTable();
		}
		catch (SQLException e) {
			throw new RuntimeException("Error checking the database table", e);
		}
	}

	private void checkAndCreateTable() throws SQLException {
		String checkTableQuery = hasTableSql(tableName);
		try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(checkTableQuery)) {
			if (rs.next()) {
				logger.info("Table {} exists.", tableName);
			}
			else {
				logger.info("Table {} does not exist. Creating table...", tableName);
				createTable();
			}
		}
	}

	private void createTable() {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute(createTableSql(tableName));
			logger.info("Table {} created successfully.", tableName);
		}
		catch (Exception e) {
			throw new RuntimeException("Error creating table " + tableName + " ", e);
		}
	}

	@Override
	public void add(String conversationId, List<Message> messages) {
		try {
			List<Message> all = this.selectMessageById(conversationId);
			all.addAll(messages);
			this.updateMessageById(conversationId, this.objectMapper.writeValueAsString(all));
		}
		catch (Exception e) {
			logger.error("Error adding messages to {} chat memory", jdbcType(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Message> get(String conversationId, int lastN) {
		List<Message> all;
		try {
			all = this.selectMessageById(conversationId);
		}
		catch (Exception e) {
			logger.error("Error getting messages from {} chat memory", jdbcType(), e);
			throw new RuntimeException(e);
		}
		return all != null ? all.stream().skip(Math.max(0, all.size() - lastN)).toList() : List.of();
	}

	@Override
	public void clear(String conversationId) {
		StringBuilder sql = new StringBuilder("DELETE FROM " + tableName + " WHERE conversation_id = '");
		sql.append(conversationId);
		sql.append("'");
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql.toString());
		}
		catch (Exception e) {
			throw new RuntimeException("Error executing delete ", e);
		}

	}

	@Override
	public void close() throws Exception {
		if (connection != null) {
			connection.close();
		}
	}

	public void clearOverLimit(String conversationId, int maxLimit, int deleteSize) {
		try {
			List<Message> all = this.selectMessageById(conversationId);
			if (all.size() >= maxLimit) {
				all = all.stream().skip(Math.max(0, deleteSize)).toList();
				this.updateMessageById(conversationId, this.objectMapper.writeValueAsString(all));
			}
		}
		catch (Exception e) {
			logger.error("Error clearing messages from {} chat memory", jdbcType(), e);
			throw new RuntimeException(e);
		}
	}

	public List<Message> selectMessageById(String conversationId) {
		List<Message> totalMessage = new ArrayList<>();
		StringBuilder sql = new StringBuilder("SELECT messages FROM " + tableName + " WHERE conversation_id = '");
		sql.append(conversationId);
		sql.append("'");
		try (Statement stmt = connection.createStatement()) {
			ResultSet resultSet = stmt.executeQuery(sql.toString());
			String oldMessage;
			while (resultSet.next()) {
				oldMessage = resultSet.getString("messages");
				if (oldMessage != null && !oldMessage.isEmpty()) {
					List<Message> all = this.objectMapper.readValue(oldMessage, new TypeReference<>() {
					});
					totalMessage.addAll(all);
				}
			}
		}
		catch (SQLException | JsonProcessingException e) {
			logger.error("select message by {} error，sql:{}", jdbcType(), sql, e);
			throw new RuntimeException(e);
		}
		return totalMessage;
	}

	public void updateMessageById(String conversationId, String messages) {
		// Remove newlines and escape single quotes
		messages = messages.replaceAll("[\\r\\n]", "").replace("'", "''");
		String sql;
		if (this.selectMessageById(conversationId).isEmpty()) {
			sql = "INSERT INTO chat_memory (messages, conversation_id) VALUES (?, ?)";
		}
		else {
			sql = "UPDATE chat_memory SET messages = ? WHERE conversation_id = ?";
		}
		try (PreparedStatement stmt = this.connection.prepareStatement(sql)) {
			if (this.selectMessageById(conversationId).isEmpty()) {
				stmt.setString(1, messages);
				stmt.setString(2, conversationId);
			}
			else {
				stmt.setString(1, messages);
				stmt.setString(2, conversationId);
			}
			stmt.executeUpdate();
		}
		catch (SQLException e) {
			logger.error("update message by {} error，sql:{}", sql, jdbcType(), e);
			throw new RuntimeException(e);
		}
	}

}
```

实现其他的数据库，继承 `JdbcChatMemory` 重写对应方法即可。 

#### MySQL

```java
/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.memory.jdbc;

import java.sql.Connection;

/**
 * @author future0923
 */
public class MysqlChatMemory extends JdbcChatMemory {

	private static final String JDBC_TYPE = "mysql";

	public MysqlChatMemory(String username, String password, String jdbcUrl) {
		super(username, password, jdbcUrl);
	}

	public MysqlChatMemory(String username, String password, String jdbcUrl, String tableName) {
		super(username, password, jdbcUrl, tableName);
	}

	public MysqlChatMemory(Connection connection) {
		super(connection);
	}

	public MysqlChatMemory(Connection connection, String tableName) {
		super(connection, tableName);
	}

	@Override
	protected String jdbcType() {
		return JDBC_TYPE;
	}

	@Override
	protected String hasTableSql(String tableName) {
		return String.format("SHOW TABLES LIKE '%s'", tableName);
	}

	@Override
	protected String createTableSql(String tableName) {
		return String.format(
				"CREATE TABLE %s( id BIGINT AUTO_INCREMENT PRIMARY KEY,conversation_id  VARCHAR(256)  NULL,messages TEXT NULL,UNIQUE (conversation_id)) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;",
				tableName);
	}

}
```

使用

```java
public void mysql() {
    MysqlChatMemory chatMemory = new MysqlChatMemory("root", "123456",
            "jdbc:mysql://127.0.0.1:3306/spring_ai_alibaba_chat_memory");
    ChatClient chatClient = ChatClient.create(new DashScopeChatModel(new DashScopeApi("test-api-key")));
    String content1 = chatClient.prompt()
        .advisors(new MessageChatMemoryAdvisor(chatMemory))
        .user("我是张三😄")
        .call()
        .content();
    System.out.println(content1);
    String content2 = chatClient.prompt()
        .advisors(new MessageChatMemoryAdvisor(chatMemory))
        .user("我是谁")
        .call()
        .content();
    System.out.println(content2);
    Assertions.assertTrue(content2.contains("张三"));
}
```

#### Oracle

```java
/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.memory.jdbc;

import java.sql.Connection;

/**
 * @author future0923
 */
public class OracleChatMemory extends JdbcChatMemory {

	private static final String JDBC_TYPE = "oracle";

	public OracleChatMemory(String username, String password, String url) {
		super(username, password, url);
	}

	public OracleChatMemory(String username, String password, String url, String tableName) {
		super(username, password, url, tableName);
	}

	public OracleChatMemory(Connection connection) {
		super(connection);
	}

	public OracleChatMemory(Connection connection, String tableName) {
		super(connection, tableName);
	}

	@Override
	protected String jdbcType() {
		return JDBC_TYPE;
	}

	@Override
	protected String hasTableSql(String tableName) {
		return String.format("SELECT table_name FROM user_tables WHERE table_name = '%s'", tableName.toUpperCase());
	}

	@Override
	protected String createTableSql(String tableName) {
		return String.format(
				"CREATE TABLE %s ( id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, conversation_id VARCHAR2(256), messages CLOB, CONSTRAINT uniq_conversation_id UNIQUE (conversation_id));",
				tableName);
	}

}

```

使用

```java
public void oracle() {
    ChatMemory chatMemory = new OracleChatMemory("system", "123456", "jdbc:oracle:thin:@localhost:1521/XEPDB1");
    ChatClient chatClient = ChatClient.create(new DashScopeChatModel(new DashScopeApi("")));
    String content1 = chatClient.prompt()
        .advisors(new MessageChatMemoryAdvisor(chatMemory))
        .user("我是张三😄")
        .call()
        .content();
    System.out.println(content1);
    String content2 = chatClient.prompt()
        .advisors(new MessageChatMemoryAdvisor(chatMemory))
        .user("我是谁")
        .call()
        .content();
    System.out.println(content2);
    Assertions.assertTrue(content2.contains("张三"));
}
```

#### PostgresChatMemory

```java
/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.memory.jdbc;

import java.sql.Connection;

/**
 * @author future0923
 */
public class PostgresChatMemory extends JdbcChatMemory {

	private static final String JDBC_TYPE = "postgresql";

	public PostgresChatMemory(String username, String password, String url) {
		super(username, password, url);
	}

	public PostgresChatMemory(String username, String password, String jdbcUrl, String tableName) {
		super(username, password, jdbcUrl, tableName);
	}

	public PostgresChatMemory(Connection connection) {
		super(connection);
	}

	public PostgresChatMemory(Connection connection, String tableName) {
		super(connection, tableName);
	}

	@Override
	protected String jdbcType() {
		return JDBC_TYPE;
	}

	@Override
	protected String hasTableSql(String tableName) {
		return String.format(
				"SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE' AND table_name LIKE '%s'",
				tableName);
	}

	@Override
	protected String createTableSql(String tableName) {
		return String.format(
				"CREATE TABLE %s ( id BIGSERIAL PRIMARY KEY, conversation_id VARCHAR(256), messages TEXT, UNIQUE (conversation_id));",
				tableName);
	}

}
```

使用

```java
public void postgresql() {
    ChatMemory chatMemory = new PostgresChatMemory("root", "123456",
            "jdbc:postgresql://127.0.0.1:5432/spring_ai_alibaba_chat_memory");
    ChatClient chatClient = ChatClient.create(new DashScopeChatModel(new DashScopeApi("")));
    String content1 = chatClient.prompt()
        .advisors(new MessageChatMemoryAdvisor(chatMemory))
        .user("我是张三😄")
        .call()
        .content();
    System.out.println(content1);
    String content2 = chatClient.prompt()
        .advisors(new MessageChatMemoryAdvisor(chatMemory))
        .user("我是谁")
        .call()
        .content();
    System.out.println(content2);
    Assertions.assertTrue(content2.contains("张三"));
}
```

#### SqlServerChatMemory

```java
/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.memory.jdbc;

import java.sql.Connection;

/**
 * @author future0923
 */
public class SqlServerChatMemory extends JdbcChatMemory {

	private static final String JDBC_TYPE = "sqlserver";

	public SqlServerChatMemory(String username, String password, String url) {
		super(username, password, url);
	}

	public SqlServerChatMemory(String username, String password, String jdbcUrl, String tableName) {
		super(username, password, jdbcUrl, tableName);
	}

	public SqlServerChatMemory(Connection connection) {
		super(connection);
	}

	public SqlServerChatMemory(Connection connection, String tableName) {
		super(connection, tableName);
	}

	@Override
	protected String jdbcType() {
		return JDBC_TYPE;
	}

	@Override
	protected String hasTableSql(String tableName) {
		return String.format("SELECT name FROM sys.tables WHERE name LIKE '%s';", tableName);
	}

	@Override
	protected String createTableSql(String tableName) {
		return String.format(
				"CREATE TABLE %s ( id BIGINT IDENTITY(1,1) PRIMARY KEY, conversation_id NVARCHAR(256), messages NVARCHAR(MAX), CONSTRAINT uq_conversation_id UNIQUE (conversation_id));",
				tableName);
	}

}

```

使用

```java
public void sqlServer() {
    ChatMemory chatMemory = new SqlServerChatMemory("sa", "qWeR124563",
            "jdbc:sqlserver://localhost:1433;database=spring_ai_alibaba_chat_memory;encrypt=true;trustServerCertificate=true");
    ChatClient chatClient = ChatClient.create(new DashScopeChatModel(new DashScopeApi("")));
    String content1 = chatClient.prompt()
        .advisors(new MessageChatMemoryAdvisor(chatMemory))
        .user("我是张三😄")
        .call()
        .content();
    System.out.println(content1);
    String content2 = chatClient.prompt()
        .advisors(new MessageChatMemoryAdvisor(chatMemory))
        .user("我是谁")
        .call()
        .content();
    System.out.println(content2);
    Assertions.assertTrue(content2.contains("张三"));
}
```

### Redis

```java
@GetMapping("/redis")
public Flux<String> redisChatMemory(
        @RequestParam("prompt") String prompt,
        @RequestParam("chatId") String chatId,
        HttpServletResponse response
) {
    response.setCharacterEncoding("UTF-8");
    return chatClient.prompt()
            .user(prompt)
            .advisors(new MessageChatMemoryAdvisor(redisChatMemory))
            .advisors(advisorSpec -> advisorSpec
                    .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                    .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
            .stream()
            .content();
}
```