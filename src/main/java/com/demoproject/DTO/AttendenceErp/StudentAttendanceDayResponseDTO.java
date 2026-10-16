package com.demoproject.DTO.AttendenceErp;

import com.demoproject.Entity.AttendenceErp.AttendanceStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class StudentAttendanceDayResponseDTO {
    private Long id;
    private String domain;
    private String subject;
    private LocalDate attendanceDate;
    private LocalTime attendanceTime;
    private Integer periodNumber;
    private AttendanceStatus status;
    private String markedByFacultyEmail;
}
