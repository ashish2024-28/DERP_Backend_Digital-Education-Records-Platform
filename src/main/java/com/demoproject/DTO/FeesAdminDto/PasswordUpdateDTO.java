package com.demoproject.DTO.FeesAdminDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordUpdateDTO {
    @NotBlank
    private String newPassword;
}
