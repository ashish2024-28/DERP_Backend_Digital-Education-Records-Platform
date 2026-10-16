package com.demoproject.DTO.FeesAccount;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter

//        submitted payments by students with proof, UPI/reference, status
public class StudentFeesPaymentDTO {

 private String academicSession;

 @NotNull
 @DecimalMin(value = "0.01")
 private Double amount;

 @NotBlank
 private String upiTransactionId;

 @NotBlank
 @Pattern(regexp = "BANK|ONLINE", flags = Pattern.Flag.CASE_INSENSITIVE)
 private String paymentType;

 private String status;

 private String rejectionReason;



}
