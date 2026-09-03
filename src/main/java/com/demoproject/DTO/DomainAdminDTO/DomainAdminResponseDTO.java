package com.demoproject.DTO.DomainAdminDTO;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DomainAdminResponseDTO {

    private String name;
    private String domain;
    private String mobileNumber;
    private String email;

    private Instant createdDateTime;
    private Instant lastLoginDateTime;

    private String profilePic;// store image path OR base64


}
