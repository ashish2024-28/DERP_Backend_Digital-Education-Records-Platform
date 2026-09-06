package com.demoproject.DTO.AttendenceErp;

import java.util.List;

public record AttendanceOverviewResponse(
        String domain,
        int totalStudents,
        long totalClasses,
        long present,
        long absent,
        double percentage,
        List<AttendanceCourseGroupResponse> courses
) {
}