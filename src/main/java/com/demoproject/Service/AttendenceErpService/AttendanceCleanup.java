//package com.demoproject.Service.AttendenceErpService;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class AttendanceCleanup {
//
// private final AttendanceService attendanceService;
//
// /**
//  * Runs every 7 days at 00:05.
//  *
//  * Cron:
//  * second minute hour day-of-month month day-of-week
//  */
// @Scheduled(cron = "0 5 0 */7 * *")
// public void cleanupAttendance() {
//
//  log.info(
//          "Starting automatic attendance cleanup..."
//  );
//
//  try {
//
//   attendanceService.cleanupOldAttendance();
//
//   log.info(
//           "Automatic attendance cleanup completed."
//   );
//
//  } catch (Exception e) {
//
//   log.error(
//           "Automatic attendance cleanup failed",
//           e
//   );
//  }
// }
////}












package com.demoproject.Service.AttendenceErpService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AttendanceCleanup {

 private final AttendanceService attendanceService;

 /**
  * Runs every 7 days.
  *
  * 604800000 milliseconds = 7 days.
  */
 @Scheduled(
         fixedDelay = 604800000,
         initialDelay = 60000
 )
 public void cleanupAttendance() {

  log.info(
          "Starting automatic attendance cleanup..."
  );

  try {

   attendanceService.cleanupOldAttendance();

   log.info(
           "Automatic attendance cleanup completed."
   );

  } catch (Exception e) {

   log.error(
           "Automatic attendance cleanup failed",
           e
   );
  }
 }
}