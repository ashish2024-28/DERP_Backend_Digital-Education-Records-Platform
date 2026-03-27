package com.demoproject.DTO.StudentDTO;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class StudentSignupDTO {

    private String rollNumber;
    private String name;
    private String email;
    private String mobileNumber; //Country code +91

    private String course;
    private String branch;
    private String batch;

    private String fatherName;
    private String fatherMobNo;

    private String password;
}
