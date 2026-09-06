package com.demoproject.DTO.AttendenceErp;

import java.util.List;

public record AttendanceStudentSummaryResponse(
        String rollNumber,
        String studentName,
        String fatherName,
        String branch,
        List<AttendanceSummaryResponse> subjects, // per-subject breakdown
        long totalClasses,
        long present,
        long absent,
        double percentage
) {
}