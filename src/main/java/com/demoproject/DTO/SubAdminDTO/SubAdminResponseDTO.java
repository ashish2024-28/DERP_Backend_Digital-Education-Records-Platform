package com.demoproject.DTO.SubAdminDTO;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
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

    private LocalDateTime createdDateTime;

    private LocalDateTime lastUpdateDateTime;

    private LocalDateTime lastLoginDateTime;

    private String subAdminId;

    private String course;

    private String teachingAssignments;

    private String universityName;


}