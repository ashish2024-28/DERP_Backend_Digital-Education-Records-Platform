package com.demoproject.DTO.FeesAccount;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

//payment method types.
public class FeesPaymentOnlineDTO {


 @NotBlank
 private String displayName;

 private String bankName;

 @NotBlank
 private String upiId;

 private String instructions;


}
