package com.hirong.agentic;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * AgenticApplication
 *
 * @author hirong
 */
@SpringBootApplication
public class AgenticApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(AgenticApplication.class);
        application.run(args);
    }

    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)                          // 关键：把 id 写入记忆实例
                .maxMessages(20)
                .build();
    }
}
