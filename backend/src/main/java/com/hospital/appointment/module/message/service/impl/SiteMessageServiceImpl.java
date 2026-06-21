package com.hospital.appointment.module.message.service.impl;

import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.module.message.mapper.SiteMessageMapper;
import com.hospital.appointment.module.message.model.SiteMessage;
import com.hospital.appointment.module.message.service.SiteMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteMessageServiceImpl implements SiteMessageService {

    private final SiteMessageMapper messageMapper;

    @Override
    public List<SiteMessage> getMyMessages(Long userId, int page, int size) {
        List<SiteMessage> all = messageMapper.findByReceiverId(userId);
        int start = (page - 1) * size;
        int end = Math.min(start + size, all.size());
        if (start >= all.size()) return List.of();
        return all.subList(start, end);
    }

    @Override
    public void sendSystemMessage(Long receiverId, String title, String content) {
        SiteMessage msg = new SiteMessage();
        msg.setSenderId(0L);
        msg.setReceiverId(receiverId);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setMsgType("SYSTEM");
        msg.setIsRead(0);
        messageMapper.insert(msg);
    }

    @Override
    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        SiteMessage msg = messageMapper.selectById(messageId);
        if (msg == null) throw BusinessException.notFound("消息不存在");
        if (!msg.getReceiverId().equals(userId)) throw BusinessException.forbidden("无权操作");
        msg.setIsRead(1);
        msg.setReadTime(LocalDateTime.now());
        messageMapper.updateById(msg);
    }

    @Override
    public int unreadCount(Long userId) {
        return messageMapper.countUnread(userId);
    }
}
