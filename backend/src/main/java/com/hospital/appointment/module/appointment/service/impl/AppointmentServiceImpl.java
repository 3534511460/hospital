package com.hospital.appointment.module.appointment.service.impl;

import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.module.appointment.mapper.AppointmentMapper;
import com.hospital.appointment.module.appointment.mapper.DoctorScheduleMapper;
import com.hospital.appointment.module.appointment.mapper.NoShowRecordMapper;
import com.hospital.appointment.module.appointment.model.Appointment;
import com.hospital.appointment.module.appointment.model.DoctorSchedule;
import com.hospital.appointment.module.appointment.model.NoShowRecord;
import com.hospital.appointment.module.appointment.service.AppointmentService;
import com.hospital.appointment.module.appointment.service.QueueService;
import com.hospital.appointment.module.hospital.mapper.DoctorMapper;
import com.hospital.appointment.module.hospital.model.Doctor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final DoctorScheduleMapper scheduleMapper;
    private final NoShowRecordMapper noShowRecordMapper;
    private final DoctorMapper doctorMapper;
    private final QueueService queueService;

    @Override
    @Transactional
    public Appointment bookAppointment(Long patientId, Long doctorId, Long scheduleId, String symptomDesc, Long companionId) {
        LocalDate blockDate = noShowRecordMapper.getActiveBlockDate(patientId);
        if (blockDate != null)
            throw BusinessException.forbidden("您因爽约已被限制预约至 " + blockDate);

        DoctorSchedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) throw BusinessException.notFound("排班不存在");
        if (schedule.getBookedCount() >= schedule.getMaxCount())
            throw BusinessException.badRequest("该时段号源已满");
        if (schedule.getWorkDate().isBefore(LocalDate.now()))
            throw BusinessException.badRequest("不能预约过去的日期");

        schedule.setBookedCount(schedule.getBookedCount() + 1);
        if (schedule.getBookedCount() >= schedule.getMaxCount()) schedule.setStatus(2);
        scheduleMapper.updateById(schedule);

        Doctor doctor = doctorMapper.selectDetailById(doctorId);

        Appointment appointment = new Appointment();
        appointment.setAppointmentNo("APT" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%06d", (int) (Math.random() * 999999)));
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
        return appointment;
    }

    @Override
    @Transactional
    public void cancelAppointment(Long appointmentId, Long userId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) throw BusinessException.notFound("预约不存在");
        if (!appointment.getPatientId().equals(userId))
            throw BusinessException.forbidden("无权取消他人预约");
        if (appointment.getStatus() != 1)
            throw BusinessException.badRequest("当前状态不可取消");
        if (appointment.getAppointmentDate().minusDays(1).isBefore(LocalDate.now()))
            throw BusinessException.badRequest("就诊前24小时内不可取消");

        appointment.setStatus(3);
        appointment.setCancelTime(LocalDateTime.now());
        appointmentMapper.updateById(appointment);

        DoctorSchedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
        if (schedule != null && schedule.getBookedCount() > 0) {
            schedule.setBookedCount(schedule.getBookedCount() - 1);
            if (schedule.getStatus() == 2) schedule.setStatus(1);
            scheduleMapper.updateById(schedule);
        }
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
    public void markNoShow(Long appointmentId) {
        Appointment a = appointmentMapper.selectById(appointmentId);
        if (a == null) throw BusinessException.notFound("预约不存在");
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
