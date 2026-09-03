package com.demoproject.DTO.FeesAdminDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FeesAdminSignupDTO {

    private String feesAdminId;
    private String name;
    private String email;
    private String mobileNumber;

    private String password; // Used for registration

}

