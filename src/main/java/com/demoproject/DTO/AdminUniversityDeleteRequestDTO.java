package com.demoproject.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUniversityDeleteRequestDTO {

    @NotBlank
    private String domain;

    @NotBlank
    private String confirmation;

    @NotBlank
    @Size(min = 10, max = 1000)
    private String reason;

    @NotBlank
    @Pattern(regexp = "\\d{6}")
    private String otp;
}
