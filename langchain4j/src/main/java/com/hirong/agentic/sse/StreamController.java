package com.hirong.agentic.sse;

import com.hirong.agentic.service.Assistant;
import dev.langchain4j.service.TokenStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author hirong
 */
@RestController
@RequestMapping("/api")
public class StreamController {

    @Autowired
    private Assistant assistant;

    @GetMapping("/chat")
    public String chat(@RequestParam("message") String message) {
        return assistant.chat(message);
    }
    public record ChatRequest(String memoryId, String message) {}
    @PostMapping(value = "/chat/stream",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestBody ChatRequest req) {
        SseEmitter emitter = new SseEmitter(0L);
        TokenStream tokenStream = assistant.chat(req.memoryId, req.message);

        // 用一个原子标记来记录 SseEmitter 是否已失效
        AtomicBoolean clientGone = new AtomicBoolean(false);
        emitter.onCompletion(() -> clientGone.set(true));
        emitter.onError(t -> clientGone.set(true));
        emitter.onTimeout(() -> {
            clientGone.set(true);
            emitter.complete();
        });

        tokenStream
                .onPartialResponseWithContext((token, context) -> {
                    // 关键：在每次收到 token 时，检查客户端是否还在
                    if (clientGone.get()) {
                        context.streamingHandle().cancel(); // 客户端已断开，取消上游
                        return;
                    }
                    try {
                        emitter.send(SseEmitter.event().data(token.text()));
                    } catch (IOException e) {
                        // send 失败通常意味着客户端已断开
                        clientGone.set(true);
                        context.streamingHandle().cancel(); // 兜底取消
                        emitter.completeWithError(e);
                    }
                })
                .onCompleteResponse(response -> {
                    if (!clientGone.get()) {
                        try { emitter.send(SseEmitter.event().data("[DONE]")); } catch (IOException ignored) {}
                        emitter.complete();
                    }
                })
                .onError(error -> {
                    if (!clientGone.get()) {
                        emitter.completeWithError(error);
                    }
                })
                .start();

        return emitter;
    }
}
