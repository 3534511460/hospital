package com.hospital.appointment.module.medical.controller;

import com.hospital.appointment.common.annotation.Log;
import com.hospital.appointment.common.annotation.RequireRole;
import com.hospital.appointment.common.result.R;
import com.hospital.appointment.module.medical.model.Evaluation;
import com.hospital.appointment.module.medical.model.MedicalRecord;
import com.hospital.appointment.module.medical.model.Prescription;
import com.hospital.appointment.module.medical.service.MedicalRecordService;
import com.hospital.appointment.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/medical")
@RequiredArgsConstructor
public class MedicalController {

    private final MedicalRecordService recordService;
    private final EvaluationServiceImpl evalService;
    private final PrescriptionServiceImpl prescriptionService;

    @PostMapping("/records")
    @RequireRole("DOCTOR")
    @Log(module = "病历", operation = "创建病历")
    public R<MedicalRecord> createRecord(@RequestBody MedicalRecord record) {
        record.setDoctorId(UserContext.getUserId());
        return R.ok(recordService.createRecord(record));
    }

    @PutMapping("/records/{id}")
    @RequireRole("DOCTOR")
    public R<MedicalRecord> updateRecord(@PathVariable Long id, @RequestBody MedicalRecord record) {
        record.setId(id);
        return R.ok(recordService.updateRecord(record));
    }

    @GetMapping("/records/appointment/{appointmentId}")
    public R<MedicalRecord> getByAppointment(@PathVariable Long appointmentId) {
        return R.ok(recordService.getByAppointmentId(appointmentId));
    }

    @GetMapping("/records/patient")
    public R<?> patientRecords() {
        return R.ok(recordService.getPatientRecords(UserContext.getUserId()));
    }

    @PostMapping("/evaluations")
    @RequireRole("PATIENT")
    @Log(module = "评价", operation = "发表评价")
    public R<Evaluation> addEvaluation(@RequestBody Evaluation evaluation) {
        evaluation.setPatientId(UserContext.getUserId());
        return R.ok(evalService.addEvaluation(evaluation));
    }

    @GetMapping("/evaluations/doctor/{doctorId}")
    public R<?> doctorEvaluations(@PathVariable Long doctorId) {
        return R.ok(evalService.getDoctorEvaluations(doctorId));
    }

    @GetMapping("/evaluations/my")
    public R<?> myEvaluations() {
        return R.ok(evalService.getMyEvaluations(UserContext.getUserId()));
    }

    @PostMapping("/prescriptions")
    @RequireRole("DOCTOR")
    @Log(module = "处方", operation = "开具处方")
    public R<Prescription> addPrescription(@RequestBody Prescription prescription) {
        prescription.setDoctorId(UserContext.getUserId());
        return R.ok(prescriptionService.addPrescription(prescription));
    }

    @GetMapping("/prescriptions/record/{recordId}")
    public R<?> prescriptions(@PathVariable Long recordId) {
        return R.ok(prescriptionService.getByMedicalRecordId(recordId));
    }
}
