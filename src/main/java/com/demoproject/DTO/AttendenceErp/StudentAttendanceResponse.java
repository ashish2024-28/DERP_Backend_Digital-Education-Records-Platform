package com.demoproject.DTO.AttendenceErp;

import java.util.List;

public record StudentAttendanceResponse(


        String studentName,

        String rollNumber,

        String studyBatch,

        List<String> subjects,

        List<AttendanceSummaryResponse> summaries,

        List<AttendanceDayResponse> last7Days
) {
}