package com.hospital.appointment.module.medical.service.impl;

import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.module.appointment.mapper.AppointmentMapper;
import com.hospital.appointment.module.appointment.model.Appointment;
import com.hospital.appointment.module.medical.mapper.MedicalRecordMapper;
import com.hospital.appointment.module.medical.model.MedicalRecord;
import com.hospital.appointment.module.medical.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordMapper recordMapper;
    private final AppointmentMapper appointmentMapper;

    @Override
    @Transactional
    public MedicalRecord createRecord(MedicalRecord record) {
        Appointment appt = appointmentMapper.selectById(record.getAppointmentId());
        if (appt == null) throw BusinessException.notFound("预约不存在");
        if (appt.getStatus() != 2)
            throw BusinessException.badRequest("仅就诊完成的预约可创建病历");

        MedicalRecord exist = recordMapper.findByAppointmentId(record.getAppointmentId());
        if (exist != null) throw BusinessException.badRequest("该预约已有病历，请使用修改接口");

        recordMapper.insert(record);
        return record;
    }

    @Override
    public MedicalRecord updateRecord(MedicalRecord record) {
        MedicalRecord exist = recordMapper.selectById(record.getId());
        if (exist == null) throw BusinessException.notFound("病历不存在");
        recordMapper.updateById(record);
        return record;
    }

    @Override
    public MedicalRecord getByAppointmentId(Long appointmentId) {
        MedicalRecord record = recordMapper.findByAppointmentId(appointmentId);
        if (record == null) throw BusinessException.notFound("病历不存在");
        return record;
    }

    @Override
    public List<MedicalRecord> getPatientRecords(Long patientId) {
        return recordMapper.findByPatientId(patientId);
    }
}
