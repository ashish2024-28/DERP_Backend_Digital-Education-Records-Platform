package com.demoproject.DTO.AttendenceErp;

import java.util.List;

public record RecentSubjectGroupResponse(
        String subject,
        List<RecentSessionResponse> sessions
) {
}