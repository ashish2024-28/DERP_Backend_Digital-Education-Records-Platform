package com.demoproject.DTO.University;


import com.demoproject.DTO.DomainAdminDTO.DomainAdminResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// lombok auto create getter, setter ,constructor,..etc 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniversityAndDomainAdminResponseDTO {

    private UniversityResponseDTO universityResponseDTO;

    private DomainAdminResponseDTO domainAdminResponseDTO;

}

