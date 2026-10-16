package com.demoproject.DTO.FeesAccount;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeesPaymentHistoryResponseDTO {

    private Long id;
    private String studentName;
    private String email;
    private String rollNumber;
    private String academicSession;
    private Double amount;
    private String upiTransactionId;
    private String paymentType;
    private String status;
    private String rejectionReason;
    private String verifiedByFeesAdminEmail;
    private LocalDateTime verifiedAt;
}
