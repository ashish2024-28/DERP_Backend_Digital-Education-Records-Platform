package com.demoproject.DTO.SubAdminDTO;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubAdminSignupDTO {

    private String subAdminId;

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
    private String  teachingAssignments;

    private String password;
}