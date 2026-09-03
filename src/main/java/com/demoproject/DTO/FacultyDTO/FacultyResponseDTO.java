package com.demoproject.DTO.FacultyDTO;

import lombok.*;

import java.time.Instant;
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

    private Instant createdDateTime;

    private Instant lastLoginDateTime;

    private String facultyId;

    private String course;

    private Map<String, List<String>> teachingAssignments;

    private String universityName;

    private String password; // Used only see by admin

}