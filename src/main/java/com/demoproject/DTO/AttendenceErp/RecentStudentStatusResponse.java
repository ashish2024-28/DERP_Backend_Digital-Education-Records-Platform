package com.demoproject.DTO.AttendenceErp;

public record RecentStudentStatusResponse(
        String rollNumber,
        String studentName,
        String status // "PRESENT" or "ABSENT"
) {
}