package com.hospital.appointment.module.appointment.service;

import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.module.appointment.mapper.QueueNumberMapper;
import com.hospital.appointment.module.appointment.model.QueueNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueNumberMapper queueNumberMapper;

    public List<QueueNumber> getTodayQueue(Long doctorId) {
        return queueNumberMapper.findTodayQueue(doctorId);
    }

    @Transactional
    public QueueNumber callNext(Long doctorId) {
        List<QueueNumber> queue = queueNumberMapper.findTodayQueue(doctorId);
        QueueNumber next = queue.stream()
                .filter(q -> q.getStatus() == 0)
                .findFirst()
                .orElseThrow(() -> BusinessException.badRequest("没有等待中的患者"));

        next.setStatus(1);
        next.setCallTime(LocalDateTime.now());
        queueNumberMapper.updateById(next);
        return queueNumberMapper.findByIdWithDetails(next.getId());
    }

    @Transactional
    public QueueNumber markCompleted(Long appointmentId) {
        QueueNumber qn = findOrThrow(appointmentId);
        if (qn.getStatus() != 1) throw BusinessException.badRequest("仅已叫号可标记就诊");
        qn.setStatus(3);
        queueNumberMapper.updateById(qn);
        return qn;
    }

    @Transactional
    public QueueNumber markMissed(Long appointmentId) {
        QueueNumber qn = findOrThrow(appointmentId);
        if (qn.getStatus() != 1) throw BusinessException.badRequest("仅已叫号可标记过号");
        qn.setStatus(2);
        queueNumberMapper.updateById(qn);
        return qn;
    }

    @Transactional
    public QueueNumber markWaitAgain(Long appointmentId) {
        QueueNumber qn = findOrThrow(appointmentId);
        if (qn.getStatus() != 2) throw BusinessException.badRequest("仅过号可重新排队");
        qn.setStatus(0);
        qn.setCallTime(null);
        queueNumberMapper.updateById(qn);
        return qn;
    }

    public QueueNumber addToQueue(Long appointmentId, Long doctorId) {
        int max = queueNumberMapper.maxQueueNumber(doctorId);
        QueueNumber qn = new QueueNumber();
        qn.setAppointmentId(appointmentId);
        qn.setDoctorId(doctorId);
        qn.setQueueNumber(max + 1);
        qn.setStatus(0);
        queueNumberMapper.insert(qn);
        return qn;
    }

    private QueueNumber findOrThrow(Long appointmentId) {
        var qns = queueNumberMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<QueueNumber>()
                        .eq(QueueNumber::getAppointmentId, appointmentId));
        if (qns.isEmpty()) throw BusinessException.notFound("排队记录不存在");
        return qns.get(0);
    }

    public Integer getCurrentQueueNumber(Long doctorId) {
        List<QueueNumber> queue = queueNumberMapper.findTodayQueue(doctorId);
        return queue.stream()
                .filter(q -> q.getStatus() == 1)
                .mapToInt(QueueNumber::getQueueNumber)
                .max()
                .orElse(0);
    }
}
