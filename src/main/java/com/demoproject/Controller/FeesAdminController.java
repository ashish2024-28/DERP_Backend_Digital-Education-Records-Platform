package com.demoproject.Controller;


import com.demoproject.DTO.ApiResponse;
import com.demoproject.DTO.FeesAdminDto.PasswordUpdateDTO;
import com.demoproject.Entity.FeesAdmin;
import com.demoproject.DTO.FeesAdminDto.FeesAdminResponseDTO;
import com.demoproject.Service.FacultyService;
import com.demoproject.Service.FeesAdminService;
import com.demoproject.Service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/{domain}/feesAdmin")
@PreAuthorize("""
        hasRole('FEES_ADMIN') and
        #domain.equalsIgnoreCase(authentication.principal.domain)
        """)
public class FeesAdminController {

    @Autowired
    private FeesAdminService feesAdminService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private FacultyService facultyService;


//     public ResponseEntity<?> test(@PathVariable String domain) {
//         try {
//             return ResponseEntity.ok(universityService.getUniversityName_Logo(domain) + ". \nSUB_ADMIN Access OK\n For dAdmin, sAdmin,...");
//         } catch (Exception e) {
//             return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
//         }
//   }

    @GetMapping
    public ResponseEntity<FeesAdminResponseDTO> getFaculty(@PathVariable String domain, Authentication authentication) {
        // 1st option
        // UsersPrinciple user =
        //     (UsersPrinciple) SecurityContextHolder
        //         .getContext()
        //         .getAuthentication()
        //         .getPrincipal();

        // String email = user.getUsername();

        // 2nd option
        // Get the email from the SecurityContext (set by JwtFilter)
        return ResponseEntity.ok(
                feesAdminService.getFeesAdminByEmailAndDomain(authentication.getName(), domain)
        );
    }




    // Update Password or Forget Password
    @PutMapping("/forgot_update_password")
    public ResponseEntity<ApiResponse<Void>> updateFeesAdminPassword(
            @PathVariable String domain,
            @Valid @RequestBody PasswordUpdateDTO request,
            Authentication authentication) {
        try {
            boolean saved = feesAdminService.updatePasswordByEmail(
                    domain, authentication.getName(), request.getNewPassword());
            if (!saved) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Fees Admin not found.", null));
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Password changed successfully.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    // UPDATE Domain + DomainId means (DId which provide by University or collage)
    @PutMapping("/update_profile")
    public ResponseEntity<ApiResponse<FeesAdminResponseDTO>> updateSubAdminByDomainId(
            @PathVariable String domain,
            @RequestBody FeesAdmin update,
            Authentication authentication) {
        try {
            FeesAdminResponseDTO response = feesAdminService.updateFeesAdminByEmail(
                    domain, authentication.getName(), update);
            return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated successfully.", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    // DELETE
    @DeleteMapping("/delete_account")
    public ResponseEntity<ApiResponse<Void>> deleteSubAdminByEmail(
            @PathVariable String domain,
            Authentication authentication) {
        String result = feesAdminService.deleteFeesAdminByEmail(domain, authentication.getName());
        if ("Not found".equals(result)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Fees Admin not found.", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Fees Admin account deleted.", null));
    }



// ------ READ ALL faculty for specific university ------

    @GetMapping("/all_faculty")
    public ResponseEntity<?> getAllFacultyBySubAdminCourse(
            @PathVariable String domain) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, null,
                        facultyService.getAllFaculty(domain))
        );
    }



    // ------ READ ALL student  for specific university ------
    @GetMapping("/all_student")
    public ResponseEntity<?> getAllStudent(
            @PathVariable String domain , Authentication authentication) {

        Set<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet());
        return ResponseEntity.ok(
                new ApiResponse<>(true, null,
                        studentService.getAllStudent(domain , roles))
        );
    }


}