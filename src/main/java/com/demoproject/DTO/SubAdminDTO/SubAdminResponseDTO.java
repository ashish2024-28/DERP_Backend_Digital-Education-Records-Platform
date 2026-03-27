package com.demoproject.DTO.SubAdminDTO;

import java.time.Instant;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubAdminResponseDTO {

    private String name;
    private String email;
    private String password;
    private String mobileNumber;

    private String subAdminId;
    private String course;
    private Instant createdDateTime;
    private Instant lastLoginDateTime; // For login purposes
 private String profilePic;// store image path OR base64

}