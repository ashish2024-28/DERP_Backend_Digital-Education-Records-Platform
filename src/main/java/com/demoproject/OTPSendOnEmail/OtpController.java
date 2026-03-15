package com.demoproject.OTPSendOnEmail;

import com.demoproject.DTO.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/otp")
public class OtpController {

    @Autowired
    private OtpService otpService;

    // SEND OTP
    @PostMapping("/send")
    public ApiResponse<String> sendOtp(@RequestParam String email) {

        otpService.sendOtp(email);

        return new ApiResponse<>(
                true,
                "OTP sent successfully",
                email
        );
    }

    // VERIFY OTP
    @PostMapping("/verify")
    public ApiResponse<Boolean> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp
    ) {

        boolean valid = otpService.verifyOtp(email, otp);

        if(valid){
            return new ApiResponse<>(true,"OTP verified",true);
        }

        return new ApiResponse<>(false,"Invalid or expired OTP",false);
    }
}