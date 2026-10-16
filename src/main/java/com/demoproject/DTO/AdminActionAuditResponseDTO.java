package com.demoproject.DTO;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminActionAuditResponseDTO {

    private String adminEmail;
    private String eventType;
    private String universityDomain;
    private String reason;
    private LocalDateTime createdAt;
}
