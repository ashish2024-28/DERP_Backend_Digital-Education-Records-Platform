package com.demoproject.DTO.University;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UniversityResponseDTO {
    private Long id;

    private String domain;
    private String permanentId;
    private String institutionName; // Name of the Institution (may be same)
    private String universityName; // Name of the University (may be same)
    private String institutionType; //   (private ,State )
    private String establishmentYear;
    private String address; // Address same as Institution
    private String email;
    private String mobileNumber;
    private LocalDateTime createdDateTime ; // date and time when create account
    private String universityLogoPath; // Stores "alex_profile.png"



    
}
