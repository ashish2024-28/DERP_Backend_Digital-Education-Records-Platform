//package com.demoproject.Controller.AttendenceErpController;
//
//import com.demoproject.DTO.AttendenceErp.AttendanceMarkRequest;
//import com.demoproject.DTO.AttendenceErp.AttendanceStudentResponse;
//import com.demoproject.DTO.AttendenceErp.AttendanceSummaryResponse;
//import com.demoproject.DTO.AttendenceErp.StudentAttendanceResponse;
//import com.demoproject.Entity.DomainAdmin;
//import com.demoproject.Entity.Faculty;
//import com.demoproject.Entity.SubAdmin;
//import com.demoproject.Repository.DomainAdminRepository;
//import com.demoproject.Repository.SubAdminRepository;
//import com.demoproject.Service.AttendenceErpService.AttendanceService;
//import com.demoproject.Repository.FacultyRepository;
//
//import jakarta.validation.Valid;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/{domain}/erp/attendance")
//@RequiredArgsConstructor
//public class AttendenceErpController {
//
//    private final AttendanceService attendanceService;
//    private final FacultyRepository facultyRepository;
//    private final SubAdminRepository subAdminRepository;
//    private final DomainAdminRepository domainAdminRepository;
//
//
//
//    // =========================================================
//    // FACULTY
//    // =========================================================
//
//    /*
//     * Faculty selects:
//     *
//     * 2A + JAVA
//     *
//     * and gets all students.
//     */
//    @GetMapping("/faculty/students")
//    @PreAuthorize("hasRole('FACULTY')")
//    public ResponseEntity<List<AttendanceStudentResponse>> getStudentsForFaculty(
//            @RequestParam String batch, @RequestParam String subject, Authentication authentication
//    ) {
//
//        return ResponseEntity.ok(
//                attendanceService.getStudentsForFaculty(
//                                authentication.getName(),batch,subject
//                        )
//        );
//    }
//
//
//    /*
//     * Faculty marks attendance.
//     */
//    @PostMapping("/faculty/mark")
//    @PreAuthorize("hasRole('FACULTY')")
//    public ResponseEntity<String> markAttendance(
//
//            @Valid
//            @RequestBody AttendanceMarkRequest request,
//
//            Authentication authentication
//    ) {
//
//        attendanceService.markAttendance(
//                authentication.getName(),
//                request
//        );
//
//        return ResponseEntity.ok(
//                "Attendance saved successfully"
//        );
//    }
//
//
//    /*
//     * Faculty's assignments.
//     *
//     * Frontend can use this to create:
//     *
//     * Batch dropdown
//     * Subject dropdown
//     */
////    @GetMapping("/faculty/assignments")
////    @PreAuthorize("hasRole('FACULTY')")
////    public ResponseEntity<?> getAssignments(
////            Authentication authentication
////    ) {
////
////        Faculty faculty =
////                facultyRepository
////                        .findByEmail(
////                                authentication.getName()
////                        )
////                        .orElseThrow(() ->
////                                new IllegalArgumentException(
////                                        "Faculty not found"
////                                ));
////
////        return ResponseEntity.ok(
////                faculty.getTeachingAssignmentsMap()
////        );
////    }
//
//
//    // =========================================================
//    // STUDENT
//    // =========================================================
//
//    /*
//     * Student sees:
//     *
//     * all subjects
//     * permanent totals
//     * last 7 days
//     */
//    @GetMapping("/student/me")
//    @PreAuthorize("hasRole('STUDENT')")
//    public ResponseEntity<StudentAttendanceResponse>
//    myAttendance(
//
//            @RequestParam String academicSession,
//
//            Authentication authentication
//    ) {
//
//        return ResponseEntity.ok(
//                attendanceService
//                        .getStudentAttendance(
//                                authentication.getName(),
//                                academicSession
//                        )
//        );
//    }
//
//
//    // =========================================================
//    // SUBADMIN
//    // =========================================================
//
//    /*
//     * SubAdmin selects 2A and gets
//     * attendance of all 2A students.
//     */
//    @GetMapping("/subadmin/batch")
//    @PreAuthorize("hasRole('SUB_ADMIN')")
//    public ResponseEntity<List<AttendanceSummaryResponse>> subAdminBatch(
//
//            @RequestParam String batch,
//
//            @RequestParam String academicSession,
//
//            Authentication authentication
//    ) {
//
//        SubAdmin subAdmin =
//                subAdminRepository
//                        .findByEmail(authentication.getName())
//                        .orElseThrow(() ->
//                                new IllegalArgumentException(
//                                        "SubAdmin not found"
//                                ));
//
//
//        return ResponseEntity.ok(
//                attendanceService.getBatchAttendance(
//                        subAdmin.getDomain(),
//                        batch,
//                        academicSession
//                )
//        );
//    }
//
//    // =========================================================
//    // DOMAIN ADMIN
//    // =========================================================
//
//    /*
//     * DomainAdmin can inspect a batch.
//     */
//    @GetMapping("/admin/batch")
//    @PreAuthorize("hasRole('DOMAIN_ADMIN')")
//    public ResponseEntity<List<AttendanceSummaryResponse>> adminBatch(
//
//            @RequestParam String batch,
//
//            @RequestParam String academicSession,
//
//            Authentication authentication
//    ) {
//
//        DomainAdmin admin =
//                domainAdminRepository
//                        .findByEmail(authentication.getName())
//                        .orElseThrow(() ->
//                                new IllegalArgumentException(
//                                        "Domain admin not found"
//                                ));
//
//        return ResponseEntity.ok(
//                attendanceService.getBatchAttendance(
//                        admin.getDomain(),
//                        batch,
//                        academicSession
//                )
//        );
//    }
//
//    /*
//     * DomainAdmin deletes/corrects attendance.
//     */
//    @DeleteMapping("/admin/{attendanceId}")
//    @PreAuthorize("hasRole('DOMAIN_ADMIN')")
//    public ResponseEntity<String> deleteAttendance(
//
//            @PathVariable Long attendanceId,
//
//            @RequestParam String domain,
//
//            Authentication authentication
//    ) {
//
//        attendanceService.deleteAttendance(
//                attendanceId,
//                domain,
//                authentication.getName(),
//                "DOMAIN_ADMIN"
//        );
//
//        return ResponseEntity.ok(
//                "Attendance deleted successfully"
//        );
//    }
//}