package com.demoproject.Controller.Home;

import com.demoproject.DTO.ApiResponse;
import com.demoproject.Repository.DomainAdminRepository;
import com.demoproject.Service.DomainAdminService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{domain}/forgot_password")
public class ForgotPassword {

    @Autowired
    private DomainAdminService domainAdminService;

    @Autowired
    private DomainAdminRepository domainAdminRepository;

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyEmail(
            @PathVariable String domain,
            @RequestParam String email) {

        boolean exists =
                domainAdminRepository.existsByEmailAndDomain(
                        email.trim(),
                        domain
                );

        if (exists) {
            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "Email verified successfully. You can request an OTP.",
                            true
                    )
            );
        }

        return ResponseEntity.ok(
                new ApiResponse<>(
                        false,
                        "This email is not registered as a Admin. " +
                                "If you are a student, teacher, faculty member, or other staff, " +
                                "please contact your Admin or higher authority for assistance.",
                        false
                )
        );
    }

    @PutMapping
    public ResponseEntity<?> forgotPassword(
            @PathVariable String domain,
            @RequestParam String email,
            @RequestParam String newPassword) {

        domainAdminService.updatePasswordByEmail(
                domain,
                email,
                newPassword
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Password updated successfully.",
                        null
                )
        );
    }
}