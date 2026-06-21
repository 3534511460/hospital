package com.hospital.appointment.module.appointment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.module.appointment.mapper.DoctorScheduleMapper;
import com.hospital.appointment.module.appointment.model.DoctorSchedule;
import com.hospital.appointment.module.appointment.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final DoctorScheduleMapper scheduleMapper;

    @Override
    public List<DoctorSchedule> getAvailableSchedules(Long doctorId, LocalDate startDate, LocalDate endDate) {
        return scheduleMapper.getAvailableSchedules(doctorId, startDate, endDate);
    }

    @Override
    public List<DoctorSchedule> getDoctorScheduleDetail(Long doctorId) {
        return scheduleMapper.getDoctorScheduleWithDetails(doctorId);
    }

    @Override
    public DoctorSchedule addSchedule(DoctorSchedule schedule) {
        LambdaQueryWrapper<DoctorSchedule> w = new LambdaQueryWrapper<>();
        w.eq(DoctorSchedule::getDoctorId, schedule.getDoctorId())
         .eq(DoctorSchedule::getWorkDate, schedule.getWorkDate())
         .eq(DoctorSchedule::getTimeSlot, schedule.getTimeSlot());
        if (scheduleMapper.selectCount(w) > 0)
            throw BusinessException.badRequest("该时段已存在排班");
        if (schedule.getStatus() == null) schedule.setStatus(1);
        if (schedule.getBookedCount() == null) schedule.setBookedCount(0);
        scheduleMapper.insert(schedule);
        return schedule;
    }

    @Override
    public DoctorSchedule updateSchedule(DoctorSchedule schedule) {
        DoctorSchedule exist = scheduleMapper.selectById(schedule.getId());
        if (exist == null) throw BusinessException.notFound("排班不存在");
        if (exist.getBookedCount() > 0)
            throw BusinessException.badRequest("已有患者预约，无法修改");
        scheduleMapper.updateById(schedule);
        return schedule;
    }

    @Override
    public void deleteSchedule(Long id) {
        DoctorSchedule exist = scheduleMapper.selectById(id);
        if (exist == null) throw BusinessException.notFound("排班不存在");
        if (exist.getBookedCount() > 0)
            throw BusinessException.badRequest("已有患者预约，无法删除");
        scheduleMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchCreateSchedules(List<DoctorSchedule> schedules) {
        for (DoctorSchedule s : schedules) addSchedule(s);
    }

    @Override
    public List<DoctorSchedule> getSchedulesByDateRange(Long doctorId, LocalDate start, LocalDate end) {
        LambdaQueryWrapper<DoctorSchedule> w = new LambdaQueryWrapper<>();
        w.eq(DoctorSchedule::getDoctorId, doctorId)
         .between(DoctorSchedule::getWorkDate, start, end)
         .orderByAsc(DoctorSchedule::getWorkDate)
         .orderByAsc(DoctorSchedule::getStartTime);
        return scheduleMapper.selectList(w);
    }
}
