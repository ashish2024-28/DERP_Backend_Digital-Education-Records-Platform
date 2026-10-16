package com.demoproject.DTO;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUniversityResponseDTO {

    private Long id;
    private String domain;
    private String permanentId;
    private String institutionName;
    private String universityName;
    private String institutionType;
    private String establishmentYear;
    private String address;
    private String state;
    private String email;
    private String mobileNumber;
    private String universityLogoPath;
    private LocalDateTime createdDateTime;
    private LocalDateTime lastUpdateDateTime;
    private String domainAdminName;
    private String domainAdminEmail;
    private String domainAdminMobileNumber;
}
