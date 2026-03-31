package com.demoproject.Controller;


import java.io.IOException;
import java.util.List;

import com.demoproject.DTO.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.demoproject.DTO.FacultyDTO.FacultySignupDTO;
import com.demoproject.DTO.StudentDTO.StudentResponseDTO;
import com.demoproject.Entity.Faculty;
import com.demoproject.Entity.Student;
import com.demoproject.Service.FacultyService;
import com.demoproject.Service.UniversityService;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/{domain}/faculty")

@PreAuthorize("""
        hasRole('FACULTY') and
        #domain.equalsIgnoreCase(authentication.principal.domain)
        """)
public class FacultyController {

    @Autowired
    private FacultyService fService;
    @Autowired
    private UniversityService universityService;

    
    // @GetMapping
    // public ResponseEntity<?> signUpPage(@PathVariable String domain){
    //      try{
    //         return  ResponseEntity.ok(universityService.getUniversityName_Logo(domain) +
    //          "FACULTY Access OK");
    //     } catch (Exception e) {
    //         return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
    //     }
    // }

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
            fService.getFacultyByEmailAndDomain(email, domain)
        );
    }



    // Update Password or Forget Password
    @PutMapping("/update_faculty_password")
    public ResponseEntity<?> updateStudentPassword(@PathVariable String domain,  @RequestParam String newpass,
                                                   Authentication authentication){
        try {
            String email = authentication.getName();
            boolean save = fService.updatePasswordByEmail(domain, email, newpass);
            return new ResponseEntity<>(save + " Password change successfully \n",HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
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

        String path = fService.updateProfilePic(domain,email,profilePic);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Profile updated successfully", path)
        );

    }


    // ------ UPDATE by facultyid  ------
    @PutMapping("/update_profile")
    public ResponseEntity<?> updateFacultyByDid(@PathVariable String domain, @RequestBody Faculty faculty) {
        try {
            boolean get = fService.updateFacultyByFacultyEmail(domain, faculty);
            return new ResponseEntity<>(get,HttpStatus.OK);
            
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        
    }

    // ------ DELETE by facultyid ------

    @DeleteMapping("/delete_account")
    public String deleteFacultyByEmail(@PathVariable String domain,
        Authentication authentication) {

        String email = authentication.getName();
        return fService.deleteFacultyByEmail(domain, email);
    }



    // ------ READ ALL student for specific university ------

    @GetMapping("/all_student")
    public ResponseEntity<?> getAllStudentByFacultyCourse(
            @PathVariable String domain,
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                new ApiResponse<>(true, null,
                        fService.getStudentsByFacultyCourse(domain, email))
        );
    }


}






/*
@RestController → tells Spring this class contains API endpoints.

@RequestMapping("/student") → all APIs will start with /student.

@Autowired → Spring will automatically create the object of StudentService.

@PostMapping → handles HTTP POST request.

@RequestBody → converts JSON → Student object.a
*/


