package com.demoproject.DTO.AttendenceErp;

import java.time.LocalDate;
import java.util.List;

public record RecentSessionResponse(
        LocalDate date,
        Integer periodNumber,
        int totalCount,
        int presentCount,
        int absentCount,
        List<RecentStudentStatusResponse> students
) {
}