package com.demoproject.DTO.FacultyDTO;

import java.time.Instant;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacultyResponseDTO {
    // Fields from BaseUser
    private String name;
    private String email;
    private String password;
    private String mobileNumber;

    private Instant createdDateTime;
    private Instant lastLoginDateTime;
    // Fields specific to Faculty
    private String facultyId;
    private String course;
    private String teachingBatch;
    private String profilePhotoPath;
}