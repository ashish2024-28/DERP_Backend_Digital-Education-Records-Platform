package com.demoproject.Controller;


import com.demoproject.DTO.ApiResponse;
import com.demoproject.Entity.FeesAdmin;
import com.demoproject.Service.FacultyService;
import com.demoproject.Service.FeesAdminService;
import com.demoproject.Service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


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
    public ResponseEntity<?> getFaculty(@PathVariable String domain) {
        // 1st option
        // UsersPrinciple user =
        //     (UsersPrinciple) SecurityContextHolder
        //         .getContext()
        //         .getAuthentication()
        //         .getPrincipal();

        // String email = user.getUsername();

        // 2nd option
        // Get the email from the SecurityContext (set by JwtFilter)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return ResponseEntity.ok(
                feesAdminService.getFeesAdminByEmailAndDomain(email, domain)
        );
    }




    // Update Password or Forget Password
    @PutMapping("/forgot_update_password")
    public ResponseEntity<?> updateFeesAdminPassword(@PathVariable String domain, @RequestParam String email, @RequestParam String newpass){
        try {

            boolean save = feesAdminService.updatePasswordByEmail(domain, email, newpass);
            return new ResponseEntity<>(save + " Password change successfully \n",HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    // UPDATE Domain + DomainId means (DId which provide by University or collage)
    @PutMapping("/update_profile")
    public FeesAdmin updateSubAdminByDomainId(@PathVariable String domain, @RequestBody FeesAdmin email) {
        return feesAdminService. updateFeesAdminByEmail(domain, email);
    }

    // DELETE
    @DeleteMapping("/delete_account")
    public String deleteSubAdminByDomainId(@PathVariable String domain, @RequestBody String subAdminId) {
        return feesAdminService.deleteFeesAdminByEmail(domain, subAdminId);
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
            @PathVariable String domain) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, null,
                        studentService.getAllStudent(domain))
        );
    }


}