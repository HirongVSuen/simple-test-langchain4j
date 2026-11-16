package com.hirong.agentic.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.memory.ChatMemoryAccess;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

/**
 * Assistant service interface.
 *
 * @author hirong
 */
@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        streamingChatModel = "openAiStreamingChatModel" , // 属性名修正
        chatMemoryProvider = "chatMemoryProvider"   // 指向下面的 Bean
)
@SystemMessage("You are a polite assistant")
public interface Assistant extends ChatMemoryAccess {

    /**
     * Chat with the assistant.
     *
     * @param userMessage The user message.
     * @return The assistant's response.
     */
    @SystemMessage("You are a polite assistant")
    String chat(String userMessage);

    /**
     * Chat with the assistant using a specific memory.
     *
     * @param memoryId The ID of the memory to use.
     * @param message The user message.
     * @return The assistant's response.
     */
    TokenStream chat(@MemoryId String memoryId, @UserMessage String message);
}