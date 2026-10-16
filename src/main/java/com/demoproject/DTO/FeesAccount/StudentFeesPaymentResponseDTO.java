package com.demoproject.DTO.FeesAccount;


import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;


@Getter @Setter

//  submitted payments by students with proof, UPI/reference, status
public class StudentFeesPaymentResponseDTO {


 private String academicSession;

 private Double amount;

 private String upiTransactionId;

 private String paymentType;

 private String status;

 private String rejectionReason;

 private String verifyByFeesAdmin;

 private LocalDateTime submittedAt;

 private LocalDateTime verifiedAt;

}
