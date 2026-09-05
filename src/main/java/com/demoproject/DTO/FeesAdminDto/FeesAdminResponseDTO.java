package com.demoproject.DTO.FeesAdminDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeesAdminResponseDTO {

    private String name;
    private String email;
    private String mobileNumber;

    private LocalDateTime createdDateTime;
    private LocalDateTime lastUpdateDateTime; // For login purposes
    private LocalDateTime lastLoginDateTime; // For login purposes

    private String profilePic;// store image path OR base64


    private String feesAdminId;


    private String universityName;
}