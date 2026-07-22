package com.hospital.appointment.module.appointment.service;

import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.module.appointment.mapper.AppointmentMapper;
import com.hospital.appointment.module.appointment.mapper.QueueNumberMapper;
import com.hospital.appointment.module.appointment.model.Appointment;
import com.hospital.appointment.module.appointment.model.QueueNumber;
import com.hospital.appointment.module.hospital.mapper.DoctorMapper;
import com.hospital.appointment.module.hospital.model.Doctor;
import com.hospital.appointment.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueNumberMapper queueNumberMapper;
    private final AppointmentMapper appointmentMapper;
    private final DoctorMapper doctorMapper;

    public List<QueueNumber> getTodayQueue(Long doctorId) {
        return queueNumberMapper.findTodayQueue(doctorId);
    }

    public List<QueueNumber> getAllTodayQueue() {
        return queueNumberMapper.findAllTodayQueue();
    }

    public List<QueueNumber> getDepartmentQueue(Long departmentId) {
        var doctors = doctorMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Doctor>()
                        .eq(Doctor::getDepartmentId, departmentId)
                        .eq(Doctor::getStatus, 1));
        List<QueueNumber> all = new ArrayList<>();
        for (var d : doctors) {
            all.addAll(queueNumberMapper.findTodayQueue(d.getUserId()));
        }
        return all;
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
        QueueNumber qn = requireOwnership(appointmentId);
        if (qn.getStatus() != 1) throw BusinessException.badRequest("仅已叫号可标记就诊");
        qn.setStatus(3);
        queueNumberMapper.updateById(qn);

        Appointment appt = appointmentMapper.selectById(appointmentId);
        if (appt != null && appt.getStatus() == 1) {
            appt.setStatus(2);
            appointmentMapper.updateById(appt);
        }
        return qn;
    }

    @Transactional
    public QueueNumber markMissed(Long appointmentId) {
        QueueNumber qn = requireOwnership(appointmentId);
        if (qn.getStatus() != 1) throw BusinessException.badRequest("仅已叫号可标记过号");
        qn.setStatus(2);
        queueNumberMapper.updateById(qn);
        return qn;
    }

    @Transactional
    public QueueNumber markWaitAgain(Long appointmentId) {
        QueueNumber qn = requireOwnership(appointmentId);
        if (qn.getStatus() != 2) throw BusinessException.badRequest("仅过号可重新排队");
        qn.setStatus(0);
        qn.setCallTime(null);
        queueNumberMapper.updateById(qn);
        return qn;
    }

    private QueueNumber requireOwnership(Long appointmentId) {
        QueueNumber qn = findOrThrow(appointmentId);
        String role = UserContext.getRole();
        Long userId = UserContext.getUserId();

        if ("DOCTOR".equals(role)) {
            if (!qn.getDoctorId().equals(userId))
                throw BusinessException.forbidden("无权操作该队列记录");
            return qn;
        }
        if ("DEPT_ADMIN".equals(role)) {
            Long deptId = UserContext.getDepartmentId();
            if (deptId == null) throw BusinessException.forbidden("未绑定科室");
            Doctor doctor = doctorMapper.selectDetailById(qn.getDoctorId());
            if (doctor == null || !deptId.equals(doctor.getDepartmentId()))
                throw BusinessException.forbidden("无权操作该队列记录");
            return qn;
        }
        // SYS_ADMIN: allow
        return qn;
    }

    public void addToQueue(Long appointmentId, Long doctorId, java.time.LocalDate workDate) {
        queueNumberMapper.insertWithNextNumber(appointmentId, doctorId, workDate);
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
