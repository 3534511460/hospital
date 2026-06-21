package com.hospital.appointment.module.medical.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.module.appointment.mapper.AppointmentMapper;
import com.hospital.appointment.module.appointment.model.Appointment;
import com.hospital.appointment.module.hospital.mapper.DoctorMapper;
import com.hospital.appointment.module.hospital.model.Doctor;
import com.hospital.appointment.module.medical.mapper.EvaluationMapper;
import com.hospital.appointment.module.medical.model.Evaluation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EvaluationServiceImpl {

    private final EvaluationMapper evaluationMapper;
    private final AppointmentMapper appointmentMapper;
    private final DoctorMapper doctorMapper;

    public EvaluationServiceImpl(EvaluationMapper evaluationMapper, AppointmentMapper appointmentMapper, DoctorMapper doctorMapper) {
        this.evaluationMapper = evaluationMapper;
        this.appointmentMapper = appointmentMapper;
        this.doctorMapper = doctorMapper;
    }

    @Transactional
    public Evaluation addEvaluation(Evaluation evaluation) {
        Appointment appt = appointmentMapper.selectById(evaluation.getAppointmentId());
        if (appt == null) throw BusinessException.notFound("预约不存在");
        if (appt.getStatus() != 2) throw BusinessException.badRequest("仅就诊完成可评价");
        if (!appt.getPatientId().equals(evaluation.getPatientId()))
            throw BusinessException.forbidden("无权评价");

        var exist = evaluationMapper.selectList(
                new LambdaQueryWrapper<Evaluation>()
                        .eq(Evaluation::getAppointmentId, evaluation.getAppointmentId()));
        if (!exist.isEmpty()) throw BusinessException.badRequest("已评价过该预约");

        if (evaluation.getIsAnonymous() == null) evaluation.setIsAnonymous(0);
        evaluationMapper.insert(evaluation);

        Double avg = evaluationMapper.avgRatingByDoctorId(evaluation.getDoctorId());
        if (avg != null) {
            Doctor doctor = doctorMapper.selectById(evaluation.getDoctorId());
            if (doctor != null) {
                doctor.setAvgRating(new java.math.BigDecimal(avg));
                doctor.setRatingCount(doctor.getRatingCount() != null ? doctor.getRatingCount() + 1 : 1);
                doctorMapper.updateById(doctor);
            }
        }
        return evaluation;
    }

    public List<Evaluation> getDoctorEvaluations(Long doctorId) {
        return evaluationMapper.findByDoctorId(doctorId);
    }

    public List<Evaluation> getMyEvaluations(Long patientId) {
        var wrapper = new LambdaQueryWrapper<Evaluation>()
                .eq(Evaluation::getPatientId, patientId)
                .orderByDesc(Evaluation::getCreateTime);
        return evaluationMapper.selectList(wrapper);
    }
}
