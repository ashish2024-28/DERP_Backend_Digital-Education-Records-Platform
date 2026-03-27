package com.demoproject.DTO.SubAdminDTO;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SubAdminSignupDTO {

    private String subAdminId;
    private String name;
    private String email;
    private String mobileNumber;

    private String course;
    private String password; // Used for registration

}

