package com.demoproject.DTO.FeesAccount;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeesPaymentReviewDTO {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String rollNumber;

    @NotBlank
    private String upiTransactionId;

    private String rejectionReason;
}
