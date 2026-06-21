package com.hospital.appointment.module.ai.controller;

import com.hospital.appointment.common.result.R;
import com.hospital.appointment.module.ai.model.AiChatRequest;
import com.hospital.appointment.module.ai.service.DeepSeekService;
import com.hospital.appointment.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final DeepSeekService deepSeekService;

    @PostMapping("/chat")
    public R<Map<String, Object>> chat(@RequestBody AiChatRequest request) {
        String sessionId = request.getSessionId() != null ? request.getSessionId() : "sess_" + System.currentTimeMillis();
        return R.ok(deepSeekService.chatSync(UserContext.getUserId(), request.getMessage(), sessionId, request.getType()));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody AiChatRequest request) {
        String sessionId = request.getSessionId() != null ? request.getSessionId() : "sess_" + System.currentTimeMillis();
        return deepSeekService.chatStream(UserContext.getUserId(), request.getMessage(), sessionId, request.getType());
    }

    @GetMapping("/history/{sessionId}")
    public R<?> sessionHistory(@PathVariable String sessionId) {
        return R.ok(deepSeekService.getHistory(sessionId));
    }

    @GetMapping("/history")
    public R<?> myHistory() {
        return R.ok(deepSeekService.getUserHistory(UserContext.getUserId()));
    }
}
