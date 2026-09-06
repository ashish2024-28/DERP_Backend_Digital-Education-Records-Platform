package com.demoproject.DTO.AttendenceErp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Map;

/**
 * Attendance payload sent by a faculty member.
 *
 * Academic session is intentionally NOT accepted from the client.
 * The server derives the semester from the attendance date and the
 * domain's semester configuration.
 */
public record AttendanceMarkRequest(

        @NotBlank
        String teachingBatch,

        @NotBlank
        String subject,

        @NotNull
        LocalDate attendanceDate,

        @NotNull
        Integer periodNumber,

        @NotEmpty
        Map<String, String> attendance

) {
}
