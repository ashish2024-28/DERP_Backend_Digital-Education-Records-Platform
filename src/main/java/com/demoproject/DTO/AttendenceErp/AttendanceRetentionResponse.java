package com.demoproject.DTO.AttendenceErp;

public record AttendanceRetentionResponse(
        String domain,
        Integer retentionMonths,
        Boolean enabled
) {
}