package com.demoproject.DTO.SubAdminDTO;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SubAdminSignupDTO {
    private String name;
    private String mobileNumber;
    private String email;
    private String password; // Used for registration

    private String subAdminId;
    private String course;
    
}

