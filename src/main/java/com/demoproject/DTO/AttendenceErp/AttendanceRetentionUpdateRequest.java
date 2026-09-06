package com.demoproject.DTO.AttendenceErp;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AttendanceRetentionUpdateRequest(

        @NotNull
        @Min(1)
        @Max(60)
        Integer retentionMonths,

        @NotNull
        Boolean enabled
) {
}