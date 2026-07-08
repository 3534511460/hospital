package com.hospital.appointment.module.appointment.service.impl;

import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.module.appointment.mapper.AppointmentMapper;
import com.hospital.appointment.module.appointment.mapper.DoctorScheduleMapper;
import com.hospital.appointment.module.appointment.mapper.NoShowRecordMapper;
import com.hospital.appointment.module.appointment.mapper.QueueNumberMapper;
import com.hospital.appointment.module.appointment.model.Appointment;
import com.hospital.appointment.module.appointment.model.DoctorSchedule;
import com.hospital.appointment.module.appointment.model.NoShowRecord;
import com.hospital.appointment.module.appointment.model.QueueNumber;
import com.hospital.appointment.module.appointment.service.AppointmentService;
import com.hospital.appointment.module.appointment.service.QueueService;
import com.hospital.appointment.module.hospital.mapper.DoctorMapper;
import com.hospital.appointment.module.hospital.model.Doctor;
import com.hospital.appointment.module.message.service.SiteMessageService;
import com.hospital.appointment.module.user.mapper.SysUserMapper;
import com.hospital.appointment.module.user.model.SysUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final DoctorScheduleMapper scheduleMapper;
    private final NoShowRecordMapper noShowRecordMapper;
    private final DoctorMapper doctorMapper;
    private final QueueService queueService;
    private final QueueNumberMapper queueNumberMapper;
    private final SiteMessageService messageService;
    private final SysUserMapper userMapper;

    @Override
    @Transactional
    public Appointment bookAppointment(Long patientId, Long doctorId, Long scheduleId, String symptomDesc, Long companionId) {
        LocalDate blockDate = noShowRecordMapper.getActiveBlockDate(patientId);
        if (blockDate != null)
            throw BusinessException.forbidden("您因爽约已被限制预约至 " + blockDate);

        DoctorSchedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) throw BusinessException.notFound("排班不存在");
        if (!schedule.getDoctorId().equals(doctorId))
            throw BusinessException.badRequest("排班不属于该医生");

        int updated = scheduleMapper.incrementBookedCount(scheduleId);
        if (updated == 0) throw BusinessException.badRequest("该时段号源已满或不可预约");

        Doctor doctor = doctorMapper.selectDetailById(doctorId);

        Appointment appointment = new Appointment();
        appointment.setAppointmentNo("APT" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        appointment.setPatientId(patientId);
        appointment.setDoctorId(doctorId);
        appointment.setScheduleId(scheduleId);
        appointment.setDepartmentId(doctor != null ? doctor.getDepartmentId() : null);
        appointment.setAppointmentDate(schedule.getWorkDate());
        appointment.setTimeSlot(schedule.getTimeSlot());
        appointment.setSymptomDesc(symptomDesc);
        appointment.setCompanionId(companionId);
        appointment.setStatus(1);
        appointment.setFee(doctor != null ? doctor.getConsultationFee() : null);
        appointmentMapper.insert(appointment);
        queueService.addToQueue(appointment.getId(), doctorId);
        log.info("预约成功: {} 患者={} 医生={}", appointment.getAppointmentNo(), patientId, doctorId);

        // Notify doctor of new appointment
        SysUser patient = userMapper.selectById(patientId);
        String pName = patient != null ? patient.getRealName() : "患者";
        String dateInfo = schedule.getWorkDate() + " " + schedule.getTimeSlot();
        messageService.sendSystemMessage(doctorId, "新预约通知",
            pName + " 已预约您的 " + dateInfo + " 门诊，请留意。");

        return appointment;
    }

    @Override
    @Transactional
    public void cancelAppointment(Long appointmentId, Long userId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) throw BusinessException.notFound("预约不存在");
        if (!appointment.getPatientId().equals(userId))
            throw BusinessException.forbidden("无权取消他人预约");

        int updated = appointmentMapper.cancelAppointmentAtomic(appointmentId);
        if (updated == 0)
            throw BusinessException.badRequest("当前状态不可取消或已超过取消时限");

        scheduleMapper.decrementBookedCount(appointment.getScheduleId());

        var qns = queueNumberMapper.selectList(
                new LambdaQueryWrapper<QueueNumber>().eq(QueueNumber::getAppointmentId, appointmentId));
        for (var qn : qns) queueNumberMapper.deleteById(qn.getId());

        SysUser patient = userMapper.selectById(userId);
        String pName = patient != null ? patient.getRealName() : "患者";
        String dateInfo = appointment.getAppointmentDate() + " " + appointment.getTimeSlot();
        messageService.sendSystemMessage(appointment.getDoctorId(), "预约取消",
            pName + " 已取消 " + dateInfo + " 的预约。");
    }

    @Override
    public List<Appointment> getPatientAppointments(Long patientId, Integer status) {
        List<Appointment> list = appointmentMapper.findByPatientId(patientId);
        if (status != null) list.removeIf(a -> !a.getStatus().equals(status));
        return list;
    }

    @Override
    public List<Appointment> getDoctorAppointments(Long doctorId, Integer status, LocalDate date) {
        List<Appointment> list = appointmentMapper.findByDoctorId(doctorId);
        if (status != null) list.removeIf(a -> !a.getStatus().equals(status));
        if (date != null) list.removeIf(a -> !a.getAppointmentDate().equals(date));
        return list;
    }

    @Override
    public void confirmAppointment(Long appointmentId, Long doctorId) {
        Appointment a = validateOwnership(appointmentId, doctorId);
        if (a.getStatus() != 1) throw BusinessException.badRequest("当前状态不可确认");
        a.setStatus(1);
        appointmentMapper.updateById(a);
    }

    @Override
    public void completeAppointment(Long appointmentId, Long doctorId) {
        Appointment a = validateOwnership(appointmentId, doctorId);
        if (a.getStatus() != 1) throw BusinessException.badRequest("当前状态不可完成");
        a.setStatus(2);
        appointmentMapper.updateById(a);
    }

    @Override
    @Transactional
    public void markNoShow(Long appointmentId, Long doctorId) {
        Appointment a = validateOwnership(appointmentId, doctorId);
        if (a.getStatus() != 1) throw BusinessException.badRequest("当前状态不能标记爽约");
        a.setStatus(4);
        appointmentMapper.updateById(a);

        NoShowRecord record = new NoShowRecord();
        record.setUserId(a.getPatientId());
        record.setAppointmentId(appointmentId);
        record.setNoShowDate(a.getAppointmentDate());
        noShowRecordMapper.insert(record);

        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        int count = noShowRecordMapper.countRecentNoShows(a.getPatientId(), thirtyDaysAgo);
        if (count >= 3) {
            LocalDate blocked = LocalDate.now().plusDays(30);
            record.setBlockedUntil(blocked);
            noShowRecordMapper.updateById(record);
            log.warn("患者{}累计爽约{}次, 封禁至{}", a.getPatientId(), count, blocked);
        }
    }

    @Override
    public boolean checkNoShowBlocked(Long userId) {
        return noShowRecordMapper.getActiveBlockDate(userId) != null;
    }

    @Override
    public Appointment getAppointmentDetail(Long appointmentId) {
        Appointment a = appointmentMapper.selectById(appointmentId);
        if (a == null) throw BusinessException.notFound("预约不存在");
        return a;
    }

    private Appointment validateOwnership(Long appointmentId, Long doctorId) {
        Appointment a = appointmentMapper.selectById(appointmentId);
        if (a == null) throw BusinessException.notFound("预约不存在");
        if (!a.getDoctorId().equals(doctorId)) throw BusinessException.forbidden("无权操作");
        return a;
    }
}
