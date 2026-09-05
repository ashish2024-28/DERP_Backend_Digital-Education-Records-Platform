package com.demoproject.DTO.StudentDTO;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentResponseDTO {

    private String name;

    private String email;

    private String mobileNumber;

    private LocalDateTime createdDateTime;

    private LocalDateTime lastUpdateDateTime;

    private LocalDateTime lastLoginDateTime;

    private String profilePic;


    private String rollNumber;

    private String course;

    private String branch;

    private String batch;

    private String studyBatch;

    private String studySubjects;

    private String fatherName;

    private String fatherMobNo;

    private String universityName;


}