package com.demoproject.Controller.AttendenceErpController;

import com.demoproject.DTO.ApiResponse;
import com.demoproject.DTO.AttendenceErp.*;
import com.demoproject.Entity.DomainAdmin;
import com.demoproject.Entity.AttendenceErp.ERPActionAudit;
import com.demoproject.Entity.SubAdmin;
import com.demoproject.Repository.DomainAdminRepository;
import com.demoproject.Repository.SubAdminRepository;
import com.demoproject.Service.AttendenceErpService.ERPActionAuditService;
import com.demoproject.Service.AttendenceErpService.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/{domain}/erp/attendance")
@RequiredArgsConstructor
public class AttendanceErpController {

    private final AttendanceService attendanceService;
    private final ERPActionAuditService auditService;
    private final SubAdminRepository subAdminRepository;
    private final DomainAdminRepository domainAdminRepository;




    // =========================================================
    // FACULTY - GET ELIGIBLE STUDENTS
    // =========================================================

    @GetMapping("/faculty/subjects")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<List<String>>> getSubjectsForFaculty(
            @PathVariable String domain,
            @RequestParam String course,
            @RequestParam String studyBatch,
            Authentication authentication
    ) {
        List<String> subjects = attendanceService.getSubjectsForFaculty(
                domain, authentication.getName(), course, studyBatch
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Subjects fetched successfully",
                        subjects
                )
        );
    }

    @GetMapping("/faculty/students")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<List<AttendanceStudentResponse>>> getStudentsForFaculty(
            @PathVariable String domain,
            @RequestParam String studyBatch,
            @RequestParam String course,
            @RequestParam String subject,
            Authentication authentication
    ) {

        List<AttendanceStudentResponse> students =
                attendanceService.getStudentsForFaculty( domain, authentication.getName(), course, studyBatch, subject );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Eligible students fetched successfully",
                        students
                )
        );
    }


    // =========================================================
    // FACULTY - MARK ATTENDANCE
    // =========================================================

    @PostMapping("/faculty/mark")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<Void>> markAttendance(
            @PathVariable String domain,
            @Valid @RequestBody AttendanceMarkRequest request,
            Authentication authentication
    ) {

        attendanceService.markAttendance( domain, authentication.getName(), request );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Attendance saved successfully",
                        null
                )
        );
    }

    // =========================================================
// FACULTY - CHECK EXISTING ATTENDANCE
// =========================================================

    @GetMapping("/faculty/existing")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<AttendanceExistingResponse>> getExistingAttendance(
            @PathVariable String domain,
            @RequestParam String studentBatch,
            @RequestParam String subject,
            @RequestParam String date,
            @RequestParam Integer period,
            Authentication authentication
    ) {
        AttendanceExistingResponse data = attendanceService.getExistingAttendance(
                domain,
                authentication.getName(),
                studentBatch,
                subject,
                java.time.LocalDate.parse(date),
                period
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Existing attendance fetched successfully", data)
        );
    }

    // =========================================================
    // STUDENT - MY ATTENDANCE
    // =========================================================

    @GetMapping("/student/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentAttendanceResponse>> myAttendance(
            @PathVariable String domain,
            Authentication authentication
    ) {

        StudentAttendanceResponse attendance = attendanceService.getStudentAttendance(
                        authentication.getName(), domain );

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Attendance fetched successfully", attendance)
        );
    }

    // =========================================================
// FACULTY - RECENT ATTENDANCE (LAST 7 DAYS)
// =========================================================

    @GetMapping("/faculty/recent")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<ApiResponse<FacultyRecentAttendanceResponse>> getRecentAttendance(
            @PathVariable String domain,
            Authentication authentication
    ) {

        FacultyRecentAttendanceResponse data =
                attendanceService.getRecentAttendance(domain, authentication.getName());

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Recent attendance fetched successfully", data)
        );
    }


    // =========================================================
    // SUB ADMIN - BATCH ATTENDANCE
    // =========================================================

    @GetMapping("/subadmin/batch")
    @PreAuthorize("hasRole('SUB_ADMIN')")
    public ResponseEntity<ApiResponse<List<AttendanceSummaryResponse>>> subAdminBatch(
            @PathVariable String domain,
            @RequestParam String batch,
            Authentication authentication
    ) {

        SubAdmin subAdmin = subAdminRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("SubAdmin not found")
                );

        List<AttendanceSummaryResponse> attendance =
                attendanceService.getBatchAttendance(
                        subAdmin.getDomain(),
                        batch
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Batch attendance fetched successfully",
                        attendance
                )
        );
    }


    // =========================================================
    // DOMAIN ADMIN - BATCH REPORT
    // =========================================================

    @GetMapping("/admin/batch")
    @PreAuthorize("hasRole('DOMAIN_ADMIN') and #domain.equalsIgnoreCase(authentication.principal.domain)")
    public ResponseEntity<ApiResponse<List<AttendanceSummaryResponse>>> adminBatch(
            @PathVariable String domain,
            @RequestParam String batch,
            Authentication authentication
    ) {

        DomainAdmin admin = domainAdminRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Domain admin not found")
                );

        List<AttendanceSummaryResponse> attendance =
                attendanceService.getBatchAttendance(
                        admin.getDomain(),
                        batch
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Batch attendance fetched successfully",
                        attendance
                )
        );
    }


    // =========================================================
    // DOMAIN ADMIN - GET RETENTION CONFIG
    // =========================================================

    @GetMapping("/admin/retention")
    @PreAuthorize("hasRole('DOMAIN_ADMIN') and #domain.equalsIgnoreCase(authentication.principal.domain)")
    public ResponseEntity<ApiResponse<AttendanceRetentionResponse>> getRetention(
            @PathVariable String domain
    ) {

        AttendanceRetentionResponse retention =
                attendanceService.getRetentionConfig(domain);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Attendance retention configuration fetched successfully",
                        retention
                )
        );
    }


    // =========================================================
    // DOMAIN ADMIN - UPDATE RETENTION CONFIG
    // =========================================================

    @PutMapping("/admin/retention")
    @PreAuthorize("hasRole('DOMAIN_ADMIN') and #domain.equalsIgnoreCase(authentication.principal.domain)")
    public ResponseEntity<ApiResponse<AttendanceRetentionResponse>> updateRetention(
            @PathVariable String domain,
            @Valid @RequestBody AttendanceRetentionUpdateRequest request,
            Authentication authentication
    ) {

        AttendanceRetentionResponse retention =
                attendanceService.updateRetentionConfig(
                        domain,
                        request.retentionMonths(),
                        request.enabled(),
                        authentication.getName()
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Attendance retention configuration updated successfully",
                        retention
                )
        );
    }


    // =========================================================
    // DOMAIN ADMIN - CLEANUP BY DATE RANGE
    // =========================================================

    @DeleteMapping("/admin/cleanup")
    @PreAuthorize("hasRole('DOMAIN_ADMIN') and #domain.equalsIgnoreCase(authentication.principal.domain)")
    public ResponseEntity<ApiResponse<Long>> cleanupAttendanceByDateRange(
            @PathVariable String domain,
            @Valid @RequestBody AttendanceCleanupRequest request,
            Authentication authentication
    ) {

        long deleted =
                attendanceService.cleanupAttendanceByDateRange(
                        domain,
                        request.fromDate(),
                        request.toDate(),
                        authentication.getName()
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Attendance cleanup completed successfully",
                        deleted
                )
        );
    }


    // =========================================================
    // DOMAIN ADMIN - DELETE ATTENDANCE
    // =========================================================

    @DeleteMapping("/admin/{attendanceId}")
    @PreAuthorize("hasRole('DOMAIN_ADMIN') and #domain.equalsIgnoreCase(authentication.principal.domain)")
    public ResponseEntity<ApiResponse<Void>> deleteAttendance(
            @PathVariable String domain,
            @PathVariable Long attendanceId,
            Authentication authentication
    ) {

        DomainAdmin admin = domainAdminRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Domain admin not found")
                );

        attendanceService.deleteAttendance(
                attendanceId,
                admin.getDomain(),
                authentication.getName(),
                "DOMAIN_ADMIN"
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Attendance deleted successfully",
                        null
                )
        );
    }

    // =========================================================
// DOMAIN ADMIN - FULL ATTENDANCE OVERVIEW (course → batch → student)
// =========================================================

    @GetMapping("/admin/audit-logs")
    @PreAuthorize("hasRole('DOMAIN_ADMIN') and #domain.equalsIgnoreCase(authentication.principal.domain)")
    public ResponseEntity<ApiResponse<List<ERPActionAudit>>> getAuditLogs(
            @PathVariable String domain
    ) {
        List<ERPActionAudit> auditLogs = auditService.all(domain);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "ERP activity fetched successfully", auditLogs)
        );
    }

    @GetMapping("/admin/overview")
    @PreAuthorize("hasRole('DOMAIN_ADMIN') and #domain.equalsIgnoreCase(authentication.principal.domain)")
    public ResponseEntity<ApiResponse<AttendanceOverviewResponse>> getAttendanceOverview(
            @PathVariable String domain,
            @RequestParam(required = false) String course,
            @RequestParam(required = false) String batch,
            Authentication authentication
    ) {

        DomainAdmin admin = domainAdminRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Domain admin not found"));

        AttendanceOverviewResponse overview =
                attendanceService.getAttendanceOverview(admin.getDomain(), course, batch);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Attendance overview fetched successfully", overview)
        );
    }

    @GetMapping("/admin/cleanup-preview")
    @PreAuthorize("hasRole('DOMAIN_ADMIN') and #domain.equalsIgnoreCase(authentication.principal.domain)")
    public ResponseEntity<ApiResponse<Long>> previewAttendanceCleanup(
            @PathVariable String domain,
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate) {
        long recordCount = attendanceService.countAttendanceByDateRange(domain, fromDate, toDate);
        return ResponseEntity.ok(new ApiResponse<>(true, "Attendance cleanup preview calculated", recordCount));
    }

    @GetMapping("/admin/records")
    @PreAuthorize("hasRole('DOMAIN_ADMIN') and #domain.equalsIgnoreCase(authentication.principal.domain)")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<AttendanceAdminRecordResponse>>> adminRecords(
            @PathVariable String domain,
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Page must be non-negative and size must be between 1 and 100");
        }
        var records = attendanceService.getAdminRecords(
                domain, fromDate, toDate, PageRequest.of(page, size));
        return ResponseEntity.ok(new ApiResponse<>(true, "Attendance records fetched", records));
    }

}





/*

Your final controller endpoints

Now your Attendance ERP API becomes:

FACULTY

GET
/{domain}/erp/attendance/faculty/setup

GET
/{domain}/erp/attendance/faculty/students

POST
/{domain}/erp/attendance/faculty/mark

GET
/{domain}/erp/attendance/faculty/assignments

Student:

GET
/{domain}/erp/attendance/student/me

SubAdmin:

GET
/{domain}/erp/attendance/subadmin/batch

Domain Admin:

GET
/{domain}/erp/attendance/admin/batch

DELETE
/{domain}/erp/attendance/admin/{attendanceId}

GET
/{domain}/erp/attendance/admin/retention

PUT
/{domain}/erp/attendance/admin/retention

DELETE
/{domain}/erp/attendance/admin/cleanup








AttendanceService should perform the matching like:

Faculty course
      ↓
Student course

      ↓
requested batch + requested subject

Student studyBatch
      ↓
requested batch

Student studySubjects
      ↓
requested subject

For example:

Faculty:
course = BBA

*/