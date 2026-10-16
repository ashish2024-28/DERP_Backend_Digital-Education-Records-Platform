package com.demoproject.Controller.FeesAcountsController;

import com.demoproject.DTO.ApiResponse;
import com.demoproject.DTO.FeesAccount.DomainAdmin.FeesCourseWiseDTO;
import com.demoproject.DTO.FeesAccount.FeesPaymentActionDTO;
import com.demoproject.DTO.FeesAccount.FeesPaymentBankDTO;
import com.demoproject.DTO.FeesAccount.FeesPaymentOnlineDTO;
import com.demoproject.DTO.FeesAccount.FeesPaymentReviewDTO;
import com.demoproject.DTO.FeesAccount.FeesPaymentHistoryResponseDTO;
import com.demoproject.DTO.FeesAccount.FeesPaymentMethodResponseDTO;
import com.demoproject.Entity.AttendenceErp.ERPActionAudit;
import com.demoproject.Service.DomainAdminService;
import com.demoproject.Service.FeesAcountsService.FeesAdminAccountService;
import com.demoproject.Service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/{domain}/fees-admin")

@PreAuthorize("""
    hasAnyRole('FEES_ADMIN', 'DOMAIN_ADMIN') and
    #domain.equalsIgnoreCase(authentication.principal.domain)
""")
@RequiredArgsConstructor
public class FeesAdminAccountController {

    private  final FeesAdminAccountService feesAdminAccountService;
    private  final DomainAdminService domainAdminService;
    private final StudentService studentService;



    @GetMapping("/fees-course-wise")
    public ResponseEntity<ApiResponse<?>> AllFeesCourseWise(@PathVariable String domain, Authentication authentication ) {

        List<FeesCourseWiseDTO> feesCourseWise  = domainAdminService.getAllFeesCourseWise( domain );

        return ResponseEntity.status(HttpStatus.OK)
                .body( new ApiResponse<>( true,null ,feesCourseWise ));
    }

    @GetMapping("/all-students")
    public ResponseEntity<ApiResponse<?>> allStudents(
            @PathVariable String domain,
            Authentication authentication) {
        Set<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet());

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                null,
                studentService.getAllStudent(domain, roles)));
    }




    // =========================================================
    // PAYMENT METHODS
    // =========================================================


    /**
     * Create bank payment method
     *
     * POST
     * /{domain}/fees/payment-method/bank?email=feesadmin@gmail.com
     */
    @PostMapping("/payment-method/bank")
    public ResponseEntity<ApiResponse<String>> createBankPaymentMethod(
            @PathVariable String domain,
            Authentication authentication,
            @Valid @RequestBody FeesPaymentBankDTO paymentBankDTO) {

        String email = authentication.getName();
        String result = feesAdminAccountService.createFeesPaymentBank(
                domain, email, paymentBankDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body( new ApiResponse<>(true, result, null ) );
    }


    /**
     * Create online / UPI payment method
     *
     * POST
     * /{domain}/fees/payment-method/online?email=feesadmin@gmail.com
     */
    @PostMapping("/payment-method/online")
    public ResponseEntity<ApiResponse<String>> createOnlinePaymentMethod(
            @PathVariable String domain,
            Authentication authentication,
            @Valid @RequestPart("paymentOnlineDTO") FeesPaymentOnlineDTO paymentOnlineDTO,
            @RequestPart(value = "qrImage", required = false) MultipartFile qrImage) throws IOException {

        String email = authentication.getName();
        String result = feesAdminAccountService.createFeesPaymentOnline(
                domain, email, paymentOnlineDTO , qrImage);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body( new ApiResponse<>( true, result, null  ));
    }


    @GetMapping("/payment-methods")
    public ResponseEntity<ApiResponse<?>> paymentMethods(@PathVariable String domain  ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true, null, feesAdminAccountService.domainAdminPaymentMethods(
                        domain)));
    }

    @GetMapping("/activity-logs")
    public ResponseEntity<ApiResponse<List<ERPActionAudit>>> feesAdminActivityLogs(
            @PathVariable String domain) {
        return ResponseEntity.ok(new ApiResponse<>(
                true, "Fees Admin activity fetched successfully",
                feesAdminAccountService.feesAdminActivity(domain)));
    }


    @GetMapping("/admin/payments")
    public ResponseEntity<ApiResponse<List<FeesPaymentHistoryResponseDTO>>> domainPaymentHistory(
            @PathVariable String domain) {
        return ResponseEntity.ok(new ApiResponse<>(
                true, null, feesAdminAccountService.domainAdminPaymentHistory(domain)));
    }

    @PutMapping("/payment-method/{id}/active")
    public ResponseEntity<ApiResponse<String>> setPaymentMethodActive(
            @PathVariable String domain,
            @PathVariable Long id,
            @RequestParam boolean active,
            Authentication authentication) {
        String result = feesAdminAccountService.setPaymentMethodActive(
                domain, authentication.getName(), id, active);
        return ResponseEntity.ok(new ApiResponse<>(true, result, null));
    }

    @GetMapping("/payments/pending")
    public ResponseEntity<ApiResponse<?>> pendingPayments(
            @PathVariable String domain, Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(
                true, null, feesAdminAccountService.pendingPayments(
                        domain, authentication.getName())));
    }

    @GetMapping("/payments/recorded/count")
    public ResponseEntity<ApiResponse<Long>> recordedPaymentCount(
            @PathVariable String domain, Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(
                true, null, feesAdminAccountService.recordedPaymentCount(
                        domain, authentication.getName())));
    }

    @PostMapping("/payment/cash")
    public ResponseEntity<ApiResponse<String>> addCashFeesOfStudent(
            @PathVariable String domain,
            Authentication authentication,
            @Valid @RequestBody FeesPaymentActionDTO paymentAction) {

        String email = authentication.getName();
        String result = feesAdminAccountService.addCashFeesInStudentAccount(
                domain, email, paymentAction);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body( new ApiResponse<>( true, result ,null ));
    }


    @PostMapping("/online-payment/verify-success")
    public ResponseEntity<ApiResponse<String>> verifyOnlineStudentFeesPayment(
            @PathVariable String domain,
            Authentication authentication,
            @Valid @RequestBody FeesPaymentReviewDTO review) {

        String email = authentication.getName();
        String result = feesAdminAccountService.verifyOnlineStudentFeesPaymentSuccess(
                domain, email, review);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body( new ApiResponse<>( true, result ,null ));
    }

    @PostMapping({"/online-payment/verify-fail", "/online-payment/verify-faill"})
    public ResponseEntity<ApiResponse<String>> verifyOnlineStudentFeesPaymentFail(
            @PathVariable String domain,
            Authentication authentication,
            @Valid @RequestBody FeesPaymentReviewDTO review) {

        String email = authentication.getName();
        String result = feesAdminAccountService.verifyOnlineStudentFeesPaymentFail(
                domain, email, review);

        return ResponseEntity.status(HttpStatus.OK)
                .body( new ApiResponse<>( true, result ,null ));
    }



}
