package com.demoproject.DTO.University;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UniversityNameDomainLogoPathDTO {

    private String universityName;
    private String domain;
    private String universityLogoPath;
}
