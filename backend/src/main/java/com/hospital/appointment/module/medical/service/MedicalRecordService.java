package com.hospital.appointment.module.medical.service;

import com.hospital.appointment.module.medical.model.MedicalRecord;

import java.util.List;

public interface MedicalRecordService {
    MedicalRecord createRecord(MedicalRecord record);
    MedicalRecord updateRecord(MedicalRecord record);
    MedicalRecord getByAppointmentId(Long appointmentId);
    List<MedicalRecord> getPatientRecords(Long patientId);
}
