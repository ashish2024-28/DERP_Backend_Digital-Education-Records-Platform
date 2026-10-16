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

    private String password;
}