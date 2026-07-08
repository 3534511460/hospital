package com.hospital.appointment.module.medical.controller;

import com.hospital.appointment.common.annotation.Log;
import com.hospital.appointment.common.annotation.RequireRole;
import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.common.result.R;
import com.hospital.appointment.module.appointment.mapper.AppointmentMapper;
import com.hospital.appointment.module.appointment.model.Appointment;
import com.hospital.appointment.module.medical.model.Evaluation;
import com.hospital.appointment.module.medical.model.MedicalRecord;
import com.hospital.appointment.module.medical.model.Prescription;
import com.hospital.appointment.module.medical.service.MedicalRecordService;
import com.hospital.appointment.module.message.service.SiteMessageService;
import com.hospital.appointment.module.user.mapper.SysUserMapper;
import com.hospital.appointment.module.user.model.SysUser;
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
    private final AppointmentMapper appointmentMapper;
    private final SiteMessageService messageService;
    private final SysUserMapper userMapper;

    @PostMapping("/records")
    @RequireRole("DOCTOR")
    @Log(module = "病历", operation = "创建病历")
    public R<MedicalRecord> createRecord(@RequestBody MedicalRecord record) {
        record.setDoctorId(UserContext.getUserId());
        MedicalRecord created = recordService.createRecord(record);
        SysUser doctor = userMapper.selectById(UserContext.getUserId());
        String docName = doctor != null ? doctor.getRealName() : "医生";
        messageService.sendSystemMessage(record.getPatientId(), "新病历",
            docName + " 已为您创建了就诊病历，请查看。");
        return R.ok(created);
    }

    @PutMapping("/records/{id}")
    @RequireRole("DOCTOR")
    public R<MedicalRecord> updateRecord(@PathVariable Long id, @RequestBody MedicalRecord record) {
        record.setId(id);
        return R.ok(recordService.updateRecord(record));
    }

    @GetMapping("/records/appointment/{appointmentId}")
    public R<MedicalRecord> getByAppointment(@PathVariable Long appointmentId) {
        Appointment a = appointmentMapper.selectById(appointmentId);
        if (a == null) throw BusinessException.notFound("预约不存在");
        String role = UserContext.getRole();
        Long userId = UserContext.getUserId();
        if ("PATIENT".equals(role) && !a.getPatientId().equals(userId))
            throw BusinessException.forbidden("无权查看他人病历");
        if ("DOCTOR".equals(role) && !a.getDoctorId().equals(userId))
            throw BusinessException.forbidden("无权查看其他医生的病历");
        if ("DEPT_ADMIN".equals(role)) {
            Long deptId = UserContext.getDepartmentId();
            if (deptId == null || !deptId.equals(a.getDepartmentId()))
                throw BusinessException.forbidden("无权查看其他科室的病历");
        }
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
        Prescription created = prescriptionService.addPrescription(prescription);
        messageService.sendSystemMessage(prescription.getPatientId(), "新处方",
            "医生已为您开具处方（" + created.getMedicationName() + "），请查看。");
        return R.ok(created);
    }

    @GetMapping("/prescriptions/record/{recordId}")
    public R<?> prescriptions(@PathVariable Long recordId) {
        return R.ok(prescriptionService.getByMedicalRecordId(recordId));
    }
}
