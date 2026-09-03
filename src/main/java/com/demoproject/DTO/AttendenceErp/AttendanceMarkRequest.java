package com.demoproject.DTO.AttendenceErp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Map;

public record AttendanceMarkRequest(

        @NotBlank
        String teachingBatch,

        @NotBlank
        String subject,

        @NotNull
        LocalDate attendanceDate,

        @NotNull
        Integer periodNumber,

        @NotBlank
        String academicSession,

        @NotEmpty
        Map<Long, String> attendance
) {
}