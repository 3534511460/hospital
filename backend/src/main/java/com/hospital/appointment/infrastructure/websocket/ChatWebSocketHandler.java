package com.hospital.appointment.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.appointment.module.chat.mapper.ChatMessageMapper;
import com.hospital.appointment.module.chat.mapper.ChatSessionMapper;
import com.hospital.appointment.module.chat.model.ChatMessage;
import com.hospital.appointment.module.chat.model.ChatSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Map<Long, WebSocketSession> onlineUsers = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;

    public ChatWebSocketHandler(ChatSessionMapper sessionMapper, ChatMessageMapper messageMapper) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            onlineUsers.put(userId, session);
            log.info("WS connected: userId={}", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long senderId = (Long) session.getAttributes().get("userId");
        if (senderId == null) return;

        var node = objectMapper.readTree(message.getPayload());
        String type = node.has("type") ? node.get("type").asText() : "MESSAGE";

        if ("PING".equals(type)) {
            session.sendMessage(new TextMessage("{\"type\":\"PONG\"}"));
            return;
        }

        if ("MESSAGE".equals(type)) {
            Long sessionId = node.get("sessionId").asLong();
            String content = node.get("content").asText();
            String msgType = node.has("msgType") ? node.get("msgType").asText() : "TEXT";

            ChatSession chatSession = sessionMapper.selectById(sessionId);
            if (chatSession == null || chatSession.getStatus() == 0) return;
            if (!chatSession.getPatientId().equals(senderId) && !chatSession.getDoctorId().equals(senderId)) return;

            LocalDateTime now = LocalDateTime.now();
            if (chatSession.getExpireTime() != null && now.isAfter(chatSession.getExpireTime())) return;

            ChatMessage msg = new ChatMessage();
            msg.setSessionId(sessionId);
            msg.setSenderId(senderId);
            msg.setReceiverId(senderId.equals(chatSession.getPatientId()) ? chatSession.getDoctorId() : chatSession.getPatientId());
            msg.setMsgType(msgType);
            msg.setContent(content);
            msg.setIsRead(0);
            messageMapper.insert(msg);

            chatSession.setLastMessageContent(content.length() > 100 ? content.substring(0, 100) : content);
            chatSession.setLastMessageTime(now);
            chatSession.setLastMessageType(msgType);
            Long otherId = msg.getReceiverId();
            if (chatSession.getPatientId().equals(otherId)) {
                chatSession.setUnreadCountPatient(chatSession.getUnreadCountPatient() + 1);
            } else {
                chatSession.setUnreadCountDoctor(chatSession.getUnreadCountDoctor() + 1);
            }
            sessionMapper.updateById(chatSession);

            String payload = objectMapper.writeValueAsString(Map.of(
                    "type", "MESSAGE",
                    "id", msg.getId(),
                    "sessionId", sessionId,
                    "senderId", senderId,
                    "content", content,
                    "msgType", msgType,
                    "createTime", now.toString()
            ));

            WebSocketSession receiverSession = onlineUsers.get(msg.getReceiverId());
            if (receiverSession != null && receiverSession.isOpen()) {
                receiverSession.sendMessage(new TextMessage(payload));
            }
            session.sendMessage(new TextMessage(payload));
        }

        if ("READ".equals(type)) {
            Long sessionId = node.get("sessionId").asLong();
            messageMapper.markAsRead(sessionId, senderId);
            ChatSession cs = sessionMapper.selectById(sessionId);
            if (cs != null) {
                if (cs.getPatientId().equals(senderId)) cs.setUnreadCountPatient(0);
                else cs.setUnreadCountDoctor(0);
                sessionMapper.updateById(cs);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            onlineUsers.remove(userId);
            log.info("WS disconnected: userId={}", userId);
        }
    }
}
