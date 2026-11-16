package com.hirong.agentic.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * SPA fallback：把「非静态资源、非 API」的前端路径交给 index.html。
 *
 * <p>路径段里排除点号（{@code [^.]}），这样 /css/main.css、/js/main.js、
 * /fragments/*.html 仍然由 Spring 的静态资源处理器负责。</p>
 */
@Controller
public class SpaFallbackController {

    @GetMapping({"/", "/{path:[^\\.]*}", "/{path:[^\\.]*}/{sub:[^\\.]*}"})
    public Object forward(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.equals("/api") || uri.startsWith("/api/")) {
            return ResponseEntity.notFound().build();
        }
        return "forward:/index.html";
    }
    /**
     * 未知的 /api/** 路径不应该落进 SPA fallback，明确返回 404。
     * 精确匹配的 API 映射优先级更高，不受影响。
     */
    @GetMapping("/api/**")
    public ResponseEntity<Void> apiNotFound() {
        return ResponseEntity.notFound().build();
    }
}
