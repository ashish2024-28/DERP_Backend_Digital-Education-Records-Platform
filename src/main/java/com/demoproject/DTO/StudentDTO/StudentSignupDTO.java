package com.demoproject.DTO.StudentDTO;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentSignupDTO {

    private String name;

    private String email;

    private String mobileNumber;

    private String rollNumber;

    private String course;

    private String branch;

    private String batch;

    /*
     * batch and section.
     *
     * Example:
     * 2A
     */
    private String studyBatch;

    /*
     * Multiple subjects.
     *
     * Example:
     *
     * ["JAVA", "DSA", "OS", "DBMS"]
     */
    private String studySubjects;

    private String fatherName;

    private String fatherMobNo;

    private String password;
}