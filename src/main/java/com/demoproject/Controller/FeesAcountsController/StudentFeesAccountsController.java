package com.demoproject.Controller.FeesAcountsController;

import com.demoproject.DTO.ApiResponse;
import com.demoproject.DTO.FeesAccount.StudentFeesAccountDTO;
import com.demoproject.DTO.FeesAccount.StudentFeesPaymentDTO;
import com.demoproject.DTO.FeesAccount.StudentFeesPaymentResponseDTO;
import com.demoproject.Service.FeesAcountsService.FeesAdminAccountService;
import com.demoproject.Service.FeesAcountsService.StudentAccountFeesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/{domain}/fees-student")

@PreAuthorize("""
    hasRole('STUDENT') and
    #domain.equalsIgnoreCase(authentication.principal.domain)
""")

@RequiredArgsConstructor
public class StudentFeesAccountsController {

    private final StudentAccountFeesService studentAccountFeesService;
    private final FeesAdminAccountService feesAdminAccountService;




    @GetMapping("/my/payment-account")
    public ResponseEntity<ApiResponse<?>> feesAccount(@PathVariable String domain, Authentication authentication ) {

        String email = authentication.getName();
        StudentFeesAccountDTO studentFeesAccount  = studentAccountFeesService.studentFeesAccount( domain, email  );

        return ResponseEntity.status(HttpStatus.OK)
                .body( new ApiResponse<>( true, null , studentFeesAccount  ));
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<ApiResponse<?>> paymentMethods(@PathVariable String domain) {
        return ResponseEntity.ok(new ApiResponse<>(
                true, null, feesAdminAccountService.studentPaymentMethods(domain)));
    }

    @GetMapping("/my/payment-history")
    public ResponseEntity<ApiResponse<?>> feesFeesHistory(@PathVariable String domain, Authentication authentication ) {

        String email = authentication.getName();
        List<StudentFeesPaymentResponseDTO> studentFeesPayments  = studentAccountFeesService.studentFeesPayment( domain, email  );

        return ResponseEntity.status(HttpStatus.OK)
                .body( new ApiResponse<>( true, null , studentFeesPayments  ));
    }

    @PostMapping("/add-payment")
    public ResponseEntity<ApiResponse<?>> addPayment(
            @PathVariable String domain,
            Authentication authentication,
            @Valid @RequestBody StudentFeesPaymentDTO studentFeesPaymentDTO) {

        String email = authentication.getName();
        String result  = studentAccountFeesService.studentAddPayment( domain, email , studentFeesPaymentDTO );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body( new ApiResponse<>( true, result , null  ));
    }




}
