package com.hospital.appointment.module.chat.controller;

import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.common.result.R;
import com.hospital.appointment.module.appointment.mapper.AppointmentMapper;
import com.hospital.appointment.module.chat.mapper.ChatMessageMapper;
import com.hospital.appointment.module.chat.mapper.ChatSessionMapper;
import com.hospital.appointment.module.chat.model.ChatMessage;
import com.hospital.appointment.module.chat.model.ChatSession;
import com.hospital.appointment.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final AppointmentMapper appointmentMapper;

    @GetMapping("/sessions")
    public R<?> mySessions() {
        String role = UserContext.getRole();
        Long userId = UserContext.getUserId();
        List<ChatSession> list;
        if ("DOCTOR".equals(role)) {
            list = sessionMapper.findByDoctor(userId);
        } else {
            list = sessionMapper.findByPatient(userId);
        }
        return R.ok(list);
    }

    @GetMapping("/sessions/{id}/messages")
    public R<?> messages(@PathVariable Long id) {
        List<ChatMessage> msgs = messageMapper.findBySessionId(id);
        messageMapper.markAsRead(id, UserContext.getUserId());
        return R.ok(msgs);
    }

    @PostMapping("/sessions/{id}/messages")
    public R<ChatMessage> sendMessage(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long senderId = UserContext.getUserId();
        ChatSession session = sessionMapper.selectById(id);
        if (session == null) throw BusinessException.notFound("会话不存在");
        if (session.getStatus() == 0) throw BusinessException.badRequest("会话已关闭");
        if (!session.getPatientId().equals(senderId) && !session.getDoctorId().equals(senderId))
            throw BusinessException.forbidden("无权操作");

        ChatMessage msg = new ChatMessage();
        msg.setSessionId(id);
        msg.setSenderId(senderId);
        msg.setReceiverId(senderId.equals(session.getPatientId()) ? session.getDoctorId() : session.getPatientId());
        msg.setMsgType(body.getOrDefault("msgType", "TEXT"));
        msg.setContent(body.get("content"));
        msg.setIsRead(0);
        messageMapper.insert(msg);

        String content = body.get("content");
        session.setLastMessageContent(content != null && content.length() > 100 ? content.substring(0, 100) : content);
        session.setLastMessageTime(LocalDateTime.now());
        session.setLastMessageType(msg.getMsgType());
        if (session.getPatientId().equals(msg.getReceiverId()))
            session.setUnreadCountPatient(session.getUnreadCountPatient() + 1);
        else
            session.setUnreadCountDoctor(session.getUnreadCountDoctor() + 1);
        sessionMapper.updateById(session);

        return R.ok(msg);
    }

    @PostMapping("/sessions")
    public R<ChatSession> createSession(@RequestBody Map<String, Object> body) {
        Long doctorId = Long.valueOf(body.get("doctorId").toString());
        Long appointmentId = body.containsKey("appointmentId") ? Long.valueOf(body.get("appointmentId").toString()) : null;
        Long patientId = UserContext.getUserId();

        if (appointmentId != null) {
            var appt = appointmentMapper.selectById(appointmentId);
            if (appt == null || appt.getStatus() != 2)
                throw BusinessException.badRequest("仅就诊完成后可发起咨询");
            if (appt.getAppointmentDate().plusDays(7).isBefore(LocalDate.now()))
                throw BusinessException.badRequest("就诊后7天内可发起咨询");
        }

        ChatSession session = new ChatSession();
        session.setPatientId(patientId);
        session.setDoctorId(doctorId);
        session.setAppointmentId(appointmentId);
        session.setStatus(1);
        session.setUnreadCountPatient(0);
        session.setUnreadCountDoctor(0);
        session.setExpireTime(LocalDateTime.now().plusDays(7));
        sessionMapper.insert(session);
        return R.ok(session);
    }

    @PutMapping("/sessions/{id}/close")
    public R<Void> closeSession(@PathVariable Long id) {
        ChatSession session = sessionMapper.selectById(id);
        if (session == null) throw BusinessException.notFound("会话不存在");
        session.setStatus(0);
        sessionMapper.updateById(session);
        return R.okMsg("会话已关闭");
    }
}
