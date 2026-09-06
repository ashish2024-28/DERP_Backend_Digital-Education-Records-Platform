package com.demoproject.DTO.AttendenceErp;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AttendanceCleanupRequest(

        @NotNull
        LocalDate fromDate,

        @NotNull
        LocalDate toDate
) {
}