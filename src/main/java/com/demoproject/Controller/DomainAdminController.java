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
import org.springframework.web.bind.annotation.*;

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

    @Autowired private DomainAdminService domainAdminService;
    @Autowired private SubAdminService    subAdminService;
    @Autowired private FacultyService     facultyService;
    @Autowired private StudentService     studentService;
    @Autowired private UniversityService  universityService;

    // -------- Profile --------

    @GetMapping
    public ResponseEntity<?> getDomainAdmin(@PathVariable String domain) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success",
                        domainAdminService.getDomainAdminByEmailAndDomain(email, domain)));
    }

    @GetMapping("get_dashboard")
    public ResponseEntity<?> getDashboard(@PathVariable String domain) {
        Map<String, Long> response = new HashMap<>();
        response.put("students", domainAdminService.getStudentCount(domain));
        response.put("faculty",  domainAdminService.getFacultyCount(domain));
        response.put("subAdmin", domainAdminService.getSubAdminCount(domain));
        return ResponseEntity.ok(new ApiResponse<>(true, "Dashboard Data", response));
    }

    //    updata profile picture
    @PutMapping("/update_profile_pic")
    public ResponseEntity<?> updateProfilePic(
            @PathVariable String domain,
            @RequestParam MultipartFile profilePic) throws IOException {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String path  = domainAdminService.updateProfilePic(domain, email, profilePic);
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated successfully", path));
    }

    // Update Password or Forget Password
    @PutMapping("/forgot_update_password")
    public ResponseEntity<?> updateDomainAdminPassword(
            @PathVariable String domain,
            @RequestParam String newPassword,
            Authentication authentication) {

        String email  = authentication.getName();
        boolean saved = domainAdminService.updatePasswordByEmail(domain, email, newPassword);
        if (!saved) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Admin not found", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Password changed successfully", null));
    }

    // ======== University profile Update ========

    //    updata profile picture
    @PutMapping("/update_university_logo")
    public ResponseEntity<?> updateUniversityLogo(
            @PathVariable String domain,
            @RequestParam MultipartFile profilePic) throws IOException {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String path  = domainAdminService.updateUniversityLogo(domain, email, profilePic);
        return ResponseEntity.ok(new ApiResponse<>(true, "Logo updated successfully", path));
    }

    // ===== STUDENT CRUD =====

    // ---- Add Student ------
    @PostMapping("/add_student")
    public ResponseEntity<?> addStudent(@PathVariable String domain, @RequestBody StudentSignupDTO s) {
        try {
            String result = studentService.addStudent(domain, s);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, result, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    // ------ READ ALL student for specific university ------
    @GetMapping("/all_student")
    public ResponseEntity<List<StudentResponseDTO>> getAllStudents(@PathVariable String domain) {
        return ResponseEntity.ok(studentService.getAllStudent(domain));
    }

    // ------ UPDATE  Student Profile ------
    @PutMapping("/update_student_profile")
    public ResponseEntity<?> updateStudentByEmail(@PathVariable String domain, @RequestBody Student s) {
        boolean updated = studentService.updateStudentByEmail(domain, s);
        if (!updated) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Student not found or update failed", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Student updated successfully", null));
    }

    // ------ Delete  Student Profile ------
    @DeleteMapping("/delete_student")
    public ResponseEntity<?> deleteStudentByEmail(@PathVariable String domain, @RequestParam String email) {
        String result = studentService.deleteStudentByEmail(domain, email);
        return ResponseEntity.ok(new ApiResponse<>(true, result, null));
    }

    // ===== FACULTY CRUD =====

    // ---- CREATE ------
    @PostMapping("/add_faculty")
    public ResponseEntity<?> addFaculty(@PathVariable String domain, @RequestBody FacultySignupDTO f) {
        try {
            String result = facultyService.addFaculty(domain, f);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, result, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    // ------ READ ALL faculty for specific university ------
    @GetMapping("/all_faculty")
    public ResponseEntity<List<FacultyResponseDTO>> getAllFaculty(@PathVariable String domain) {
        return ResponseEntity.ok(facultyService.getAllFaculty(domain));
    }

    // ------ UPDATE Faculty Profile  ------
    @PutMapping("/update_faculty")
    public ResponseEntity<?> updateFaculty(@PathVariable String domain, @RequestBody Faculty f) {
        boolean updated = facultyService.updateFacultyByFacultyEmail(domain, f);
        if (!updated) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Faculty not found or update failed", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Faculty updated successfully", null));
    }

    // ------ DELETE by facultyId ------
    @DeleteMapping("/delete_faculty")
    public ResponseEntity<?> deleteFaculty(@PathVariable String domain, @RequestParam String email) {
        String result = facultyService.deleteFacultyByEmail(domain, email);
        return ResponseEntity.ok(new ApiResponse<>(true, result, null));
    }

    // ===== SUBADMIN CRUD =====

    @PostMapping("/add_subAdmin")
    public ResponseEntity<?> addSubAdmin(@PathVariable String domain, @RequestBody SubAdminSignupDTO subAdmin) {
        try {
            String result = subAdminService.addSubAdmin(domain, subAdmin);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, result, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    // READ ALL subadmin by domain
    @GetMapping("/all_subadmin")
    public ResponseEntity<List<SubAdminResponseDTO>> getAllSubAdmin(@PathVariable String domain) {
        return ResponseEntity.ok(subAdminService.getAllSubAdmin(domain));
    }

    // UPDATE
    @PutMapping("/update_subadmin")
    public ResponseEntity<?> updateSubAdmin(@PathVariable String domain, @RequestBody SubAdmin s) {
        SubAdmin updated = subAdminService.updateSubAdminByEmail(domain, s);
        if (updated == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "SubAdmin not found or update failed", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "SubAdmin updated successfully", updated));
    }

    // DELETE
    @DeleteMapping("/delete_subadmin")
    public ResponseEntity<?> deleteSubAdmin(@PathVariable String domain, @RequestParam String email) {
        String result = subAdminService.deleteSubAdminBySubAdminEmail(domain, email);
        return ResponseEntity.ok(new ApiResponse<>(true, result, null));
    }

    // --- Bulk Excel Upload ------------------

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
            return ResponseEntity.ok(new ApiResponse<>(true, "Students uploaded successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Upload failed: " + e.getMessage(), null));
        }
    }

    //    Faculty
    @PostMapping("/upload_faculty")
    public ResponseEntity<?> uploadFaculty(
            @PathVariable String domain,
            @RequestParam("file") MultipartFile file) {
        try {
            domainAdminService.uploadFacultyFromExcel(domain, file);
            return ResponseEntity.ok(new ApiResponse<>(true, "Faculty uploaded successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Upload failed: " + e.getMessage(), null));
        }
    }
    //    SubAdmin
    @PostMapping("/upload_subAdmin")
    public ResponseEntity<?> uploadSubAdmin(
            @PathVariable String domain,
            @RequestParam("file") MultipartFile file) {
        try {
            domainAdminService.uploadSubAdminFromExcel(domain, file);
            return ResponseEntity.ok(new ApiResponse<>(true, "SubAdmin uploaded successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Upload failed: " + e.getMessage(), null));
        }
    }
}






