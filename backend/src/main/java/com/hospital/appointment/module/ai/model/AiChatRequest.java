package com.hospital.appointment.module.ai.model;

import lombok.Data;

@Data
public class AiChatRequest {
    private String message;
    private String sessionId;
    private String type;
}
