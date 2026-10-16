package com.demoproject.Controller;



import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.demoproject.DTO.ApiResponse;
import com.demoproject.DTO.BulkUpload.BulkUploadResultDTO;
import com.demoproject.DTO.FeesAccount.DomainAdmin.FeesCourseWiseDTO;
import com.demoproject.DTO.FeesAdminDto.FeesAdminResponseDTO;
import com.demoproject.DTO.FeesAdminDto.FeesAdminSignupDTO;
import com.demoproject.DTO.StudentDTO.StudentResponseWithProfileDTO;
import com.demoproject.Entity.*;
import com.demoproject.Service.*;
import com.demoproject.Service.AttendenceErpService.ERPActionAuditService;
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
    @Autowired
    private ERPActionAuditService erpActionAuditService;

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
        long totalFeesCourseWise = domainAdminService.getCourseCount(domain);

        Map<String, Long> response = new HashMap<>();
        response.put("students", totalStudent);
        response.put("faculty", totalFaculty);
        response.put("subAdmin", totalSubAdmin);
        response.put("feesAdmin", totalFeesAdmin);
        response.put("course",totalFeesCourseWise );

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
        recordDomainAdminAction(domain, "UPDATE_DOMAIN_ADMIN_PROFILE_PICTURE", "DOMAIN_ADMIN", email);

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
        recordDomainAdminAction(domain, "UPDATE_UNIVERSITY_LOGO", "UNIVERSITY", domain);

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
            if (save) {
                recordDomainAdminAction(domain, "UPDATE_DOMAIN_ADMIN_PASSWORD", "DOMAIN_ADMIN", email);
            }
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
            if (save) {
                recordDomainAdminAction(domain, "UPDATE_DOMAIN_ADMIN_PROFILE", "DOMAIN_ADMIN", "Profile updated");
            }
            return new ResponseEntity<>(save + " Update successfully \n", HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


// ===== Fees Add Course Wise and Get =====

    @PostMapping("/add-fees-course-wise")
    public ResponseEntity<ApiResponse<?>> addFeesCourseWise(@PathVariable String domain, Authentication authentication , @RequestBody FeesCourseWiseDTO feesCourseWiseDTO ) {

        String result = domainAdminService.addFeesCourseWise(domain, feesCourseWiseDTO);
        recordDomainAdminAction(domain, "CREATE_COURSE_FEES", "COURSE_FEES", feesCourseWiseDTO.getCourse());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, result, null));
    }



        @GetMapping("/all-fees-course-wise")
    public ResponseEntity<ApiResponse<?>> allFeesCourseWise(@PathVariable String domain, Authentication authentication ) {

        List<FeesCourseWiseDTO> feesCourseWise  = domainAdminService.getAllFeesCourseWise( domain );

        return ResponseEntity.ok()
                .body( new ApiResponse<>( true,null ,feesCourseWise ));
    }

    // ============================================================
// COURSE FEES - UPDATE
// ============================================================

    @PutMapping("/update-fees-course-wise")
    public ResponseEntity<ApiResponse<?>> updateFeesCourseWise(
            @PathVariable String domain,
            @RequestBody FeesCourseWiseDTO dto
    ) {
        try {
            String result = domainAdminService.updateFeesCourseWise( domain, dto);
            recordDomainAdminAction(domain, "UPDATE_COURSE_FEES", "COURSE_FEES", dto.getCourse());

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            result,
                            null
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(
                            new ApiResponse<>(
                                    false,
                                    e.getMessage(),
                                    null
                            )
                    );
        }
    }


// ============================================================
// COURSE FEES - DELETE
// ============================================================

    @DeleteMapping("/delete-fees-course-wise")
    public ResponseEntity<ApiResponse<?>> deleteFeesCourseWise(
            @PathVariable String domain,
            @RequestParam String course
    ) {

        try {

            String result =
                    domainAdminService.deleteFeesCourseWise(
                            domain,
                            course
                    );
            recordDomainAdminAction(domain, "DELETE_COURSE_FEES", "COURSE_FEES", course);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            result,
                            null
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(
                            new ApiResponse<>(
                                    false,
                                    e.getMessage(),
                                    null
                            )
                    );
        }
    }


    @PostMapping("/fees/course/excel")
    public ResponseEntity<BulkUploadResultDTO>
    uploadFeesCourseWiseFromExcel(
            @PathVariable String domain,
            @RequestParam("files") MultipartFile[] files
    ) {

        BulkUploadResultDTO result = domainAdminService.uploadFeesCourseWiseFromExcel(domain, files);
        recordDomainAdminAction(domain, "BULK_UPLOAD_COURSE_FEES", "COURSE_FEES",
                "Saved " + result.getSavedRows() + " rows");
        return ResponseEntity.ok(result);
    }






// ===== STUDENT CRUD =====

    // ---- Add Student ------
    @PostMapping("/add_student")
    public ResponseEntity<?> addStudent(@PathVariable String domain, @RequestBody StudentSignupDTO s) {
        try {
            String save = studentService.addStudent(domain, s);
            recordDomainAdminAction(domain, "CREATE_STUDENT", "STUDENT", s.getEmail());
            return new ResponseEntity<>(save, HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // ------ READ ALL student for specific university ------
//    @GetMapping("/all_student")
//    public List<StudentResponseDTO> getAllStudents(@PathVariable String domain  ,Authentication authentication) {
//
//        Set<String> roles = authentication.getAuthorities().stream()
//                .map(authority -> authority.getAuthority())
//                .collect(Collectors.toSet());
//        return studentService.getAllStudent(domain, roles);
//    }


    // ------ UPDATE Student Profile by Email ------
    @PutMapping("/update_student_profile")
    public Boolean updateStudentByEmail(@PathVariable String domain, @RequestBody Student s) {
        Boolean updated = studentService.updateStudentByEmail(domain, s);
        if (Boolean.TRUE.equals(updated)) {
            recordDomainAdminAction(domain, "UPDATE_STUDENT", "STUDENT", s.getEmail());
        }
        return updated;
    }

    // Update Password or Forget Password
    @PutMapping("/update_student_password")
    public ResponseEntity<?> updateStudentPassword(@PathVariable String domain,
                                                   @RequestParam String email, @RequestParam String newpass) {

        try {

            boolean save = studentService.updatePasswordByEmail(domain, email, newpass);
            if (save) {
                recordDomainAdminAction(domain, "UPDATE_STUDENT_PASSWORD", "STUDENT", email);
            }
            return new ResponseEntity<>(save + " Password change successfully \n", HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    // DELETE By email
    @DeleteMapping("/delete_student")
    public String deleteByEmail(@PathVariable String domain, @RequestParam String email) {
        String result = studentService.deleteStudentByEmail(domain, email);
        if (result.startsWith("Deleted")) {
            recordDomainAdminAction(domain, "DELETE_STUDENT", "STUDENT", email);
        }
        return result;
    }


// ===== FACULTY CRUD =====

    // ---- CREATE ------
    @PostMapping("/add_faculty")
    public ResponseEntity<?> addFaculty(@PathVariable String domain, @RequestBody FacultySignupDTO f) {
        try {
            String save = facultyService.addFaculty(domain, f);
            recordDomainAdminAction(domain, "CREATE_FACULTY", "FACULTY", f.getEmail());
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
            if (save) {
                recordDomainAdminAction(domain, "UPDATE_FACULTY_PASSWORD", "FACULTY", email);
            }
            return new ResponseEntity<>(save + " Password change successfully \n", HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    // ------ UPDATE Faculty Profile by Email ------
    @PutMapping("/update_faculty_profile")
    public Boolean updateFacultyByFacultyId(@PathVariable String domain, @RequestBody Faculty faculty) {
        Boolean updated = facultyService.updateFacultyByFacultyEmail(domain, faculty);
        if (Boolean.TRUE.equals(updated)) {
            recordDomainAdminAction(domain, "UPDATE_FACULTY", "FACULTY", faculty.getEmail());
        }
        return updated;
    }

    // ------ DELETE by faculty Email ------
    @DeleteMapping("/delete_faculty")
    public String deleteFacultyByEmail(@PathVariable String domain, @RequestParam String email) {
        String result = facultyService.deleteFacultyByEmail(domain, email);
        if (result.startsWith("Deleted")) {
            recordDomainAdminAction(domain, "DELETE_FACULTY", "FACULTY", email);
        }
        return result;
    }


    // ===== SUBADMIN CRUD =====

    // CREATE
    @PostMapping("/add_subAdmin")
    public ResponseEntity<?> addSubAdmin(@PathVariable String domain, @RequestBody SubAdminSignupDTO subAdmin) {
        try {
            String save = subAdminService.addSubAdmin(domain, subAdmin);
            recordDomainAdminAction(domain, "CREATE_SUB_ADMIN", "SUB_ADMIN", subAdmin.getEmail());
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
    public Boolean updateSubAdminByEmail(@PathVariable String domain, @RequestBody SubAdmin s) {
        Boolean updated = subAdminService.updateSubAdminByEmail(domain, s);
        if (Boolean.TRUE.equals(updated)) {
            recordDomainAdminAction(domain, "UPDATE_SUB_ADMIN", "SUB_ADMIN", s.getEmail());
        }
        return updated;
    }


    // Update Password or Forget Password
    @PutMapping("/update_subAdmin_password")
    public ResponseEntity<?> updateSubAdminPassword(@PathVariable String domain, @RequestParam String email, @RequestParam String newpass) {
        try {

            boolean save = subAdminService.updatePasswordByEmail(domain, email, newpass);
            if (save) {
                recordDomainAdminAction(domain, "UPDATE_SUB_ADMIN_PASSWORD", "SUB_ADMIN", email);
            }
            return new ResponseEntity<>(save + " Password change successfully \n", HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // ------ DELETE SubAdmin Profile by Email  ------
    @DeleteMapping("/delete_subAdmin")
    public String deleteSubAdminByEmail(@PathVariable String domain, @RequestParam String email) {
        String result = subAdminService.deleteSubAdminByEmail(domain, email);
        if (result.startsWith("Deleted")) {
            recordDomainAdminAction(domain, "DELETE_SUB_ADMIN", "SUB_ADMIN", email);
        }
        return result;
    }


    // ===== FEESADMIN CRUD =====

    // CREATE
    @PostMapping("/add_feesAdmin")
    public ResponseEntity<?> addFeesAdmin(@PathVariable String domain, @RequestBody FeesAdminSignupDTO feesAdmin) {
        try {
            String save = feesAdminService.addFeesAdmin(domain, feesAdmin);
            recordDomainAdminAction(domain, "CREATE_FEES_ADMIN", "FEES_ADMIN", feesAdmin.getEmail());
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
    public FeesAdminResponseDTO updateFeesAdminByEmail(@PathVariable String domain, @RequestBody FeesAdmin feesAdmin) {
        FeesAdminResponseDTO updated = feesAdminService.updateFeesAdminByEmail(domain, feesAdmin);
        if (updated != null) {
            recordDomainAdminAction(domain, "UPDATE_FEES_ADMIN", "FEES_ADMIN", updated.getEmail());
        }
        return updated;
    }

    // Update Password or Forget Password
    @PutMapping("/update_feesAdmin_password")
    public ResponseEntity<?> updateFeesAdminPassword(@PathVariable String domain, @RequestParam String email, @RequestParam String newpass) {
        try {

            boolean save = feesAdminService.updatePasswordByEmail(domain, email, newpass);
            if (save) {
                recordDomainAdminAction(domain, "UPDATE_FEES_ADMIN_PASSWORD", "FEES_ADMIN", email);
            }
            return new ResponseEntity<>(save + " Password change successfully \n", HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // ------ DELETE feesAdmin Profile by Email  ------
    @DeleteMapping("/delete_feesAdmin")
    public String deleteFeesAdminByEmail(@PathVariable String domain, @RequestParam String email) {
        String result = feesAdminService.deleteFeesAdminByEmail(domain, email);
        if (result.startsWith("Deleted")) {
            recordDomainAdminAction(domain, "DELETE_FEES_ADMIN", "FEES_ADMIN", email);
        }
        return result;
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
            BulkUploadResultDTO resultDTO =  domainAdminService.uploadStudentsFromExcel(domain, files);
            recordBulkUpload(domain, "BULK_UPLOAD_STUDENTS", "STUDENT", resultDTO);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "Students uploaded successfully",
                            resultDTO
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
            BulkUploadResultDTO resultDTO = domainAdminService.uploadFacultyFromExcel(domain, files);
            recordBulkUpload(domain, "BULK_UPLOAD_FACULTY", "FACULTY", resultDTO);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "Faculty uploaded successfully",
                            resultDTO
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
            BulkUploadResultDTO resultDTO = domainAdminService.uploadSubAdminFromExcel(domain, files);
            recordBulkUpload(domain, "BULK_UPLOAD_SUB_ADMINS", "SUB_ADMIN", resultDTO);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "SubAdmin uploaded successfully",
                            resultDTO
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
            BulkUploadResultDTO resultDTO = domainAdminService.uploadFeesAdminFromExcel(domain, files);
            recordBulkUpload(domain, "BULK_UPLOAD_FEES_ADMINS", "FEES_ADMIN", resultDTO);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "FeesAdmin uploaded successfully",
                            resultDTO
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private void recordBulkUpload(
            String domain,
            String action,
            String targetType,
            BulkUploadResultDTO result
    ) {
        recordDomainAdminAction(
                domain,
                action,
                targetType,
                "Saved " + result.getSavedRows() + " of " + result.getTotalRows()
                        + " rows; " + result.getDuplicateRows() + " duplicates and "
                        + result.getInvalidRows() + " invalid"
        );
    }

    private void recordDomainAdminAction(String domain, String action, String targetType, String details) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        erpActionAuditService.log(
                domain,
                authentication.getName(),
                "DOMAIN_ADMIN",
                action,
                targetType,
                null,
                details
        );
    }

}