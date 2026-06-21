package com.hospital.appointment.module.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.appointment.module.ai.mapper.AiChatHistoryMapper;
import com.hospital.appointment.module.ai.model.AiChatHistory;
import com.hospital.appointment.module.hospital.mapper.DepartmentMapper;
import com.hospital.appointment.module.hospital.mapper.DoctorMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class DeepSeekService {

    private final ObjectMapper objectMapper;
    private final AiChatHistoryMapper historyMapper;
    private final DepartmentMapper departmentMapper;
    private final DoctorMapper doctorMapper;
    private final String apiKey;
    private final String model;
    private final WebClient webClient;
    private final boolean hasApiKey;

    public DeepSeekService(@Value("${app.ai.api-key:}") String apiKey,
                           @Value("${app.ai.api-url}") String apiUrl,
                           @Value("${app.ai.model}") String model,
                           ObjectMapper objectMapper,
                           AiChatHistoryMapper historyMapper,
                           DepartmentMapper departmentMapper,
                           DoctorMapper doctorMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
        this.historyMapper = historyMapper;
        this.departmentMapper = departmentMapper;
        this.doctorMapper = doctorMapper;
        this.hasApiKey = apiKey != null && !apiKey.isBlank() && apiKey.startsWith("sk-");
        this.webClient = WebClient.builder().baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json").build();
    }

    public Map<String, Object> chatSync(Long userId, String message, String sessionId, String type) {
        if (!hasApiKey) {
            String fallback = generateFallback(message, type);
            saveHistory(userId, sessionId, message, fallback, type);
            return Map.of("reply", fallback, "mode", "local");
        }

        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", buildSystemPrompt(type)),
                            Map.of("role", "user", "content", message)
                    ),
                    "stream", false, "max_tokens", 2000, "temperature", 0.7
            );

            String response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(body)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);
            String text = root.get("choices").get(0).get("message").get("content").asText();
            saveHistory(userId, sessionId, message, text, type);
            return Map.of("reply", text, "mode", "ai");

        } catch (Exception e) {
            log.warn("DeepSeek API failed: {}", e.getMessage());
            String fallback = generateFallback(message, type);
            saveHistory(userId, sessionId, message, fallback, type);
            return Map.of("reply", fallback, "mode", "local");
        }
    }

    public SseEmitter chatStream(Long userId, String message, String sessionId, String type) {
        SseEmitter emitter = new SseEmitter(120_000L);
        String text = (String) chatSync(userId, message, sessionId, type).get("reply");
        try {
            for (char c : text.toCharArray()) {
                emitter.send(SseEmitter.event().name("delta").data(String.valueOf(c)));
                Thread.sleep(15);
            }
            emitter.send(SseEmitter.event().name("done").data("{\"type\":\"done\"}"));
            emitter.complete();
        } catch (Exception e) {
            try { emitter.send(SseEmitter.event().name("delta").data(text)); } catch (IOException ignored) {}
            try { emitter.send(SseEmitter.event().name("done").data("{\"type\":\"done\"}")); } catch (IOException ignored) {}
            emitter.complete();
        }
        return emitter;
    }

    private String buildSystemPrompt(String type) {
        var depts = departmentMapper.selectEnabled();
        StringBuilder deptList = new StringBuilder();
        for (var d : depts)
            deptList.append("- ").append(d.getName()).append("：").append(d.getDescription() != null ? d.getDescription() : "").append("\n");

        return """
            你是医院智能导诊助手，仅提供导诊参考，不能替代医生诊断。

            可用科室：
            %s

            规则：
            1. 根据患者症状推荐1-3个匹配科室并说明理由
            2. 疾病名称查询直接推荐对应科室
            3. 常见就诊问题解答
            4. 所有回复末尾标注：【智能导诊仅供参考，不能替代专业医生诊断】
            5. 紧急症状（胸痛、大出血等）引导去急诊科
            6. 回复简洁专业，用中文
            """.formatted(deptList.toString());
    }

    public String generateFallback(String message, String type) {
        var depts = departmentMapper.selectEnabled();
        if (depts.isEmpty()) return "暂无可用的科室信息。请到医院现场挂号。\n【智能导诊仅供参考，不能替代专业医生诊断】";

        StringBuilder sb = new StringBuilder("根据您的描述，建议考虑以下科室：\n\n");
        int count = Math.min(3, depts.size());
        for (int i = 0; i < count; i++) {
            var d = depts.get(i);
            sb.append(i + 1).append(". **").append(d.getName()).append("**");
            if (d.getDescription() != null) sb.append(" — ").append(d.getDescription());
            sb.append("\n");
        }
        sb.append("\n请根据自身情况选择科室挂号。\n【智能导诊仅供参考，不能替代专业医生诊断】");
        return sb.toString();
    }

    private void saveHistory(Long userId, String sessionId, String msg, String resp, String type) {
        AiChatHistory h = new AiChatHistory();
        h.setUserId(userId);
        h.setSessionId(sessionId);
        h.setUserMessage(msg);
        h.setAiResponse(resp);
        h.setChatType(type != null ? type : "TRIAGE");
        h.setModel(model);
        historyMapper.insert(h);
    }

    public List<AiChatHistory> getHistory(String sessionId) {
        return historyMapper.findBySessionId(sessionId);
    }

    public List<AiChatHistory> getUserHistory(Long userId) {
        return historyMapper.findByUserId(userId);
    }
}
