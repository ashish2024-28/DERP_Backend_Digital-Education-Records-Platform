package com.demoproject.DTO.FeesAccount;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeesPaymentQueueResponseDTO {

    private String studentName;
    private String email;
    private String rollNumber;
    private String academicSession;
    private Double amount;
    private String upiTransactionId;
    private String paymentType;
    private String status;
    private String rejectionReason;
}
