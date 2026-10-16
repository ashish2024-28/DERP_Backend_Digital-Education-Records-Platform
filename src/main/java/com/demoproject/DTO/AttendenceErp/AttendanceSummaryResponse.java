package com.demoproject.DTO.AttendenceErp;

public record AttendanceSummaryResponse(
        String studentName,
        String rollNumber,
        String studyBatch,
        String subject,
        Long totalClasses,
        Long present,
        Long absent,
        Double percentage
) {
}
