package com.demoproject.DTO.DomainAdminDTO;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DomainAdminResponseDTO {

    private Long id;
    private String name;
    private String domain;
    private String mobileNumber;
    private String email;
    private String role;

    private Instant createdDateTime;
    private Instant lastLoginDateTime;
    
    // University details (Flattened or nested)
    private String universityName;
    private Long universityId;

}
