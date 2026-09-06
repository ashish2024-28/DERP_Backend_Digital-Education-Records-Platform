package com.demoproject.DTO.AttendenceErp;

import java.util.List;

public record FacultyRecentAttendanceResponse(
        List<RecentBatchGroupResponse> batches
) {
}