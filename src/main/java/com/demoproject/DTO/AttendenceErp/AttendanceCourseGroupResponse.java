package com.demoproject.DTO.AttendenceErp;

import java.util.List;

public record AttendanceCourseGroupResponse(
        String course,
        int studentCount,
        long totalClasses,
        long present,
        long absent,
        double percentage,
        List<AttendanceBatchGroupResponse> studyBatches
) {
}