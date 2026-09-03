package com.demoproject.DTO.FacultyDTO;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacultySignupDTO {

    private String facultyId;

    private String name;

    private String email;

    private String mobileNumber;

    private String course;

    /*
     * Example:
     *
     * {
     *   "2A": ["JAVA", "DSA"],
     *   "2B": ["OS"],
     *   "3A": ["DBMS"]
     * }
     */
    private Map<String, List<String>> teachingAssignments;

    private String password;
}