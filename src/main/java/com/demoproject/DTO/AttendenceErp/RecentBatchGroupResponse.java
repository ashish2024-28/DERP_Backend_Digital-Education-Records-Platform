package com.demoproject.DTO.AttendenceErp;

import java.util.List;

public record RecentBatchGroupResponse(
        String key, List<RecentSubjectGroupResponse> subjects
) {
}