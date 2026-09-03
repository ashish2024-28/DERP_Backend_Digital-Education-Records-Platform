package com.demoproject.DTO.SubAdminDTO;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubAdminResponseDTO {

    private String name;

    private String email;

    private String mobileNumber;

    private String profilePic;

    private Instant createdDateTime;

    private Instant lastLoginDateTime;

    private String subAdminId;

    private String course;

    private Map<String, List<String>> teachingAssignments;

    private String universityName;

    private String password;

}