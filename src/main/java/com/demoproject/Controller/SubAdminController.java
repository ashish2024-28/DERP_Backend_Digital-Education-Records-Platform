package com.demoproject.Controller;


import java.io.IOException;

import com.demoproject.DTO.ApiResponse;
import com.demoproject.DTO.SubAdminDTO.SubAdminResponseDTO;
import com.demoproject.Service.FacultyService;
import com.demoproject.Service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demoproject.Entity.SubAdmin;
import com.demoproject.Service.SubAdminService;
import com.demoproject.Service.UniversityService;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/{domain}/subAdmin")
@PreAuthorize("""
        hasRole('SUB_ADMIN') and
        #domain.equalsIgnoreCase(authentication.principal.domain)
        """)
public class SubAdminController {

    @Autowired
    private SubAdminService sAService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private FacultyService facultyService;
    @Autowired
    private UniversityService universityService;


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
            sAService.getSubAdminByEmailAndDomain(email, domain)
        );
    }



    //    updata profile picture
    @PutMapping("/update_profile_pic")
    public ResponseEntity<?> updateProfilePic(
            @PathVariable String domain,
            @RequestParam MultipartFile profilePic
    ) throws IOException {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        String path = sAService.updateProfilePic(domain,email,profilePic);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Profile updated successfully", path)
        );

    }

    // Update Password or Forget Password
    @PutMapping("/forgot_update_password")
    public ResponseEntity<?> updateSubAdminPassword(@PathVariable String domain, @RequestParam String email, @RequestParam String newpass){
        try {

            boolean save = sAService.updatePasswordByEmail(domain, email, newpass);
            return new ResponseEntity<>(save + " Password change successfully \n",HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    // ------ UPDATE SubAdmin Profile by Email  ------
    @PutMapping("/update_profile")
    public Boolean updateSubAdminByDomainEmail(@PathVariable String domain, @RequestBody SubAdmin email) {
        return sAService. updateSubAdminByEmail(domain, email);
    }

    // DELETE
    @DeleteMapping("/delete_account")
    public String deleteSubAdminByDomainEmail(@PathVariable String domain, @RequestBody String Email) {
        return sAService.deleteSubAdminByEmail(domain, Email);
    }


// ------ READ ALL faculty specific university ------
    @GetMapping("/all_faculty")
    public ResponseEntity<?> getAllFaculty(
            @PathVariable String domain) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, null,
                        facultyService.getAllFaculty(domain))
        );
    }


    // ------ READ ALL student for specific university ------
    @GetMapping("/all_student")
    public ResponseEntity<?> getAllStudentBySubAdminCourse(
            @PathVariable String domain) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, null,
                        studentService.getAllStudent(domain))
        );
    }


}