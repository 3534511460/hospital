package com.hospital.appointment.module.message.service;

import com.hospital.appointment.module.message.model.SiteMessage;

import java.util.List;

public interface SiteMessageService {
    List<SiteMessage> getMyMessages(Long userId, int page, int size);
    void sendSystemMessage(Long receiverId, String title, String content);
    void markAsRead(Long messageId, Long userId);
    int unreadCount(Long userId);
}
