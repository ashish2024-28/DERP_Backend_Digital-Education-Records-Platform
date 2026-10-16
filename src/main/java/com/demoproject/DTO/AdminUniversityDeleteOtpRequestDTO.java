package com.demoproject.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUniversityDeleteOtpRequestDTO {

    @NotBlank
    private String domain;

    @NotBlank
    @Size(min = 10, max = 1000)
    private String reason;
}
