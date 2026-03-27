package com.demoproject.Controller;


import java.io.IOException;
import java.util.List;

import com.demoproject.DTO.ApiResponse;
import com.demoproject.Service.FacultyService;
import com.demoproject.Service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demoproject.DTO.FacultyDTO.FacultyResponseDTO;
import com.demoproject.DTO.StudentDTO.StudentResponseDTO;
import com.demoproject.DTO.SubAdminDTO.SubAdminSignupDTO;
import com.demoproject.Entity.Faculty;
import com.demoproject.Entity.Student;
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
    public ResponseEntity<?> updateStudentPassword(@PathVariable String domain, @RequestParam String email, @RequestParam String newpass){
        try {

            boolean save = sAService.updatePasswordByEmail(domain, email, newpass);
            return new ResponseEntity<>(save + " Password change successfully \n",HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    // UPDATE Domain + DomainId means (DId which provide by University or collage)
    @PutMapping("/update_profile")
    public SubAdmin  updateSubAdminByDomainId(@PathVariable String domain, @RequestBody SubAdmin email) {
        return sAService. updateSubAdminByEmail(domain, email);
    }

    // DELETE
    @DeleteMapping("/delete_account")
    public String deleteSubAdminByDomainId(@PathVariable String domain, @RequestBody String subAdminId) {
        return sAService.deleteSubAdminBySubAdminId(domain, subAdminId);
    }



// ------ READ ALL faculty with SubAdmin Course for specific university ------

    @GetMapping("/all_faculty")
    public ResponseEntity<?> getAllFacultyBySubAdminCourse(
            @PathVariable String domain,
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                new ApiResponse<>(true, null,
                        sAService.getAllFacultyBySubAdminCourse(domain, email))
        );
    }



    // ------ READ ALL student with SubAdmin Course for specific university ------
    @GetMapping("/all_student")
    public ResponseEntity<?> getAllStudentBySubAdminCourse(
            @PathVariable String domain,
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                new ApiResponse<>(true, null,
                        sAService.getStudentBySubAdminCourse(domain, email))
        );
    }


}