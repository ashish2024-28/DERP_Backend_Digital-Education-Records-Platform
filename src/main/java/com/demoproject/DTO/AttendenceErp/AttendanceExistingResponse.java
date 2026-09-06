package com.demoproject.DTO.AttendenceErp;

import java.util.Map;

public record AttendanceExistingResponse(
        boolean alreadyMarked,
        Map<String, String> attendance
) {
}