package com.demoproject.DTO.FeesAccount;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

//payment method BANK.
public class FeesPaymentBankDTO {


 @NotBlank
 private String displayName;
 @NotBlank
 private String bankName;
 @NotBlank
 private String accountName;
 @NotBlank
 private String accountNumber;
 @NotBlank
 private String ifsc;
 private String branch;


 private String instructions;


}
