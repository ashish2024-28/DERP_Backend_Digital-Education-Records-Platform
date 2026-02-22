package com.demoproject.DTO.SubAdminDTO;

import java.time.Instant;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubAdminResponseDTO {
   // Fields from BaseUser
    private String name;
    private String email;
    private String password;
    private String mobileNumber;

    // Fields specific to SubAdmin
    private String subAdminId;
    private String course;
    private Instant createdDateTime;
    private Instant lastLoginDateTime; // For login purposes
    private String profilePhotoPath; // store image path OR base64

}