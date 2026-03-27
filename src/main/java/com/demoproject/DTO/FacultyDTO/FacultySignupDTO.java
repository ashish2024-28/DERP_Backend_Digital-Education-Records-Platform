package com.demoproject.DTO.FacultyDTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacultySignupDTO {

    private String facultyId;
    private String name;
    private String email;
    private String mobileNumber;

    private String course;
    private String teachingBatch;
    private String password; // Used for registration
}
