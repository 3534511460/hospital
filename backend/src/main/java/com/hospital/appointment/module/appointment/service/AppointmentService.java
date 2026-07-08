package com.hospital.appointment.module.appointment.service;

import com.hospital.appointment.module.appointment.model.Appointment;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {
    Appointment bookAppointment(Long patientId, Long doctorId, Long scheduleId, String symptomDesc, Long companionId);
    void cancelAppointment(Long appointmentId, Long userId);
    List<Appointment> getPatientAppointments(Long patientId, Integer status);
    List<Appointment> getDoctorAppointments(Long doctorId, Integer status, LocalDate date);
    void confirmAppointment(Long appointmentId, Long doctorId);
    void completeAppointment(Long appointmentId, Long doctorId);
    void markNoShow(Long appointmentId, Long doctorId);
    boolean checkNoShowBlocked(Long userId);
    Appointment getAppointmentDetail(Long appointmentId);
}
