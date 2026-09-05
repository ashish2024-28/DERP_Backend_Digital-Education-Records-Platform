package com.demoproject.DTO.DomainAdminDTO;

import java.time.LocalDateTime;

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

    private LocalDateTime createdDateTime;
    private LocalDateTime lastUpdateDateTime;
    private LocalDateTime lastLoginDateTime;

    private String profilePic;// store image path OR base64


}
