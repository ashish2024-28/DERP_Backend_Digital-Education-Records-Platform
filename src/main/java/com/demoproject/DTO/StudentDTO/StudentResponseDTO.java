package com.demoproject.DTO.StudentDTO;

import java.time.Instant;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentResponseDTO {
   
    private String name;
    private String rollNumber;
    private String email;
    private String password;
    private String mobileNumber;
    private String fatherName;
    private String fatherMobNo;
    private String course;
    private String branch;
    private String batch;
    private Instant createdDateTime;
    private Instant lastLoginDateTime; // For login purposes
    private String profilePhotoPath; // store image path OR base64


}

