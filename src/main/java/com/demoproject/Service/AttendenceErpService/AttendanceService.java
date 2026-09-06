package com.demoproject.Service.AttendenceErpService;

import com.demoproject.DTO.AttendenceErp.*;
import com.demoproject.Entity.AttendenceErp.AttendanceAggregate;
import com.demoproject.Entity.AttendenceErp.AttendanceRecord;
import com.demoproject.Entity.AttendenceErp.AttendanceRetentionConfig;
import com.demoproject.Entity.AttendenceErp.AttendanceStatus;
import com.demoproject.Entity.Faculty;
import com.demoproject.Entity.Student;
import com.demoproject.Repository.AttendenceErpRepository.AttendanceAggregateRepository;
import com.demoproject.Repository.AttendenceErpRepository.AttendanceRecordRepository;
import com.demoproject.Repository.AttendenceErpRepository.AttendanceRetentionConfigRepository;
import com.demoproject.Repository.FacultyRepository;
import com.demoproject.Repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private static final int DEFAULT_RETENTION_MONTHS = 12;
    private static final int MIN_RETENTION_MONTHS = 1;
    private static final int MAX_RETENTION_MONTHS = 120;

    private final AttendanceRecordRepository recordRepository;
    private final AttendanceAggregateRepository aggregateRepository;
    private final AttendanceRetentionConfigRepository retentionConfigRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final ERPActionAuditService auditService;

    // =========================================================
    // FACULTY SETUP
    // =========================================================

    @Transactional(readOnly = true)
    public FacultyAttendanceSetupResponse getFacultySetup(
            String domain,
            String facultyEmail
    ) {
        Faculty faculty = findFaculty(domain, facultyEmail);

        return new FacultyAttendanceSetupResponse(
                normalize(faculty.getCourse()),
                faculty.getTeachingAssignments() == null
                        ? ""
                        : faculty.getTeachingAssignments()
        );
    }

    // =========================================================
    // GET STUDENTS FOR FACULTY
    // =========================================================

    @Transactional(readOnly = true)
    public List<AttendanceStudentResponse> getStudentsForFaculty(String domain, String facultyEmail, String batch, String subject ) {

        Faculty faculty = findFaculty(domain, facultyEmail);

        String requestedBatch = normalize(batch);
        String requestedSubject = normalize(subject);


        if (!faculty.teaches(requestedBatch, requestedSubject)) {
            throw new SecurityException(
                    "You are not assigned to " + requestedSubject + " for batch " + requestedBatch
            );
        }
        String facultyCourse = normalize(faculty.getCourse());

        return studentRepository
//                .findByDomainAndCourseAndStudyBatchOrderByRollNumberAsc(
                .findByDomainIgnoreCaseAndCourseIgnoreCaseAndStudyBatchIgnoreCaseOrderByRollNumberAsc(
                        faculty.getDomain(),facultyCourse,requestedBatch
                )
                .stream()
                .filter(student -> student.studiesSubject(requestedSubject))
                .map(student -> new AttendanceStudentResponse(
                        student.getRollNumber(),
                        student.getName(),
                        student.getFatherName(),
                        student.getStudyBatch(),
                        student.getBranch()
                ))
                .toList();
    }

    // =========================================================
    // MARK ATTENDANCE
    // =========================================================

    @Transactional
    public void markAttendance(String domain, String facultyEmail, AttendanceMarkRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Attendance request is required"
            );
        }

        Faculty faculty = findFaculty(domain, facultyEmail);

        String batch = normalize(request.teachingBatch());
        String subject = normalize(request.subject());

        validateAttendanceDate(request.attendanceDate());

        if (!faculty.teaches(batch, subject)) {
            throw new SecurityException(
                    "You are not assigned to "+ subject+ " for batch "+ batch
            );
        }

        String facultyCourse = normalize(faculty.getCourse());

        List<Student> students =
                studentRepository
//                        .findByDomainAndCourseAndStudyBatchOrderByRollNumberAsc(
                        .findByDomainIgnoreCaseAndCourseIgnoreCaseAndStudyBatchIgnoreCaseOrderByRollNumberAsc(

                                faculty.getDomain(),
                                facultyCourse,
                                batch
                        );

        if (students.isEmpty()) {
            throw new IllegalArgumentException(
                    "No students found for this teaching batch"
            );
        }

        Map<String, String> submitted = request.attendance();

        if (submitted == null || submitted.isEmpty()) {
            throw new IllegalArgumentException(
                    "Attendance data is required"
            );
        }

        int processed = 0;

        for (Student student : students) {

            if (!student.studiesSubject(subject)) {
                continue;
            }

            String statusValue =
                    submitted.get(student.getRollNumber());

            if (statusValue == null) {
                continue;
            }

            saveAttendance(
                    faculty,
                    student,
                    batch,
                    subject,
                    request.attendanceDate(),
                    request.periodNumber(),
                    parseStatus(statusValue)
            );

            processed++;
        }

        if (processed == 0) {
            throw new IllegalArgumentException(
                    "No valid student attendance was submitted"
            );
        }

        auditService.log(
                faculty.getDomain(),
                faculty.getEmail(),
                String.valueOf(faculty.getRole()),
                "MARK_ATTENDANCE",
                "ATTENDANCE",
                null,
                "Batch=" + batch
                        + ", Subject=" + subject
                        + ", Date=" + request.attendanceDate()
                        + ", Period=" + request.periodNumber()
                        + ", Students=" + processed
        );
    }

    // =========================================================
    // SAVE / UPDATE ATTENDANCE
    // =========================================================

    private void saveAttendance(
            Faculty faculty,
            Student student,
            String batch,
            String subject,
            LocalDate date,
            Integer periodNumber,
            AttendanceStatus newStatus
    ) {

        Optional<AttendanceRecord> existing =
                recordRepository
                        .findByStudent_IdAndSubjectAndAttendanceDateAndPeriodNumber(
                                student.getId(),
                                subject,
                                date,
                                periodNumber
                        );

        if (existing.isPresent()) {

            AttendanceRecord record = existing.get();

            AttendanceStatus oldStatus = record.getStatus();

            if (oldStatus == newStatus) {
                return;
            }

            record.setStatus(newStatus);
            record.setMarkedBy(faculty);

            recordRepository.save(record);

            AttendanceAggregate aggregate =
                    getOrCreateAggregate(
                            student,
                            subject,
                            faculty.getDomain()
                    );

            if (oldStatus == AttendanceStatus.PRESENT) {

                aggregate.setPresentCount(
                        Math.max(
                                0,
                                aggregate.getPresentCount() - 1
                        )
                );

                aggregate.setAbsentCount(
                        aggregate.getAbsentCount() + 1
                );

            } else {

                aggregate.setAbsentCount(
                        Math.max(
                                0,
                                aggregate.getAbsentCount() - 1
                        )
                );

                aggregate.setPresentCount(
                        aggregate.getPresentCount() + 1
                );
            }

            aggregateRepository.save(aggregate);

            return;
        }

        AttendanceRecord record = new AttendanceRecord();

        record.setDomain(faculty.getDomain());
        record.setTeachingBatch(batch);
        record.setStudyBatch(student.getStudyBatch());
        record.setSubject(subject);
        record.setAttendanceDate(date);
        record.setPeriodNumber(periodNumber);
        record.setStatus(newStatus);
        record.setStudent(student);
        record.setMarkedBy(faculty);

        recordRepository.save(record);

        AttendanceAggregate aggregate =
                getOrCreateAggregate(
                        student,
                        subject,
                        faculty.getDomain()
                );

        aggregate.setTotalClasses(
                aggregate.getTotalClasses() + 1
        );

        if (newStatus == AttendanceStatus.PRESENT) {

            aggregate.setPresentCount(
                    aggregate.getPresentCount() + 1
            );

        } else {

            aggregate.setAbsentCount(
                    aggregate.getAbsentCount() + 1
            );
        }

        aggregateRepository.save(aggregate);
    }

    // =========================================================
// CHECK EXISTING ATTENDANCE (for a batch/subject/date/period)
// =========================================================

    @Transactional(readOnly = true)
    public AttendanceExistingResponse getExistingAttendance(
            String domain,
            String facultyEmail,
            String batch,
            String subject,
            LocalDate date,
            Integer periodNumber
    ) {

        Faculty faculty = findFaculty(domain, facultyEmail);

        String normalizedBatch = normalize(batch);
        String normalizedSubject = normalize(subject);

        if (!faculty.teaches(normalizedBatch, normalizedSubject)) {
            throw new SecurityException(
                    "You are not assigned to " + normalizedSubject + " for batch " + normalizedBatch
            );
        }

        if (date == null) {
            throw new IllegalArgumentException("Attendance date is required");
        }

        if (periodNumber == null || periodNumber < 1) {
            throw new IllegalArgumentException("Valid period is required");
        }

        List<AttendanceRecord> existingRecords =
                recordRepository.findByDomainAndTeachingBatchAndSubjectAndAttendanceDateAndPeriodNumber(
                        faculty.getDomain(),
                        normalizedBatch,
                        normalizedSubject,
                        date,
                        periodNumber
                );

        Map<String, String> attendanceMap = new LinkedHashMap<>();

        for (AttendanceRecord record : existingRecords) {
            attendanceMap.put(
                    record.getStudent().getRollNumber(),
                    record.getStatus().name().equals("PRESENT") ? "P" : "A"
            );
        }

        return new AttendanceExistingResponse(
                !existingRecords.isEmpty(),
                attendanceMap
        );
    }

    // =========================================================
    // AGGREGATE
    // =========================================================

    private AttendanceAggregate getOrCreateAggregate(
            Student student,
            String subject,
            String domain
    ) {

        return aggregateRepository
                .findByStudent_IdAndSubject(
                        student.getId(),
                        subject
                )
                .orElseGet(() -> {

                    AttendanceAggregate aggregate =
                            new AttendanceAggregate();

                    aggregate.setDomain(domain);
                    aggregate.setSubject(subject);
                    aggregate.setStudent(student);

                    return aggregateRepository.save(aggregate);
                });
    }

    // =========================================================
    // STUDENT - CURRENT USER
    // =========================================================

    @Transactional(readOnly = true)
    public StudentAttendanceResponse getStudentAttendance(
            String email,
            String domain
    ) {

        String normalizedDomain =
                normalizeDomain(domain);

        String normalizedEmail =
                email == null
                        ? ""
                        : email.trim().toLowerCase();

        if (normalizedEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Authenticated student email is required"
            );
        }

        Student student =
                studentRepository
                        .findByEmail(normalizedEmail)
                        .filter(
                                s -> normalizedDomain
                                        .equalsIgnoreCase(
                                                s.getDomain()
                                        )
                        )
                        .orElseThrow(() ->
                                new SecurityException(
                                        "Student account does not belong to this domain"
                                )
                        );

        return buildStudentAttendance(
                student,
                normalizedDomain
        );
    }

    // =========================================================
    // STUDENT BY ROLL NUMBER
    // =========================================================

    @Transactional(readOnly = true)
    public StudentAttendanceResponse getStudentAttendanceById(
            String rollNumber,
            String domain
    ) {

        String normalizedDomain =
                normalizeDomain(domain);

        Student student =
                studentRepository
                        .findByRollNumberAndDomain(
                                normalize(rollNumber),
                                normalizedDomain
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Student not found"
                                )
                        );

        return buildStudentAttendance(
                student,
                normalizedDomain
        );
    }

    // =========================================================
    // BUILD STUDENT ATTENDANCE
    // =========================================================

    private StudentAttendanceResponse buildStudentAttendance(
            Student student,
            String domain
    ) {

        List<String> subjects =
                student.getStudySubjectsList();

        List<AttendanceAggregate> aggregates =
                aggregateRepository
                        .findByDomainAndStudent_Id(
                                domain,
                                student.getId()
                        );

        List<AttendanceSummaryResponse> summaries =
                aggregates
                        .stream()
                        .map(aggregate ->
                                new AttendanceSummaryResponse(
                                        student.getName(),
                                        student.getRollNumber(),
                                        null,
                                        student.getStudyBatch(),
                                        aggregate.getSubject(),
                                        aggregate.getTotalClasses(),
                                        aggregate.getPresentCount(),
                                        aggregate.getAbsentCount(),
                                        percentage(
                                                aggregate.getPresentCount(),
                                                aggregate.getTotalClasses()
                                        )
                                )
                        )
                        .toList();

        LocalDate fromDate =
                LocalDate.now().minusDays(6);

        List<AttendanceDayResponse> recent =
                recordRepository
                        .findByStudent_IdAndAttendanceDateGreaterThanEqualOrderByAttendanceDateDesc(
                                student.getId(),
                                fromDate
                        )
                        .stream()
                        .filter(record ->
                                domain.equalsIgnoreCase(
                                        record.getDomain()
                                )
                        )
                        .map(record ->
                                new AttendanceDayResponse(
                                        record.getAttendanceDate(),
                                        record.getPeriodNumber(),
                                        record.getSubject(),
                                        record.getStatus().name()
                                )
                        )
                        .toList();

        return new StudentAttendanceResponse(
                student.getName(),
                student.getRollNumber(),
                student.getCourse(),
                student.getBranch(),
                student.getBatch(),
                student.getStudyBatch(),
                subjects,
                summaries,
                recent
        );
    }

    // =========================================================
    // BATCH ATTENDANCE
    // =========================================================

    @Transactional(readOnly = true)
    public List<AttendanceSummaryResponse> getBatchAttendance(
            String domain,
            String batch
    ) {

        String normalizedDomain =
                normalizeDomain(domain);

        String normalizedBatch =
                normalize(batch);

        List<Student> students =
                studentRepository
                        .findByDomainAndStudyBatchOrderByRollNumberAsc(
                                normalizedDomain,
                                normalizedBatch
                        );

        List<AttendanceSummaryResponse> response =
                new ArrayList<>();

        for (Student student : students) {

            aggregateRepository
                    .findByDomainAndStudent_Id(
                            normalizedDomain,
                            student.getId()
                    )
                    .forEach(aggregate ->
                            response.add(
                                    new AttendanceSummaryResponse(
                                            student.getName(),
                                            student.getRollNumber(),
                                            normalizedBatch,
                                            student.getStudyBatch(),
                                            aggregate.getSubject(),
                                            aggregate.getTotalClasses(),
                                            aggregate.getPresentCount(),
                                            aggregate.getAbsentCount(),
                                            percentage(
                                                    aggregate.getPresentCount(),
                                                    aggregate.getTotalClasses()
                                            )
                                    )
                            )
                    );
        }

        return response;
    }

    // =========================================================
    // DELETE SINGLE ATTENDANCE
    // =========================================================

    @Transactional
    public void deleteAttendance(
            Long attendanceId,
            String domain,
            String actorEmail,
            String role
    ) {

        String normalizedDomain =
                normalizeDomain(domain);

        AttendanceRecord record =
                recordRepository
                        .findById(attendanceId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Attendance record not found"
                                )
                        );

        if (record.getDomain() == null ||
                !record.getDomain()
                        .equalsIgnoreCase(normalizedDomain)) {

            throw new SecurityException(
                    "Attendance belongs to another domain"
            );
        }

        AttendanceAggregate aggregate =
                aggregateRepository
                        .findByStudent_IdAndSubject(
                                record.getStudent().getId(),
                                record.getSubject()
                        )
                        .orElse(null);

        if (aggregate != null) {

            aggregate.setTotalClasses(
                    Math.max(
                            0,
                            aggregate.getTotalClasses() - 1
                    )
            );

            if (record.getStatus() ==
                    AttendanceStatus.PRESENT) {

                aggregate.setPresentCount(
                        Math.max(
                                0,
                                aggregate.getPresentCount() - 1
                        )
                );

            } else {

                aggregate.setAbsentCount(
                        Math.max(
                                0,
                                aggregate.getAbsentCount() - 1
                        )
                );
            }

            if (aggregate.getTotalClasses() == 0) {

                aggregateRepository.delete(aggregate);

            } else {

                aggregateRepository.save(aggregate);
            }
        }

        recordRepository.delete(record);

        auditService.log(
                normalizedDomain,
                actorEmail,
                role,
                "DELETE_ATTENDANCE",
                "ATTENDANCE",
                attendanceId,
                "Deleted attendance for student="
                        + record.getStudent().getRollNumber()
                        + ", subject="
                        + record.getSubject()
                        + ", date="
                        + record.getAttendanceDate()
                        + ", period="
                        + record.getPeriodNumber()
        );
    }

    // =========================================================
    // RETENTION CONFIG
    // =========================================================

    @Transactional
    public AttendanceRetentionResponse getRetentionConfig(
            String domain
    ) {

        String normalizedDomain =
                normalizeDomain(domain);

        AttendanceRetentionConfig config =
                getOrCreateRetentionConfig(
                        normalizedDomain
                );

        return new AttendanceRetentionResponse(
                config.getDomain(),
                config.getRetentionMonths(),
                config.getEnabled()
        );
    }

    @Transactional
    public AttendanceRetentionResponse updateRetentionConfig(
            String domain,
            Integer retentionMonths,
            Boolean enabled,
            String actorEmail
    ) {

        String normalizedDomain =
                normalizeDomain(domain);

        validateRetentionMonths(retentionMonths);

        if (enabled == null) {
            throw new IllegalArgumentException(
                    "Enabled value is required"
            );
        }

        AttendanceRetentionConfig config =
                getOrCreateRetentionConfig(
                        normalizedDomain
                );

        config.setRetentionMonths(retentionMonths);
        config.setEnabled(enabled);
        config.setUpdatedAt(
                java.time.Instant.now()
        );

        AttendanceRetentionConfig saved =
                retentionConfigRepository.save(config);

        auditService.log(
                normalizedDomain,
                actorEmail,
                "DOMAIN_ADMIN",
                "UPDATE_ATTENDANCE_RETENTION",
                "ATTENDANCE_RETENTION",
                saved.getId(),
                "RetentionMonths="
                        + retentionMonths
                        + ", Enabled="
                        + enabled
        );

        return new AttendanceRetentionResponse(
                saved.getDomain(),
                saved.getRetentionMonths(),
                saved.getEnabled()
        );
    }

    private AttendanceRetentionConfig
    getOrCreateRetentionConfig(String domain) {

        return retentionConfigRepository
                .findByDomainIgnoreCase(domain)
                .orElseGet(() -> {

                    AttendanceRetentionConfig config =
                            new AttendanceRetentionConfig();

                    config.setDomain(domain);
                    config.setRetentionMonths(
                            DEFAULT_RETENTION_MONTHS
                    );
                    config.setEnabled(true);

                    return retentionConfigRepository.save(
                            config
                    );
                });
    }

    // =========================================================
    // AUTOMATIC CLEANUP
    // =========================================================

    @Transactional
    public void cleanupOldAttendance() {

        Set<String> domains =
                new HashSet<>(
                        recordRepository.findDistinctDomains()
                );

        domains.addAll(
                retentionConfigRepository
                        .findAll()
                        .stream()
                        .map(
                                AttendanceRetentionConfig::getDomain
                        )
                        .filter(Objects::nonNull)
                        .map(this::normalizeDomain)
                        .collect(Collectors.toSet())
        );

        LocalDate today =
                LocalDate.now();

        for (String domain : domains) {

            AttendanceRetentionConfig config =
                    getOrCreateRetentionConfig(domain);

            if (!Boolean.TRUE.equals(
                    config.getEnabled()
            )) {
                continue;
            }

            LocalDate cutoffDate =
                    today.minusMonths(
                            config.getRetentionMonths()
                    );

            long deleted =
                    recordRepository
                            .deleteByDomainAndAttendanceDateBefore(
                                    domain,
                                    cutoffDate
                            );

            if (deleted > 0) {

                rebuildAggregatesForDomain(
                        domain
                );

                auditService.log(
                        domain,
                        "SYSTEM",
                        "SYSTEM",
                        "AUTO_DELETE_ATTENDANCE",
                        "ATTENDANCE",
                        null,
                        "Deleted="
                                + deleted
                                + ", CutoffDate="
                                + cutoffDate
                );
            }
        }
    }

    // =========================================================
    // MANUAL DATE RANGE CLEANUP
    // =========================================================

    @Transactional
    public long cleanupAttendanceByDateRange(
            String domain,
            LocalDate fromDate,
            LocalDate toDate,
            String actorEmail
    ) {

        String normalizedDomain =
                normalizeDomain(domain);

        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException(
                    "From date and to date are required"
            );
        }

        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException(
                    "From date cannot be after to date"
            );
        }

        long deleted =
                recordRepository
                        .deleteByDomainAndAttendanceDateBetween(
                                normalizedDomain,
                                fromDate,
                                toDate
                        );

        if (deleted > 0) {

            rebuildAggregatesForDomain(
                    normalizedDomain
            );
        }

        auditService.log(
                normalizedDomain,
                actorEmail,
                "DOMAIN_ADMIN",
                "MANUAL_DELETE_ATTENDANCE",
                "ATTENDANCE",
                null,
                "From="
                        + fromDate
                        + ", To="
                        + toDate
                        + ", Deleted="
                        + deleted
        );

        return deleted;
    }

    // =========================================================
    // REBUILD AGGREGATES
    // =========================================================

    @Transactional
    public void rebuildAggregatesForDomain(
            String domain
    ) {

        List<AttendanceRecord> records =
                recordRepository
                        .findByDomainOrderByAttendanceDateAsc(
                                domain
                        );

        aggregateRepository.deleteByDomain(domain);

        Map<String, List<AttendanceRecord>> grouped =
                records.stream()
                        .collect(
                                Collectors.groupingBy(
                                        record ->
                                                record.getStudent()
                                                        .getId()
                                                        + "|"
                                                        + record.getSubject(),
                                        LinkedHashMap::new,
                                        Collectors.toList()
                                )
                        );

        for (List<AttendanceRecord> group :
                grouped.values()) {

            AttendanceRecord first =
                    group.get(0);

            AttendanceAggregate aggregate =
                    new AttendanceAggregate();

            aggregate.setDomain(domain);
            aggregate.setSubject(
                    first.getSubject()
            );
            aggregate.setStudent(
                    first.getStudent()
            );

            aggregate.setTotalClasses(
                    group.size()
            );

            aggregate.setPresentCount(
                    group.stream()
                            .filter(
                                    r -> r.getStatus()
                                            == AttendanceStatus.PRESENT
                            )
                            .count()
            );

            aggregate.setAbsentCount(
                    group.stream()
                            .filter(
                                    r -> r.getStatus()
                                            == AttendanceStatus.ABSENT
                            )
                            .count()
            );

            aggregateRepository.save(
                    aggregate
            );
        }
    }

    // =========================================================
// FULL ATTENDANCE OVERVIEW — course-wise → studyBatch-wise → student-wise
// =========================================================

    @Transactional(readOnly = true)
    public AttendanceOverviewResponse getAttendanceOverview(
            String domain,
            String course,      // optional — null/blank means "all courses"
            String studyBatch    // optional — null/blank means "all batches"
    ) {

        String normalizedDomain = normalizeDomain(domain);

        String normalizedCourse =
                (course == null || course.isBlank()) ? null : normalize(course);

        String normalizedBatch =
                (studyBatch == null || studyBatch.isBlank()) ? null : normalize(studyBatch);

        List<Student> students;

        if (normalizedCourse != null && normalizedBatch != null) {
            students = studentRepository
                    .findByDomainIgnoreCaseAndCourseIgnoreCaseAndStudyBatchIgnoreCaseOrderByRollNumberAsc(
                            normalizedDomain, normalizedCourse, normalizedBatch
                    );
        } else if (normalizedCourse != null) {
            students = studentRepository
                    .findByDomainIgnoreCaseAndCourseIgnoreCaseOrderByRollNumberAsc(
                            normalizedDomain, normalizedCourse
                    );
        } else if (normalizedBatch != null) {
            students = studentRepository
                    .findByDomainIgnoreCaseAndStudyBatchIgnoreCaseOrderByRollNumberAsc(
                            normalizedDomain, normalizedBatch
                    );
        } else {
            students = studentRepository
                    .findByDomainIgnoreCaseOrderByCourseAscStudyBatchAscRollNumberAsc(
                            normalizedDomain
                    );
        }

        // course -> studyBatch -> students
        Map<String, Map<String, List<AttendanceStudentSummaryResponse>>> grouped =
                new LinkedHashMap<>();

        long overallTotal = 0, overallPresent = 0, overallAbsent = 0;
        int overallStudentCount = 0;

        for (Student student : students) {

            List<AttendanceAggregate> aggregates =
                    aggregateRepository.findByDomainAndStudent_Id(
                            normalizedDomain, student.getId()
                    );

            if (aggregates.isEmpty()) {
                continue; // skip students with no attendance recorded at all
            }

            List<AttendanceSummaryResponse> subjectSummaries = aggregates.stream()
                    .map(agg -> new AttendanceSummaryResponse(
                            student.getName(),
                            student.getRollNumber(),
                            null,
                            student.getStudyBatch(),
                            agg.getSubject(),
                            agg.getTotalClasses(),
                            agg.getPresentCount(),
                            agg.getAbsentCount(),
                            percentage(agg.getPresentCount(), agg.getTotalClasses())
                    ))
                    .toList();

            long studentTotal = aggregates.stream().mapToLong(AttendanceAggregate::getTotalClasses).sum();
            long studentPresent = aggregates.stream().mapToLong(AttendanceAggregate::getPresentCount).sum();
            long studentAbsent = aggregates.stream().mapToLong(AttendanceAggregate::getAbsentCount).sum();

            AttendanceStudentSummaryResponse studentSummary = new AttendanceStudentSummaryResponse(
                    student.getRollNumber(),
                    student.getName(),
                    student.getFatherName(),
                    student.getBranch(),
                    subjectSummaries,
                    studentTotal,
                    studentPresent,
                    studentAbsent,
                    percentage(studentPresent, studentTotal)
            );

            grouped
                    .computeIfAbsent(student.getCourse(), k -> new LinkedHashMap<>())
                    .computeIfAbsent(student.getStudyBatch(), k -> new ArrayList<>())
                    .add(studentSummary);

            overallTotal += studentTotal;
            overallPresent += studentPresent;
            overallAbsent += studentAbsent;
            overallStudentCount++;
        }

        List<AttendanceCourseGroupResponse> courseGroups = new ArrayList<>();

        for (var courseEntry : grouped.entrySet()) {

            String courseName = courseEntry.getKey();
            List<AttendanceBatchGroupResponse> batchGroups = new ArrayList<>();

            long courseTotal = 0, coursePresent = 0, courseAbsent = 0;
            int courseStudentCount = 0;

            for (var batchEntry : courseEntry.getValue().entrySet()) {

                String batchName = batchEntry.getKey();
                List<AttendanceStudentSummaryResponse> studentList = batchEntry.getValue();

                long batchTotal = studentList.stream().mapToLong(AttendanceStudentSummaryResponse::totalClasses).sum();
                long batchPresent = studentList.stream().mapToLong(AttendanceStudentSummaryResponse::present).sum();
                long batchAbsent = studentList.stream().mapToLong(AttendanceStudentSummaryResponse::absent).sum();

                batchGroups.add(new AttendanceBatchGroupResponse(
                        batchName,
                        studentList.size(),
                        batchTotal,
                        batchPresent,
                        batchAbsent,
                        percentage(batchPresent, batchTotal),
                        studentList
                ));

                courseTotal += batchTotal;
                coursePresent += batchPresent;
                courseAbsent += batchAbsent;
                courseStudentCount += studentList.size();
            }

            courseGroups.add(new AttendanceCourseGroupResponse(
                    courseName,
                    courseStudentCount,
                    courseTotal,
                    coursePresent,
                    courseAbsent,
                    percentage(coursePresent, courseTotal),
                    batchGroups
            ));
        }

        return new AttendanceOverviewResponse(
                normalizedDomain,
                overallStudentCount,
                overallTotal,
                overallPresent,
                overallAbsent,
                percentage(overallPresent, overallTotal),
                courseGroups
        );
    }



    // =========================================================
// FACULTY - RECENT ATTENDANCE (last 7 days, all taught batches/subjects)
// =========================================================

    @Transactional(readOnly = true)
    public FacultyRecentAttendanceResponse getRecentAttendance(
            String domain,
            String facultyEmail
    ) {

        Faculty faculty = findFaculty(domain, facultyEmail);

        LocalDate fromDate = LocalDate.now().minusDays(6);

        List<AttendanceRecord> records =
                recordRepository
                        .findByDomainAndMarkedBy_IdAndAttendanceDateGreaterThanEqualOrderByAttendanceDateDescPeriodNumberAsc(
                                faculty.getDomain(),
                                faculty.getId(),
                                fromDate
                        );

        // teachingBatch -> subject -> "date|period" -> records
        Map<String, Map<String, Map<String, List<AttendanceRecord>>>> grouped =
                new LinkedHashMap<>();

        for (AttendanceRecord record : records) {

            String sessionKey = record.getAttendanceDate() + "|" + record.getPeriodNumber();

            grouped
                    .computeIfAbsent(record.getTeachingBatch(), k -> new LinkedHashMap<>())
                    .computeIfAbsent(record.getSubject(), k -> new LinkedHashMap<>())
                    .computeIfAbsent(sessionKey, k -> new ArrayList<>())
                    .add(record);
        }

        List<RecentBatchGroupResponse> batchGroups = new ArrayList<>();

        for (var batchEntry : grouped.entrySet()) {

            List<RecentSubjectGroupResponse> subjectGroups = new ArrayList<>();

            for (var subjectEntry : batchEntry.getValue().entrySet()) {

                List<RecentSessionResponse> sessions = new ArrayList<>();

                for (var sessionEntry : subjectEntry.getValue().entrySet()) {

                    List<AttendanceRecord> sessionRecords = sessionEntry.getValue();
                    String[] parts = sessionEntry.getKey().split("\\|");
                    LocalDate date = LocalDate.parse(parts[0]);
                    Integer period = Integer.valueOf(parts[1]);

                    List<RecentStudentStatusResponse> studentStatuses =
                            sessionRecords.stream()
                                    .sorted(Comparator.comparing(r -> r.getStudent().getRollNumber()))
                                    .map(r -> new RecentStudentStatusResponse(
                                            r.getStudent().getRollNumber(),
                                            r.getStudent().getName(),
                                            r.getStatus().name()
                                    ))
                                    .toList();

                    long present = sessionRecords.stream()
                            .filter(r -> r.getStatus() == AttendanceStatus.PRESENT)
                            .count();
                    long absent = sessionRecords.size() - present;

                    sessions.add(new RecentSessionResponse(
                            date,
                            period,
                            sessionRecords.size(),
                            (int) present,
                            (int) absent,
                            studentStatuses
                    ));
                }

                sessions.sort((a, b) -> {
                    int dateCompare = b.date().compareTo(a.date());
                    if (dateCompare != 0) return dateCompare;
                    return b.periodNumber().compareTo(a.periodNumber());
                });

                subjectGroups.add(new RecentSubjectGroupResponse(subjectEntry.getKey(), sessions));
            }

            batchGroups.add(new RecentBatchGroupResponse(batchEntry.getKey(), subjectGroups));
        }

        return new FacultyRecentAttendanceResponse(batchGroups);
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private Faculty findFaculty(
            String domain,
            String email
    ) {

        String normalizedDomain =
                normalizeDomain(domain);

        String normalizedEmail =
                email == null
                        ? ""
                        : email.trim().toLowerCase();

        if (normalizedEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Authenticated faculty email is required"
            );
        }

        return facultyRepository
                .findByEmailAndDomain(
                        normalizedEmail,
                        normalizedDomain
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Faculty not found"
                        )
                );
    }

    private void validateAttendanceDate(
            LocalDate date
    ) {

        if (date == null) {
            throw new IllegalArgumentException(
                    "Attendance date is required"
            );
        }

        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Future attendance is not allowed"
            );
        }
    }




    private void validateRetentionMonths(
            Integer value
    ) {

        if (value == null ||
                value < MIN_RETENTION_MONTHS ||
                value > MAX_RETENTION_MONTHS) {

            throw new IllegalArgumentException(
                    "Retention must be between 1 and 120 months"
            );
        }
    }

    private double percentage(
            long present,
            long total
    ) {

        return total == 0
                ? 0.0
                : present * 100.0 / total;
    }

    private AttendanceStatus parseStatus(
            String status
    ) {

        if (status == null ||
                status.isBlank()) {

            throw new IllegalArgumentException(
                    "Attendance status is required"
            );
        }

        return switch (
                status.trim().toUpperCase()
                ) {

            case "P", "PRESENT" ->
                    AttendanceStatus.PRESENT;

            case "A", "ABSENT" ->
                    AttendanceStatus.ABSENT;

            default ->
                    throw new IllegalArgumentException(
                            "Status must be P/PRESENT or A/ABSENT"
                    );
        };
    }

    private String normalizeDomain(
            String value
    ) {

        if (value == null ||
                value.isBlank()) {

            throw new IllegalArgumentException(
                    "Domain is required"
            );
        }

        return value.trim().toLowerCase();
    }

    private String normalize(
            String value
    ) {

        if (value == null ||
                value.isBlank()) {

            throw new IllegalArgumentException(
                    "Value is required"
            );
        }

        return value.trim().toUpperCase();
    }
}