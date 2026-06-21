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
        // Parse time slot "08:00-09:00" -> start_time, end_time
        if (schedule.getTimeSlot() != null && schedule.getStartTime() == null) {
            String[] parts = schedule.getTimeSlot().split("-");
            if (parts.length == 2) {
                schedule.setStartTime(java.time.LocalTime.parse(parts[0].trim()));
                schedule.setEndTime(java.time.LocalTime.parse(parts[1].trim()));
            }
        }
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

    @Override
    public List<DoctorSchedule> getAllSchedules() {
        return scheduleMapper.selectList(
            new LambdaQueryWrapper<DoctorSchedule>()
                .orderByDesc(DoctorSchedule::getWorkDate)
                .orderByAsc(DoctorSchedule::getStartTime)
        );
    }

    @Override
    public List<DoctorSchedule> getAllWithDetails() {
        return scheduleMapper.getAllWithDetails();
    }

    @Override
    public List<DoctorSchedule> getWeekSchedules(Long doctorId, LocalDate start, LocalDate end) {
        return scheduleMapper.getWeekSchedules(doctorId, start, end);
    }

    @Override
    @Transactional
    public int copyLastWeekSchedules(Long doctorId, LocalDate targetMonday) {
        LocalDate lastMon = targetMonday.minusDays(7);
        LocalDate lastSun = targetMonday.minusDays(1);
        LocalDate thisSun = targetMonday.plusDays(6);

        List<DoctorSchedule> lastWeek = scheduleMapper.getWeekSchedules(doctorId, lastMon, lastSun);
        int count = 0;
        for (DoctorSchedule s : lastWeek) {
            LocalDate newDate = s.getWorkDate().plusDays(java.time.temporal.ChronoUnit.DAYS.between(lastMon, targetMonday));
            s.setId(null);
            s.setWorkDate(newDate);
            s.setBookedCount(0);
            s.setStatus(1);
            LambdaQueryWrapper<DoctorSchedule> w = new LambdaQueryWrapper<>();
            w.eq(DoctorSchedule::getDoctorId, s.getDoctorId())
             .eq(DoctorSchedule::getWorkDate, s.getWorkDate())
             .eq(DoctorSchedule::getTimeSlot, s.getTimeSlot());
            if (scheduleMapper.selectCount(w) == 0) {
                scheduleMapper.insert(s);
                count++;
            }
        }
        return count;
    }
}
