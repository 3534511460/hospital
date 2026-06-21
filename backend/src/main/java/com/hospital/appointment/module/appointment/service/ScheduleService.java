package com.hospital.appointment.module.appointment.service;

import com.hospital.appointment.module.appointment.model.DoctorSchedule;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {
    List<DoctorSchedule> getAvailableSchedules(Long doctorId, LocalDate startDate, LocalDate endDate);
    List<DoctorSchedule> getDoctorScheduleDetail(Long doctorId);
    DoctorSchedule addSchedule(DoctorSchedule schedule);
    DoctorSchedule updateSchedule(DoctorSchedule schedule);
    void deleteSchedule(Long id);
    void batchCreateSchedules(List<DoctorSchedule> schedules);
    List<DoctorSchedule> getSchedulesByDateRange(Long doctorId, LocalDate start, LocalDate end);
}
