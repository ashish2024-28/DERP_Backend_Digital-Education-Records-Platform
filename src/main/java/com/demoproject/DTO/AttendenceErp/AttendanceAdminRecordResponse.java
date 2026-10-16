package com.demoproject.DTO.AttendenceErp;

import java.time.LocalDate;

public record AttendanceAdminRecordResponse(
        Long id,
        String studentName,
        String rollNumber,
        String course,
        String studyBatch,
        String subject,
        LocalDate date,
        Integer periodNumber,
        String status,
        String facultyName
) {
}
