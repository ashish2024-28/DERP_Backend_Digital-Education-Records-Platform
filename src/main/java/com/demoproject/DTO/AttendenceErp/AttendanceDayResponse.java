package com.demoproject.DTO.AttendenceErp;

import java.time.LocalDate;

public record AttendanceDayResponse(

        LocalDate date,

        Integer periodNumber,

        String subject,

        String status
) {
}