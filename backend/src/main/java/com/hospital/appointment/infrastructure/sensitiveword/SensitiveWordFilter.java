package com.hospital.appointment.infrastructure.sensitiveword;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.appointment.module.chat.mapper.ChatMessageMapper;
import com.hospital.appointment.module.message.mapper.SiteMessageMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class SensitiveWordFilter {

    private final com.hospital.appointment.module.chat.mapper.ChatMessageMapper chatMessageMapper;
    private final com.hospital.appointment.module.message.mapper.SiteMessageMapper siteMessageMapper;
    private Set<String> words = new HashSet<>();

    public SensitiveWordFilter(ChatMessageMapper chatMessageMapper, SiteMessageMapper siteMessageMapper) {
        this.chatMessageMapper = chatMessageMapper;
        this.siteMessageMapper = siteMessageMapper;
    }

    @PostConstruct
    public void loadWords() {
        // 简化版：直接初始化常见敏感词
        words.addAll(List.of("脏话1", "脏话2", "广告"));
        log.info("敏感词库加载完成，共 {} 个词", words.size());
    }

    public boolean containsSensitive(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        for (String word : words) {
            if (lower.contains(word.toLowerCase())) return true;
        }
        return false;
    }

    public String filter(String text) {
        if (text == null) return null;
        String result = text;
        for (String word : words) {
            result = result.replaceAll("(?i)" + word, "*".repeat(word.length()));
        }
        return result;
    }
}
