package com.demoproject.Service.AttendenceErpService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AttendanceCleanup {

 private final AttendanceService attendanceService;

 public AttendanceCleanup(
         AttendanceService attendanceService
 ) {
  this.attendanceService =
          attendanceService;
 }

 /*
  * Every day at 00:05.
  */
 @Scheduled(cron = "0 5 0 * * *")
 public void run() {

  attendanceService.cleanup();
 }
}