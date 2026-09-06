package com.demoproject.DTO.AttendenceErp;

import java.util.List;

public record AttendanceBatchGroupResponse(
        String studyBatch,
        int studentCount,
        long totalClasses,
        long present,
        long absent,
        double percentage,
        List<AttendanceStudentSummaryResponse> students
) {
}