package com.demoproject.DTO.AttendenceErp;

import java.util.List;

public record StudentAttendanceResponse(


        String studentName,

        String rollNumber,

        String course,

        String branch,

        String batch,

        String studyBatch,

        List<String> subjects,

        List<AttendanceSummaryResponse> summaries,

        List<AttendanceDayResponse> last7Days
) {
}