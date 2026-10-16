package com.demoproject.DTO.FeesAccount;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeesPaymentActionDTO {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String rollNumber;

    @NotNull
    @DecimalMin(value = "0.01")
    private Double amount;
}
