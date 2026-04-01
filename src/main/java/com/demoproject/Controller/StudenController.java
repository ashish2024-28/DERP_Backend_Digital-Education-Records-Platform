package com.demoproject.Controller;

import com.demoproject.DTO.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.demoproject.Entity.Student;
import com.demoproject.Service.StudentService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/{domain}/student")

@PreAuthorize("""
        hasRole('STUDENT') and
        #domain.equalsIgnoreCase(authentication.principal.domain)
        """)
        // #domain == principal.domain


public class StudenController {

    @Autowired
    private StudentService studentService;
    

    // @GetMapping
    // public ResponseEntity<?> test(@PathVariable String domain) {
    //     try {
    //         return  ResponseEntity.ok(universityService.getUniversityName_Logo(domain) +
    //          "Student Access OK");
            
    //     } catch (Exception e) {
    //         return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
    //     }

    // }

    @GetMapping
    public ResponseEntity<?> getStudent(@PathVariable String domain) {
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
            studentService.getStudentByEmailAndDomain(email, domain)
        );
    }


    //    updata profile picture
    @PutMapping("/update_profile_pic")
    public ResponseEntity<?> updateProfilePic(
            @PathVariable String domain,
            @RequestParam MultipartFile profilePic
    ) throws IOException {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String path = studentService.updateProfilePic(domain,email,profilePic);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Profile updated successfully", path)
        );

    }

    // Update Password or Forget Password
    @PutMapping("/forgot_update_password")
    public ResponseEntity<?> updateStudentPassword(@PathVariable String domain,
            Authentication authentication, @RequestParam String newpass) throws  Exception{

        String email = authentication.getName();
        boolean save = studentService.updatePasswordByEmail(domain, email, newpass);
        return new ResponseEntity<>(save + " Password change successfully \n",HttpStatus.CREATED);

    }

    // ------ UPDATE  profile ------
    @PutMapping("/update_profile")
    public ResponseEntity<?> updateStudentByEmail(@PathVariable String domain, @RequestBody Student s) throws  Exception{

        boolean save = studentService.updateStudentByEmail(domain, s);
        return new ResponseEntity<>(save + " Update successfully \n",HttpStatus.CREATED);

    }


    // DELETE Account
    @DeleteMapping("/delete_account")
    public ResponseEntity<?> deleteByEmail(@PathVariable String domain,
        Authentication authentication) {
        try {
            String email = authentication.getName();
            String save = studentService.deleteStudentByEmail(domain, email);
            return new ResponseEntity<>(save ,HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }
}


/*
@RestController → tells Spring this class contains API endpoints.

@RequestMapping("/student") → all APIs will start with /student.

@Autowired → Spring will automatically create the object of StudentService.

@PostMapping → handles HTTP POST request.

@RequestBody → converts JSON → Student object.
*/