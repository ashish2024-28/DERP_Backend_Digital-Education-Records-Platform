package com.demoproject.DTO.FeesAdminDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeesAdminResponseDTO {

    private String name;
    private String email;
    private String mobileNumber;

    private Instant createdDateTime;
    private Instant lastLoginDateTime; // For login purposes

    private String profilePic;// store image path OR base64


    private String feesAdminId;

    private String password; // Used only see by admin

    private String universityName;
}