package com.demoproject.Controller;



import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.demoproject.DTO.ApiResponse;
import com.demoproject.DTO.FeesAdminDto.FeesAdminResponseDTO;
import com.demoproject.DTO.FeesAdminDto.FeesAdminSignupDTO;
import com.demoproject.Entity.*;
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
    private StudentService studentService;
    @Autowired
    private FacultyService facultyService;
    @Autowired
    private SubAdminService subAdminService;
    @Autowired
    private FeesAdminService feesAdminService;
    @Autowired
    private UniversityService universityService;


    @GetMapping
    public ResponseEntity<?> getDomainAdminAndUniversity(@PathVariable String domain) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", domainAdminService.getDomainAdminAndUniversity(email, domain)));

    }

    @GetMapping("get_dashboard")
    public ResponseEntity<?> getDashboard(@PathVariable String domain) {

        long totalStudent = studentService.getStudentCount(domain);
        long totalFaculty = facultyService.getFacultyCount(domain);
        long totalSubAdmin = subAdminService.getSubAdminCount(domain);
        long totalFeesAdmin = feesAdminService.getFeesAdminCount(domain);

        Map<String, Long> response = new HashMap<>();
        response.put("students", totalStudent);
        response.put("faculty", totalFaculty);
        response.put("subAdmin", totalSubAdmin);
        response.put("feesAdmin", totalFeesAdmin);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Dashboard Data", response)
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

        String path = domainAdminService.updateProfilePic(domain, email, profilePic);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Profile updated successfully", path)
        );

    }


    //    updata university Logo
    @PutMapping("/update_university_logo")
    public ResponseEntity<?> updateUniversityLogo(
            @PathVariable String domain,
            @RequestParam MultipartFile profilePic
    ) throws IOException {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        String path = universityService.updateLogo(domain, email, profilePic);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Profile updated successfully", path)
        );

    }

    // Update Password or Forget Password
    @PutMapping("/forgot_update_password")
    // public ResponseEntity<?> updateStudentPassword(@PathVariable String domain, @RequestParam String email, @RequestParam String newPassword){
    public ResponseEntity<?> updateStudentPassword(@PathVariable String domain, @RequestParam String newPassword,
                                                   Authentication authentication) {

        try {
            String email = authentication.getName();
            boolean save = domainAdminService.updatePasswordByEmail(domain, email, newPassword);
            return new ResponseEntity<>(save + " Password change successfully \n", HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // ------ UPDATE  profile ------
    @PutMapping("/update_profile")
    public ResponseEntity<?> updateDomainAdminByEmail(@PathVariable String domain, @RequestBody DomainAdmin domainAdmin) {
        try {

            boolean save = domainAdminService.updateDomainAdminByEmail(domain, domainAdmin);
            return new ResponseEntity<>(save + " Update successfully \n", HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

// ===== STUDENT CRUD =====

    // ---- Add Student ------
    @PostMapping("/add_student")
    public ResponseEntity<?> addStudent(@PathVariable String domain, @RequestBody StudentSignupDTO s) {
        try {
            String save = studentService.addStudent(domain, s);
            return new ResponseEntity<>(save, HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // ------ READ ALL student for specific university ------
    @GetMapping("/all_student")
    public List<StudentResponseDTO> getAllStudents(@PathVariable String domain) {
        return studentService.getAllStudent(domain);
    }

    // ------ UPDATE Student Profile by Email ------
    @PutMapping("/update_student_profile")
    public Boolean updateStudentByEmail(@PathVariable String domain, @RequestBody Student s) {
        return studentService.updateStudentByEmail(domain, s);
    }

    // Update Password or Forget Password
    @PutMapping("/update_student_password")
    public ResponseEntity<?> updateStudentPassword(@PathVariable String domain,
                                                   @RequestParam String email, @RequestParam String newpass) {

        try {

            boolean save = studentService.updatePasswordByEmail(domain, email, newpass);
            return new ResponseEntity<>(save + " Password change successfully \n", HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
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
            return new ResponseEntity<>(save, HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // ------ READ ALL faculty for specific university ------
    @GetMapping("/all_faculty")
    public List<FacultyResponseDTO> getAllFaculty(@PathVariable String domain) {
        return facultyService.getAllFaculty(domain);
    }

    // Update Password or Forget Password
    @PutMapping("/update_faculty_password")
    public ResponseEntity<?> updateFacultyPassword(@PathVariable String domain, @RequestParam String newpass, @RequestParam String email) {
        try {
            boolean save = facultyService.updatePasswordByEmail(domain, email, newpass);
            return new ResponseEntity<>(save + " Password change successfully \n", HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    // ------ UPDATE Faculty Profile by Email ------
    @PutMapping("/update_faculty_profile")
    public Boolean updateFacultyByFacultyId(@PathVariable String domain, @RequestBody Faculty faculty) {
        return facultyService.updateFacultyByEmail(domain, faculty);
    }

    // ------ DELETE by faculty Email ------
    @DeleteMapping("/delete_faculty")
    public String deleteFacultyByEmail(@PathVariable String domain, @RequestParam String email) {
        return facultyService.deleteFacultyByEmail(domain, email);
    }


    // ===== SUBADMIN CRUD =====

    // CREATE
    @PostMapping("/add_subAdmin")
    public ResponseEntity<?> addSubAdmin(@PathVariable String domain, @RequestBody SubAdminSignupDTO subAdmin) {
        try {
            String save = subAdminService.addSubAdmin(domain, subAdmin);
            return new ResponseEntity<>(save, HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
    }

    // READ ALL subadmin by domain
    @GetMapping("/all_subAdmin")
    public List<SubAdminResponseDTO> getAllSubAdmin(@PathVariable String domain) {
        return subAdminService.getAllSubAdmin(domain);
    }

    // ------ UPDATE SubAdmin Profile by Email  ------
    @PutMapping("/update_subAdmin")
    public SubAdminResponseDTO updateSubAdminByEmail(@PathVariable String domain, @RequestBody SubAdmin s) {
        return subAdminService.updateSubAdminByEmail(domain, s);
    }


    // Update Password or Forget Password
    @PutMapping("/update_subAdmin_password")
    public ResponseEntity<?> updateSubAdminPassword(@PathVariable String domain, @RequestParam String email, @RequestParam String newpass) {
        try {

            boolean save = subAdminService.updatePasswordByEmail(domain, email, newpass);
            return new ResponseEntity<>(save + " Password change successfully \n", HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // ------ DELETE SubAdmin Profile by Email  ------
    @DeleteMapping("/delete_subAdmin")
    public String deleteSubAdminByEmail(@PathVariable String domain, @RequestParam String email) {
        return subAdminService.deleteSubAdminByEmail(domain, email);
    }


    // ===== FEESADMIN CRUD =====

    // CREATE
    @PostMapping("/add_feesAdmin")
    public ResponseEntity<?> addFeesAdmin(@PathVariable String domain, @RequestBody FeesAdminSignupDTO feesAdmin) {
        try {
            String save = feesAdminService.addFeesAdmin(domain, feesAdmin);
            return new ResponseEntity<>(save, HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
    }

    // READ ALL feesAdmin by domain
    @GetMapping("/all_feesAdmin")
    public List<FeesAdminResponseDTO> getAllFeesAdmin(@PathVariable String domain) {
        return feesAdminService.getAllFeesAdmin(domain);
    }

    // ------ UPDATE feesAdmin Profile by Email  ------
    @PutMapping("/update_feesAdmin")
    public FeesAdmin updateFeesAdminByEmail(@PathVariable String domain, @RequestBody FeesAdmin feesAdmin) {
        return feesAdminService.updateFeesAdminByEmail(domain, feesAdmin);
    }

    // Update Password or Forget Password
    @PutMapping("/update_feesAdmin_password")
    public ResponseEntity<?> updateFeesAdminPassword(@PathVariable String domain, @RequestParam String email, @RequestParam String newpass) {
        try {

            boolean save = feesAdminService.updatePasswordByEmail(domain, email, newpass);
            return new ResponseEntity<>(save + " Password change successfully \n", HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // ------ DELETE feesAdmin Profile by Email  ------
    @DeleteMapping("/delete_feesAdmin")
    public String deleteFeesAdminByEmail(@PathVariable String domain, @RequestParam String email) {
        return feesAdminService.deleteFeesAdminByEmail(domain, email);
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


    // =========================
// Students
// =========================
    @PostMapping("/upload_students")
    public ResponseEntity<?> uploadStudents(
            @PathVariable String domain,
            @RequestParam("files") MultipartFile[] files) {

        try {
            domainAdminService.uploadStudentsFromExcel(domain, files);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "Students uploaded successfully",
                            null
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // =========================
// Faculty
// =========================
    @PostMapping("/upload_faculty")
    public ResponseEntity<?> uploadFaculty(
            @PathVariable String domain,
            @RequestParam("files") MultipartFile[] files) {

        try {
            domainAdminService.uploadFacultyFromExcel(domain, files);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "Faculty uploaded successfully",
                            null
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // =========================
// SubAdmin
// =========================
    @PostMapping("/upload_subAdmin")
    public ResponseEntity<?> uploadSubAdmin(
            @PathVariable String domain,
            @RequestParam("files") MultipartFile[] files) {

        try {
            domainAdminService.uploadSubAdminFromExcel(domain, files);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "SubAdmin uploaded successfully",
                            null
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // =========================
// FeesAdmin
// =========================
    @PostMapping("/upload_feesAdmin")
    public ResponseEntity<?> uploadFeesAdmin(
            @PathVariable String domain,
            @RequestParam("files") MultipartFile[] files) {

        try {
            domainAdminService.uploadFeesAdminFromExcel(domain, files);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "FeesAdmin uploaded successfully",
                            null
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}