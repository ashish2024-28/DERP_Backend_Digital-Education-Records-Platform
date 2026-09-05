//package com.demoproject.Service.AttendenceErpService;
//
//import com.demoproject.DTO.AttendenceErp.*;
//import com.demoproject.Entity.AttendenceErp.*;
//import com.demoproject.Entity.Faculty;
//import com.demoproject.Entity.Student;
//import com.demoproject.Repository.AttendenceErpRepository.AttendanceAggregateRepository;
//import com.demoproject.Repository.AttendenceErpRepository.AttendanceRecordRepository;
//import com.demoproject.Repository.FacultyRepository;
//import com.demoproject.Repository.StudentRepository;
//
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class AttendanceService {
//
// private final AttendanceRecordRepository recordRepository;
// private final AttendanceAggregateRepository aggregateRepository;
// private final StudentRepository studentRepository;
// private final FacultyRepository facultyRepository;
// private final ERPActionAuditService auditService;
//
//
// // =========================================================
// // GET STUDENTS FOR FACULTY
// // =========================================================
// //
// // Faculty sends:
// //
// // batch   = 2A
// // subject = JAVA
// //
// // Backend checks:
// //
// // 1. Faculty exists
// // 2. Faculty teaches JAVA in 2A
// // 3. Student course == Faculty course
// // 4. Student studyBatch == 2A
// // 5. Student studies JAVA
// //
// // No student ID is required.
// // Roll number is used as the attendance key.
// //
// // =========================================================
//
// @Transactional(readOnly = true)
// public List<AttendanceStudentResponse> getStudentsForFaculty(
//         String facultyEmail,
//         String batch,
//         String subject
// ) {
//
//  // -----------------------------------------------------
//  // Validate request
//  // -----------------------------------------------------
//
//  String requestedBatch = normalize(batch);
//  String requestedSubject = normalize(subject);
//
//
//  // -----------------------------------------------------
//  // Find logged-in faculty
//  // -----------------------------------------------------
//
//  Faculty faculty =
//          facultyRepository
//                  .findByEmail(facultyEmail)
//                  .orElseThrow(() ->
//                          new IllegalArgumentException(
//                                  "Faculty not found"
//                          )
//                  );
//
//
//  // -----------------------------------------------------
//  // Get faculty course
//  // -----------------------------------------------------
//
//  String facultyCourse =
//          normalize(faculty.getCourse());
//
//
//  // -----------------------------------------------------
//  // Check faculty assignment
//  //
//  // Example:
//  //
//  // {
//  //     "2A": ["JAVA", "DSA"],
//  //     "2B": ["OS"]
//  // }
//  //
//  // -----------------------------------------------------
////
////  if (!faculty.teaches(
////          requestedBatch,
////          requestedSubject
////  )) {
////
////   throw new SecurityException(
////           "You are not assigned to "
////                   + requestedSubject
////                   + " for batch "
////                   + requestedBatch
////   );
////  }
//
//
//  // -----------------------------------------------------
//  // Find students using course + study batch.
//  //
//  // We intentionally do not use student ID.
//  // -----------------------------------------------------
//
//  List<Student> students =
//          studentRepository
//                  .findByDomainAndCourseAndStudyBatchOrderByRollNumberAsc(
//                          faculty.getDomain(),
//                          facultyCourse,
//                          requestedBatch
//                  );
//
//
//  // -----------------------------------------------------
//  // Final subject check
//  //
//  // Student must actually study the subject.
//  // -----------------------------------------------------
//
//  return students.stream()
//
//          .filter(student ->
//                  student.(
//                          requestedSubject
//                  )
//          )
//
//          .map(student ->
//                  new AttendanceStudentResponse(
//
//                          student.getRollNumber(),
//
//                          student.getName(),
//
//                          student.getFatherName(),
//
//                          student.getStudyBatch(),
//
//                          student.getBranch()
//                  )
//          )
//
//          .toList();
// }
//
//
// // =========================================================
// // MARK ATTENDANCE
// // =========================================================
//
// @Transactional
// public void markAttendance(
//         String facultyEmail,
//         AttendanceMarkRequest request
// ) {
//
//  Faculty faculty =
//          facultyRepository
//                  .findByEmail(facultyEmail)
//                  .orElseThrow(() ->
//                          new IllegalArgumentException(
//                                  "Faculty not found"
//                          )
//                  );
//
//
//  String batch =
//          normalize(request.teachingBatch());
//
//  String subject =
//          normalize(request.subject());
//
//
//  // -----------------------------------------------------
//  // Future date validation
//  // -----------------------------------------------------
//
//  if (request.attendanceDate()
//          .isAfter(LocalDate.now())) {
//
//   throw new IllegalArgumentException(
//           "Future attendance is not allowed"
//   );
//  }
//
//
//  // -----------------------------------------------------
//  // Faculty assignment validation
//  // -----------------------------------------------------
//
//  if (!faculty.teaches(batch, subject)) {
//
//   throw new SecurityException(
//           "Faculty is not assigned "
//                   + subject
//                   + " for batch "
//                   + batch
//   );
//  }
//
//
//  // -----------------------------------------------------
//  // Get only students belonging to:
//  //
//  // Faculty domain
//  // Faculty course
//  // Selected study batch
//  //
//  // -----------------------------------------------------
//
//  List<Student> students =
//          studentRepository
//                  .findByDomainAndCourseAndStudyBatchOrderByRollNumberAsc(
//                          faculty.getDomain(),
//                          faculty.getCourse(),
//                          batch
//                  );
//
//
//  // -----------------------------------------------------
//  // Process attendance
//  // -----------------------------------------------------
//
//  for (Student student : students) {
//
//   // Student must study selected subject.
//   if (!student.studiesSubject(subject)) {
//    continue;
//   }
//
//
//   // -------------------------------------------------
//   // Attendance map uses rollNumber.
//   // -------------------------------------------------
//
//   String statusValue =
//           request.attendance()
//                   .get(student.getRollNumber());
//
//
//   /*
//    * If faculty did not send this student,
//    * don't automatically mark absent.
//    */
//   if (statusValue == null) {
//    continue;
//   }
//
//
//   AttendanceStatus status;
//
//   try {
//
//    status = parseStatus(statusValue);
//
//   } catch (Exception exception) {
//
//    throw new IllegalArgumentException(
//            "Invalid attendance status for student "
//                    + student.getRollNumber()
//    );
//   }
//
//
//   saveAttendance(
//           faculty,
//           student,
//           batch,
//           subject,
//           request.attendanceDate(),
//           request.periodNumber(),
//           request.academicSession(),
//           status
//   );
//  }
//
//
//  // -----------------------------------------------------
//  // Audit
//  // -----------------------------------------------------
//
//  auditService.log(
//          faculty.getDomain(),
//          faculty.getEmail(),
//          String.valueOf(faculty.getRole()),
//          "MARK_ATTENDANCE",
//          "ATTENDANCE",
//          null,
//          "Batch=" + batch
//                  + ", Subject=" + subject
//                  + ", Date=" + request.attendanceDate()
//                  + ", Period=" + request.periodNumber()
//  );
// }
//
//
// // =========================================================
// // SAVE / UPDATE ATTENDANCE
// // =========================================================
//
// private void saveAttendance(
//         Faculty faculty,
//         Student student,
//         String batch,
//         String subject,
//         LocalDate date,
//         Integer periodNumber,
//         String academicSession,
//         AttendanceStatus newStatus
// ) {
//
//  var existing =
//          recordRepository
//                  .findByStudentRollNumberAndSubjectAndAttendanceDateAndPeriodNumberAndAcademicSession(
//                          student.getRollNumber(),
//                          subject,
//                          date,
//                          periodNumber,
//                          academicSession
//                  );
//
//
//  // =====================================================
//  // EXISTING RECORD
//  // =====================================================
//
//  if (existing.isPresent()) {
//
//   AttendanceRecord record =
//           existing.get();
//
//   AttendanceStatus oldStatus =
//           record.getStatus();
//
//
//   // Nothing changed.
//   if (oldStatus == newStatus) {
//    return;
//   }
//
//
//   record.setStatus(newStatus);
//   record.setMarkedBy(faculty);
//
//   recordRepository.save(record);
//
//
//   // -------------------------------------------------
//   // Correct aggregate
//   // -------------------------------------------------
//
//   AttendanceAggregate aggregate =
//           getOrCreateAggregate(
//                   student,
//                   subject,
//                   academicSession,
//                   faculty.getDomain()
//           );
//
//
//   if (oldStatus == AttendanceStatus.PRESENT) {
//
//    aggregate.setPresentCount(
//            Math.max(
//                    0,
//                    aggregate.getPresentCount() - 1
//            )
//    );
//
//    aggregate.setAbsentCount(
//            aggregate.getAbsentCount() + 1
//    );
//
//   } else {
//
//    aggregate.setAbsentCount(
//            Math.max(
//                    0,
//                    aggregate.getAbsentCount() - 1
//            )
//    );
//
//    aggregate.setPresentCount(
//            aggregate.getPresentCount() + 1
//    );
//   }
//
//
//   aggregateRepository.save(aggregate);
//
//   return;
//  }
//
//
//  // =====================================================
//  // NEW RECORD
//  // =====================================================
//
//  AttendanceRecord record =
//          new AttendanceRecord();
//
//  record.setDomain(faculty.getDomain());
//  record.setAcademicSession(academicSession);
//  record.setTeachingBatch(batch);
//  record.setStudyBatch(student.getStudyBatch());
//  record.setSubject(subject);
//  record.setAttendanceDate(date);
//  record.setPeriodNumber(periodNumber);
//  record.setStatus(newStatus);
//  record.setStudent(student);
//  record.setMarkedBy(faculty);
//
//  recordRepository.save(record);
//
//
//  // -----------------------------------------------------
//  // Update permanent aggregate
//  // -----------------------------------------------------
//
//  AttendanceAggregate aggregate =
//          getOrCreateAggregate(
//                  student,
//                  subject,
//                  academicSession,
//                  faculty.getDomain()
//          );
//
//
//  aggregate.setTotalClasses(
//          aggregate.getTotalClasses() + 1
//  );
//
//
//  if (newStatus == AttendanceStatus.PRESENT) {
//
//   aggregate.setPresentCount(
//           aggregate.getPresentCount() + 1
//   );
//
//  } else {
//
//   aggregate.setAbsentCount(
//           aggregate.getAbsentCount() + 1
//   );
//  }
//
//
//  aggregateRepository.save(aggregate);
// }
//
//
// // =========================================================
// // GET / CREATE AGGREGATE
// // =========================================================
//
// private AttendanceAggregate getOrCreateAggregate(
//         Student student,
//         String subject,
//         String academicSession,
//         String domain
// ) {
//
//  return aggregateRepository
//          .findByStudentRollNumberAndSubjectAndAcademicSession(
//                  student.getRollNumber(),
//                  subject,
//                  academicSession
//          )
//          .orElseGet(() -> {
//
//           AttendanceAggregate aggregate =
//                   new AttendanceAggregate();
//
//           aggregate.setDomain(domain);
//
//           aggregate.setAcademicSession(
//                   academicSession
//           );
//
//           aggregate.setSubject(subject);
//           aggregate.setStudent(student);
//
//           aggregate.setTotalClasses(0);
//           aggregate.setPresentCount(0);
//           aggregate.setAbsentCount(0);
//
//           return aggregateRepository.save(
//                   aggregate
//           );
//          });
// }
//
//
// // =========================================================
// // STUDENT ATTENDANCE
// // =========================================================
//
// @Transactional(readOnly = true)
// public StudentAttendanceResponse getStudentAttendance(
//         String email,
//         String academicSession
// ) {
//
//  Student student =
//          studentRepository
//                  .findByEmail(email)
//                  .orElseThrow(() ->
//                          new IllegalArgumentException(
//                                  "Student not found"
//                          )
//                  );
//
//  return buildStudentAttendance(
//          student,
//          academicSession
//  );
// }
//
//
// // =========================================================
// // STUDENT ATTENDANCE BY ROLL NUMBER
// // =========================================================
//
// @Transactional(readOnly = true)
// public StudentAttendanceResponse getStudentAttendanceById(
//         String rollNumber,
//         String domain,
//         String academicSession
// ) {
//
//  Student student =
//          studentRepository
//                  .findByRollNumberAndDomain(
//                          rollNumber,
//                          domain
//                  )
//                  .orElseThrow(() ->
//                          new IllegalArgumentException(
//                                  "Student not found"
//                          )
//                  );
//
//  return buildStudentAttendance(
//          student,
//          academicSession
//  );
// }
//
//
// // =========================================================
// // BUILD STUDENT ATTENDANCE
// // =========================================================
//
// private StudentAttendanceResponse buildStudentAttendance(
//         Student student,
//         String academicSession
// ) {
//
//  List<String> subjects =
//          student.getStudySubjectsList();
//
//
//  List<AttendanceAggregate> aggregates =
//          aggregateRepository
//                  .findByStudentRollNumberOrderBySubjectAsc(
//                          student.getRollNumber()
//                  )
//                  .stream()
//                  .filter(aggregate ->
//                          academicSession.equals(
//                                  aggregate.getAcademicSession()
//                          )
//                  )
//                  .toList();
//
//
//  List<AttendanceSummaryResponse> summaries =
//          new ArrayList<>();
//
//
//  for (AttendanceAggregate aggregate : aggregates) {
//
//   double percentage = 0;
//
//
//   if (aggregate.getTotalClasses() > 0) {
//
//    percentage =
//            aggregate.getPresentCount()
//                    * 100.0
//                    /
//                    aggregate.getTotalClasses();
//   }
//
//
//   summaries.add(
//           new AttendanceSummaryResponse(
//
//                   student.getName(),
//
//                   student.getRollNumber(),
//
//                   null,
//
//                   student.getStudyBatch(),
//
//                   aggregate.getSubject(),
//
//                   aggregate.getTotalClasses(),
//
//                   aggregate.getPresentCount(),
//
//                   aggregate.getAbsentCount(),
//
//                   percentage
//           )
//   );
//  }
//
//
//  // -----------------------------------------------------
//  // Last 7 days
//  // -----------------------------------------------------
//
//  LocalDate fromDate =
//          LocalDate.now().minusDays(6);
//
//
//  List<AttendanceDayResponse> recent =
//          recordRepository
//                  .findByStudentRollNumberAndAttendanceDateGreaterThanEqualOrderByAttendanceDateDesc(
//                          student.getRollNumber(),
//                          fromDate
//                  )
//                  .stream()
//                  .filter(record ->
//                          academicSession.equals(
//                                  record.getAcademicSession()
//                          )
//                  )
//                  .map(record ->
//                          new AttendanceDayResponse(
//
//                                  record.getAttendanceDate(),
//
//                                  record.getPeriodNumber(),
//
//                                  record.getSubject(),
//
//                                  record.getStatus().name()
//                          )
//                  )
//                  .toList();
//
//
//  return new StudentAttendanceResponse(
//
//          student.getName(),
//
//          student.getRollNumber(),
//
//          student.getCourse(),
//
//          student.getBranch(),
//
//          student.getBatch(),
//
//          student.getStudyBatch(),
//
//          subjects,
//
//          summaries,
//
//          recent
//  );
// }
//
//
// // =========================================================
// // BATCH ATTENDANCE
// // =========================================================
//
// @Transactional(readOnly = true)
// public List<AttendanceSummaryResponse> getBatchAttendance(
//         String domain,
//         String teachingBatch,
//         String academicSession
// ) {
//
//  domain = normalize(domain);
//  teachingBatch = normalize(teachingBatch);
//
//
//  if (academicSession == null ||
//          academicSession.isBlank()) {
//
//   throw new IllegalArgumentException(
//           "Academic session is required"
//   );
//  }
//
//
//  academicSession =
//          academicSession.trim();
//
//
//  List<Student> students =
//          studentRepository
//                  .findByDomainAndStudyBatchOrderByRollNumberAsc(
//                          domain,
//                          teachingBatch
//                  );
//
//
//  List<AttendanceSummaryResponse> response =
//          new ArrayList<>();
//
//
//  for (Student student : students) {
//
//   List<AttendanceAggregate> aggregates =
//           aggregateRepository
//                   .findByStudentRollNumberOrderBySubjectAsc(
//                           student.getRollNumber()
//                   );
//
//
//   for (AttendanceAggregate aggregate :
//           aggregates) {
//
//    if (!aggregate.getAcademicSession()
//            .equalsIgnoreCase(
//                    academicSession
//            )) {
//
//     continue;
//    }
//
//
//    double percentage =
//            aggregate.getTotalClasses() == 0
//                    ? 0
//                    : aggregate.getPresentCount()
//                    * 100.0
//                    /
//                    aggregate.getTotalClasses();
//
//
//    response.add(
//            new AttendanceSummaryResponse(
//
//                    student.getName(),
//
//                    student.getRollNumber(),
//
//                    teachingBatch,
//
//                    student.getStudyBatch(),
//
//                    aggregate.getSubject(),
//
//                    aggregate.getTotalClasses(),
//
//                    aggregate.getPresentCount(),
//
//                    aggregate.getAbsentCount(),
//
//                    percentage
//            )
//    );
//   }
//  }
//
//
//  return response;
// }
//
//
// // =========================================================
// // DOMAIN ADMIN DELETE ATTENDANCE
// // =========================================================
//
// @Transactional
// public void deleteAttendance(
//         Long attendanceId,
//         String domain,
//         String actorEmail,
//         String role
// ) {
//
//  AttendanceRecord record =
//          recordRepository
//                  .findById(attendanceId)
//                  .orElseThrow(() ->
//                          new IllegalArgumentException(
//                                  "Attendance record not found"
//                          )
//                  );
//
//
//  if (!record.getDomain()
//          .equalsIgnoreCase(domain)) {
//
//   throw new SecurityException(
//           "Attendance belongs to another domain"
//   );
//  }
//
//
//  AttendanceAggregate aggregate =
//          aggregateRepository
//                  .findByStudentRollNumberAndSubjectAndAcademicSession(
//                          record.getStudent()
//                                  .getRollNumber(),
//
//                          record.getSubject(),
//
//                          record.getAcademicSession()
//                  )
//                  .orElseThrow(() ->
//                          new IllegalStateException(
//                                  "Attendance aggregate not found"
//                          )
//                  );
//
//
//  aggregate.setTotalClasses(
//          Math.max(
//                  0,
//                  aggregate.getTotalClasses() - 1
//          )
//  );
//
//
//  if (record.getStatus()
//          == AttendanceStatus.PRESENT) {
//
//   aggregate.setPresentCount(
//           Math.max(
//                   0,
//                   aggregate.getPresentCount() - 1
//           )
//   );
//
//  } else {
//
//   aggregate.setAbsentCount(
//           Math.max(
//                   0,
//                   aggregate.getAbsentCount() - 1
//           )
//   );
//  }
//
//
//  aggregateRepository.save(aggregate);
//
//  recordRepository.delete(record);
//
//
//  auditService.log(
//          domain,
//          actorEmail,
//          role,
//          "DELETE_ATTENDANCE",
//          "ATTENDANCE",
//          attendanceId,
//          "Deleted attendance for student="
//                  + record.getStudent()
//                  .getRollNumber()
//                  + ", subject="
//                  + record.getSubject()
//  );
// }
//
//
// // =========================================================
// // CLEANUP
// // =========================================================
//
// @Transactional
// public void cleanup() {
//
//  LocalDate deleteBefore =
//          LocalDate.now().minusDays(6);
//
//  recordRepository.deleteByAttendanceDateBefore(
//          deleteBefore
//  );
// }
//
//
// // =========================================================
// // STATUS PARSER
// // =========================================================
//
// private AttendanceStatus parseStatus(
//         String status
// ) {
//
//  if (status == null) {
//
//   throw new IllegalArgumentException(
//           "Attendance status is required"
//   );
//  }
//
//
//  return switch (
//          status.trim().toUpperCase()
//          ) {
//
//   case "P", "PRESENT" ->
//           AttendanceStatus.PRESENT;
//
//   case "A", "ABSENT" ->
//           AttendanceStatus.ABSENT;
//
//   default ->
//           throw new IllegalArgumentException(
//                   "Status must be P/PRESENT or A/ABSENT"
//           );
//  };
// }
//
//
// // =========================================================
// // NORMALIZE STRING
// // =========================================================
//
// private String normalize(String value) {
//
//  if (value == null ||
//          value.isBlank()) {
//
//   throw new IllegalArgumentException(
//           "Value is required"
//   );
//  }
//
//  return value.trim().toUpperCase();
// }
//}