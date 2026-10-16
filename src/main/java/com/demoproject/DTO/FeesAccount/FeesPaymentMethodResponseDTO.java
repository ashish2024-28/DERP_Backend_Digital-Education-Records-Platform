package com.demoproject.DTO.FeesAccount;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeesPaymentMethodResponseDTO {

    private Long id;
    private String paymentType;
    private String displayName;
    private String bankName;
    private String accountName;
    private String accountNumber;
    private String ifsc;
    private String branch;
    private String upiId;
    private String qrImagePath;
    private String instructions;
    private String feesAdminEmail;
    private boolean active;
}
