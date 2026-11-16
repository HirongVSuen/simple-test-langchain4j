package com.hirong.agentic;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.http.client.okhttp.OkHttpClientBuilder;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.*;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;


/**
 * @author hirong
 */
public class Client {

    public static void main(String[] args) {
//        OpenAiChatModel openAiChatModel = getOpenAiChatModel();
//        simpleHello(openAiChatModel);
//        mutualityChat(openAiChatModel);
//        StreamingChatModel model = getStreamingChatModel();
//        streamSimpleHello(model);
//        simpleAssistant();
//        systemProvider();
//        returnType();
    }

    private static void returnType() {
        SentimentAnalyzer sentimentAnalyzer = AiServices.create(SentimentAnalyzer.class, getOpenAiChatModel());

        boolean positive = sentimentAnalyzer.isPositive("It's wonderful!");
        System.err.println(positive);
    }

    private static void systemProvider() {
        Assistant tenant = AiServices.builder(Assistant.class).chatModel(getOpenAiChatModel())
                .systemMessageProvider(chatMemoryId -> "You are a good friend of mine. Answer using slang.")
                .systemMessageTransformer((systemMessage, context) -> systemMessage + " Tenant: " + context.invocationParameters().get("tenant") + ".")
                .build();
        String chat = tenant.chat("你好，今天有来租房子的吗？");
        System.out.println(chat);
        chat = tenant.chat("租的怎么样？");
        System.out.println(chat);
    }

    private static void simpleAssistant() {
        Assistant assistant = AiServices.create(Assistant.class, getOpenAiChatModel());
        String chat = assistant.chat("Say 'Hello World'");
        System.out.println(chat);
    }

    private static void streamSimpleHello(StreamingChatModel model) {
        StreamChatRes streamChatRes = new StreamChatRes();
        model.chat("Say 'Hello World'", streamChatRes);
    }

    private static StreamingChatModel getStreamingChatModel() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        StreamingChatModel model = OpenAiStreamingChatModel.builder().apiKey(apiKey)
                .baseUrl("https://api.deepseek.com/")
                .modelName("deepseek-flash")
                .httpClientBuilder(new OkHttpClientBuilder())
//                .logRequests(true)
//                .logResponses(true)
                .build();
        return model;
    }

    private static void mutualityChat(OpenAiChatModel openAiChatModel) {
        UserMessage sayMe = UserMessage.from("你好, 我叫张三!");
        ChatResponse chat = openAiChatModel.chat(sayMe);
        System.out.println(chat.aiMessage().text());
        System.err.println("--------------------------------------------------");
        UserMessage whoI = UserMessage.from("我叫什么名字?");
        chat = openAiChatModel.chat(sayMe, chat.aiMessage(),whoI);
        System.out.println(chat.aiMessage().text());
    }

    private static void simpleHello(OpenAiChatModel openAiChatModel) {
        String answer = openAiChatModel.chat("Say 'Hello World'");
        System.out.println(answer);
    }

    private static OpenAiChatModel getOpenAiChatModel() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-flash")
                .logRequests(true)
                .logResponses(true)
                .httpClientBuilder(new OkHttpClientBuilder())
                .build();
        return openAiChatModel;
    }

    public static class StreamChatRes implements StreamingChatResponseHandler {

        @Override
        public void onPartialResponse(String partialResponse) {
            System.out.println("onPartialResponse_text: " + partialResponse);
            StreamingChatResponseHandler.super.onPartialResponse(partialResponse);
        }

        @Override
        public void onPartialResponse(PartialResponse partialResponse, PartialResponseContext context) {
            System.out.println("onPartialResponse: " + partialResponse.text());
            StreamingChatResponseHandler.super.onPartialResponse(partialResponse, context);
        }

        @Override
        public void onPartialThinking(PartialThinking partialThinking) {
            System.out.println("onPartialThinking_text: " + partialThinking.text());
            StreamingChatResponseHandler.super.onPartialThinking(partialThinking);
        }

        @Override
        public void onPartialThinking(PartialThinking partialThinking, PartialThinkingContext context) {
            System.out.println("onPartialThinking_context_text: " + partialThinking.text());
            StreamingChatResponseHandler.super.onPartialThinking(partialThinking, context);
        }

        @Override
        public void onPartialToolCall(PartialToolCall partialToolCall) {
            System.out.println("onPartialToolCall: " + partialToolCall);
            StreamingChatResponseHandler.super.onPartialToolCall(partialToolCall);
        }

        @Override
        public void onPartialToolCall(PartialToolCall partialToolCall, PartialToolCallContext context) {
            System.out.println("onPartialToolCall: " + partialToolCall);
            StreamingChatResponseHandler.super.onPartialToolCall(partialToolCall, context);
        }

        @Override
        public void onCompleteToolCall(CompleteToolCall completeToolCall) {
            System.out.println("onCompleteToolCall: " + completeToolCall);
            StreamingChatResponseHandler.super.onCompleteToolCall(completeToolCall);
        }

        @Override
        public void onUnmappedRawEvent(Object rawEvent) {
            System.out.println("onUnmappedRawEvent: " + rawEvent);
            StreamingChatResponseHandler.super.onUnmappedRawEvent(rawEvent);
        }

        @Override
        public void onCompleteResponse(ChatResponse completeResponse) {
            System.out.println("onCompleteResponse: " + completeResponse.aiMessage().text());

        }

        @Override
        public void onError(Throwable error) {
            System.err.println("onError: " + error);
        }
    }

    interface SentimentAnalyzer {

        @dev.langchain4j.service.UserMessage("Does {{it}} has a positive sentiment?")
        boolean isPositive(String text);

    }

    interface Assistant {
        /**
         * Chat with the assistant.
         *
         * @param message The message to chat.
         * @return The assistant's response.
         */
//        @dev.langchain4j.service.UserMessage("You are a good friend of mine. Answer using slang. {{it}}")
        String chat(String message);
    }
}
