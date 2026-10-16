package com.demoproject.DTO.AttendenceErp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentAttendanceSubjectResponseDTO {
    private Long id;
    private String domain;
    private String subject;
    private long totalClasses;
    private long presentCount;
    private long absentCount;
}
