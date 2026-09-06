package com.demoproject.DTO.AttendenceErp;

import java.util.List;

public record RecentBatchGroupResponse(
        String teachingBatch,
        List<RecentSubjectGroupResponse> subjects
) {
}