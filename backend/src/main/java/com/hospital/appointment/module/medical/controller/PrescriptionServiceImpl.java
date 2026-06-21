package com.hospital.appointment.module.medical.controller;

import com.hospital.appointment.module.medical.mapper.PrescriptionMapper;
import com.hospital.appointment.module.medical.model.Prescription;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrescriptionServiceImpl {

    private final PrescriptionMapper prescriptionMapper;

    public PrescriptionServiceImpl(PrescriptionMapper prescriptionMapper) {
        this.prescriptionMapper = prescriptionMapper;
    }

    public Prescription addPrescription(Prescription prescription) {
        prescriptionMapper.insert(prescription);
        return prescription;
    }

    public List<Prescription> getByMedicalRecordId(Long recordId) {
        return prescriptionMapper.findByMedicalRecordId(recordId);
    }
}
