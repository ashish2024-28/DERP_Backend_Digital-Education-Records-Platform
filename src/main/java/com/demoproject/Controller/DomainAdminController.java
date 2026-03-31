package com.demoproject.Controller;



import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.demoproject.DTO.ApiResponse;
import com.demoproject.Service.*;
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
import com.demoproject.DTO.FacultyDTO.FacultySignupDTO;
import com.demoproject.DTO.StudentDTO.StudentResponseDTO;
import com.demoproject.DTO.StudentDTO.StudentSignupDTO;
import com.demoproject.DTO.SubAdminDTO.SubAdminResponseDTO;
import com.demoproject.DTO.SubAdminDTO.SubAdminSignupDTO;
import com.demoproject.Entity.Faculty;
import com.demoproject.Entity.Student;
import com.demoproject.Entity.SubAdmin;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/{domain}/domainAdmin")

@PreAuthorize("""
    hasRole('DOMAIN_ADMIN') and
    #domain.equalsIgnoreCase(authentication.principal.domain)
""")
public class DomainAdminController {

    @Autowired
    private DomainAdminService domainAdminService;
    @Autowired
    private SubAdminService subAdminService;
    @Autowired
    private FacultyService facultyService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private UniversityService universityService;


    @GetMapping
    public ResponseEntity<?> getDomainAdmin(@PathVariable String domain) throws Exception{
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            return ResponseEntity.ok(
                    new ApiResponse<>(true,"Success", domainAdminService.getDomainAdminByEmailAndDomain(email, domain)));

    }

    @GetMapping("get_dashboard")
    public ResponseEntity<?> getDashboard(@PathVariable String domain) {

        long totalstudent = domainAdminService.getStudentCount(domain);
        long totalfaculty = domainAdminService.getFacultyCount(domain);
        long totalsubAdmin = domainAdminService.getSubAdminCount(domain);

        Map<String, Long> response = new HashMap<>();
        response.put("students", totalstudent);
        response.put("faculty", totalfaculty);
        response.put("subAdmin", totalsubAdmin);

        return ResponseEntity.ok(
                new ApiResponse<>(true,"Dashboard Data",response)
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

            String path = domainAdminService.updateProfilePic(domain,email,profilePic);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Profile updated successfully", path)
            );

    }

     // Update Password or Forget Password
    @PutMapping("/forgot_update_password")
    // public ResponseEntity<?> updateStudentPassword(@PathVariable String domain, @RequestParam String email, @RequestParam String newPassword){
    public ResponseEntity<?> updateStudentPassword(@PathVariable String domain, @RequestParam String newPassword,
        Authentication authentication){

        try {
            String email = authentication.getName();
            boolean save = domainAdminService.updatePasswordByEmail(domain, email, newPassword);
            return new ResponseEntity<>(save + " Password change successfully \n",HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }


// ===== STUDENT CRUD =====

    // ---- Add Student ------
    @PostMapping("/add_student")
    public ResponseEntity<?> addStudent(@PathVariable String domain, @RequestBody StudentSignupDTO s) {
       try {
        String save = studentService.addStudent(domain, s);
        return new ResponseEntity<>(save,HttpStatus.CREATED);
        
    } catch (Exception e) {
           return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);  
       }    
    }

    // ------ READ ALL student for specific university ------
    @GetMapping("/all_student")
    public List<StudentResponseDTO> getAllStudents(@PathVariable String domain) {
        return studentService.getAllStudent(domain);
    }

    // ------ UPDATE  Student Profile ------
    @PutMapping("/update_student_profile")
    public Boolean updateStudentByEmail(@PathVariable String domain,  @RequestBody Student s) {
        return studentService.updateStudentByEmail(domain, s);
    }

    // DELETE By email
    @DeleteMapping("/delete_student")
    public String deleteByEmail(@PathVariable String domain, @RequestParam String email) {
        return studentService.deleteStudentByEmail(domain, email);
    }



// ===== FACULTY CRUD =====

    // ---- CREATE ------
    @PostMapping("/add_faculty")
    public ResponseEntity<?> addFaculty(@PathVariable String domain, @RequestBody FacultySignupDTO f) {
       try {
        String save = facultyService.addFaculty(domain, f);
        return new ResponseEntity<>(save,HttpStatus.CREATED);
        
    } catch (Exception e) {
           return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
       }    
    }

    // ------ READ ALL faculty for specific university ------
    @GetMapping("/all_faculty")
    public List<FacultyResponseDTO> getAllFaculty(@PathVariable String domain) {
        return facultyService.getAllFaculty(domain);
    }
   


    // ------ UPDATE Faculty Profile  ------
    @PutMapping("/update_faculty")
    public Boolean updateFacultyByFacultyId(@PathVariable String domain, @RequestBody Faculty f) {
        return facultyService.updateFacultyByFacultyEmail(domain, f);
    }

    // ------ DELETE by facultyId ------
    @DeleteMapping("/delete_faculty")
    public String deleteFacultyByDId(@PathVariable String domain, @RequestParam String email) {
        return facultyService.deleteFacultyByEmail(domain, email);
    }


    // ===== SUBADMIN CRUD =====

    // CREATE
    @PostMapping("/add_subAdmin")
    public ResponseEntity<?> addSubAdmin(@PathVariable String domain, @RequestBody SubAdminSignupDTO subAdmin) {
       try {
        String save = subAdminService.addSubAdmin(domain, subAdmin);
        return new ResponseEntity<>(save,HttpStatus.CREATED);
        
    } catch (Exception e) {
           return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        
       }    
    }

    // READ ALL subadmin by domain
    @GetMapping("/all_subadmin")
    public List<SubAdminResponseDTO> getAllSubAdmin(@PathVariable String domain) {
        return subAdminService.getAllSubAdmin(domain);
    }

    // UPDATE
    @PutMapping("/update_subadmin")
    public SubAdmin updateSubAdminBySubAdminId(@PathVariable String domain, @RequestBody SubAdmin s) {
        return subAdminService.updateSubAdminByEmail(domain, s);
    }

    // DELETE
    @DeleteMapping("/delete_subadmin")
    public String deleteSubAdminBySubAdminId(@PathVariable String domain, @RequestParam String email) {
        return subAdminService.deleteSubAdminBySubAdminEmail(domain, email);
    }


//    🚀 1. Overall Flow (Understand First)
//    Frontend (React)
//          ↓ upload file
//    Spring Boot Controller
//          ↓
//    Service Layer
//          ↓
//    Read Excel (Apache POI)
//          ↓
//    Loop rows → Save in DB


    @PostMapping("/upload_students")
    public ResponseEntity<?> uploadStudents(
            @PathVariable String domain,
            @RequestParam("file") MultipartFile file) {

        try {
            domainAdminService.uploadStudentsFromExcel(domain, file);
            return ResponseEntity.ok(
            new ApiResponse<>(true, "Students uploaded successfully",null));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

//    Faculty
    @PostMapping("/upload_faculty")
    public ResponseEntity<?> uploadFacuty(
            @PathVariable String domain,
            @RequestParam("file") MultipartFile file) throws Exception {

            domainAdminService.uploadFacultyFromExcel(domain, file);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Faculty uploaded successfully",null));
    }

//    SubAdmin
    @PostMapping("/upload_subAdmin")
    public ResponseEntity<?> uploadSubAdmin(
            @PathVariable String domain,
            @RequestParam("file") MultipartFile file) throws Exception {

            domainAdminService.uploadSubAdminFromExcel(domain, file);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "SubAdmin uploaded successfully",null));
    }
}
