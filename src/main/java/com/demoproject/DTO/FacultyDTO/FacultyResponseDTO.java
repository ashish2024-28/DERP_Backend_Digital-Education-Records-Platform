package com.demoproject.DTO.FacultyDTO;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacultyResponseDTO {

    private String name;

    private String email;

    private String mobileNumber;

    private String profilePic;

    private LocalDateTime createdDateTime;

    private LocalDateTime lastUpdateDateTime; // For login purposes

    private LocalDateTime lastLoginDateTime;

    private String facultyId;

    private String course;

    private String teachingAssignments;

    private String universityName;


}