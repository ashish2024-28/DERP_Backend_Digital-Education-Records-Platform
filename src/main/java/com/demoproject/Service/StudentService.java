package com.demoproject.Service;

import com.demoproject.DTO.StudentDTO.StudentResponseDTO;
import com.demoproject.DTO.StudentDTO.StudentResponseWithProfileDTO;
import com.demoproject.DTO.StudentDTO.StudentSignupDTO;
import com.demoproject.DTO.AttendenceErp.StudentAttendanceDayResponseDTO;
import com.demoproject.DTO.AttendenceErp.StudentAttendanceSubjectResponseDTO;
import com.demoproject.DTO.CertificationResponseDTO;
import com.demoproject.DTO.FeesAccount.StudentFeesAccountDTO;
import com.demoproject.DTO.FeesAccount.StudentFeesPaymentResponseDTO;
import com.demoproject.Entity.AttendenceErp.StudentAttendanceRecordDayWise;
import com.demoproject.Entity.AttendenceErp.StudentTotalAttendanceSubjectWise;
import com.demoproject.Entity.FeesAccounts.FeesCourseWise;
import com.demoproject.Entity.FeesAccounts.StudentFeesPayment;
import com.demoproject.Entity.ProfileInformation.StudentInfo.Certifications;
import com.demoproject.Entity.FeesAccounts.StudentFeesAccount;
import com.demoproject.Entity.Role;
import com.demoproject.Entity.Student;
import com.demoproject.Entity.University;
import com.demoproject.Repository.FeesAcountsRepository.FeesCourseWiseRepository;
import com.demoproject.Repository.StudentRepository;
import com.demoproject.Repository.UniversityRepo;

import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final BaseUserService baseUserService;
    private final UniversityRepo universityRepo;
    private final ModelMapper modelMapper;
    private final FeesCourseWiseRepository feesCourseWiseRepository;

    @Qualifier("bcryptEncoder")
    private final PasswordEncoder passwordEncoder;


    // =========================================================
    // GET LOGGED-IN STUDENT
    // =========================================================

    @Transactional
    public StudentResponseWithProfileDTO getStudentByEmailAndDomain(String email, String domain) {

        Student student = studentRepository.findByEmailAndDomain( email, domain )
                        .orElseThrow(() ->new RuntimeException( "Student not found" ) );

        LocalDateTime previousLastLogin = student.getLastLoginDateTime();

        student.setLastLoginDateTime(LocalDateTime.now());

        student = studentRepository.save(student);
        StudentResponseWithProfileDTO response = toStudentProfileResponse(student, domain);
        response.setLastLoginDateTime(previousLastLogin);
        return response;
    }


    // =========================================================
    // LOGIN
    // =========================================================

//    public StudentResponseWithProfileDTO LoginStudent(
//            LoginRequestDTO loginRequestDTO) {
//
//        Student student =
//                studentRepository
//                        .findByEmailAndDomain(
//                                loginRequestDTO.getEmail(),
//                                loginRequestDTO.getDomain()
//                        )
//                        .orElse(null);
//
//        if (student == null) {
//            return null;
//        }
//
//        boolean passwordMatch =
//                passwordEncoder.matches(
//                        loginRequestDTO.getPassword(),
//                        student.getPassword()
//                );
//
//        if (!passwordMatch) {
//            return null;
//        }
//
//        student.setLastLoginDateTime(
//                LocalDateTime.now()
//        );
//
//        studentRepository.save(student);
//
//        return modelMapper.map(
//                student,
//                StudentResponseWithProfileDTO.class
//        );
//    }


    // =========================================================
    // PROFILE PICTURE
    // =========================================================

    public String updateProfilePic(
            String domain,
            String email,
            MultipartFile file)
            throws IOException {

        Student student =
                studentRepository
                        .findByDomainAndEmail(
                                domain,
                                email
                        );

        if (student == null) {
            throw new RuntimeException(
                    "Student not found"
            );
        }

        if (file == null || file.isEmpty()) {
            throw new RuntimeException(
                    "Profile picture is required"
            );
        }

        String uploadDir =
                "uploads/profile/";

        Files.createDirectories(
                Paths.get(uploadDir)
        );

        String originalName =
                file.getOriginalFilename();

        String fileName =
                System.currentTimeMillis()
                        + "_"
                        + (originalName == null
                        ? "profile"
                        : originalName);

        Path path =
                Paths.get(
                        uploadDir,
                        fileName
                );

        Files.write(
                path,
                file.getBytes()
        );

        student.setProfilePic(
                uploadDir + fileName
        );

        studentRepository.save(student);

        return uploadDir + fileName;
    }


    // =========================================================
    // EMAIL EXISTS
    // =========================================================

    public boolean emailVerifiy(
            String email) {

        return baseUserService
                .existsUserByEmail(email);
    }


    // =========================================================
    // CREATE STUDENT
    // =========================================================

    public String addStudent( String domain, StudentSignupDTO dto) {

        if (dto == null) {
            throw new RuntimeException( "Student data is required" );
        }

        if (baseUserService.existsUserByEmail(dto.getEmail())) {
            throw new RuntimeException("User already exists with this email.");
        }

        University university = universityRepo.findByDomain(domain)
                        .orElseThrow(() -> new RuntimeException("Invalid domain"));

        Student student = modelMapper.map(dto, Student.class );

        student.setDomain(domain);
        // set university
        student.setUniversity(university);
        student.setRole(Role.STUDENT);

        //fees Add here
        String course = student.getCourse() == null
                ? ""
                : student.getCourse().trim();
        FeesCourseWise feesCourseWise = feesCourseWiseRepository
                .findByUniversityAndCourseIgnoreCase(university, course)
                .orElseThrow(() -> new RuntimeException(
                        "Fees for student course '" + course + "' not found for this university"
                ));
        Double totalFees = feesCourseWise.getFees();

        StudentFeesAccount studentFeesAccount = new StudentFeesAccount();
        studentFeesAccount.setTotalFees(totalFees);
        studentFeesAccount.setStudent(student);

        student.setFeesAccount(studentFeesAccount);


        /* Password encryption. */
        student.setPassword( passwordEncoder.encode( student.getPassword()));

        if (studentRepository.existsByRollNumberAndDomain(student.getRollNumber(), domain)) {
            throw new RuntimeException( "Student Roll Number already exists." );
        }

        if (studentRepository.existsByDomainAndEmail( domain, student.getEmail())) {
            throw new RuntimeException( "Student email already exists.");
        }
        Student saved = studentRepository.save(student);

        return saved.getName()
                + ",\nYour account was created successfully."
                + "\nRoll Number : "
                + saved.getRollNumber();
    }


    // =========================================================
    // STUDENT COUNT
    // =========================================================

    public long getStudentCount(
            String domain) {

        return studentRepository
                .countByUniversity_Domain(domain);
    }


    // =========================================================
    // ALL STUDENTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<StudentResponseDTO> getAllStudent(String domain, Set<String> roles) {

        List<StudentResponseDTO> students = studentRepository
                .findAllByDomain(domain)
                .stream()
                .map(student -> modelMapper.map(
                        student,
                        StudentResponseDTO.class
                ))
                .toList();

        students.forEach(student -> {
            if (student.getFeesAccount() != null) {
                double totalFees = student.getFeesAccount().getTotalFees() == null
                        ? 0.0 : student.getFeesAccount().getTotalFees();
                double totalPaid = student.getFeesAccount().getTotalPaid() == null
                        ? 0.0 : student.getFeesAccount().getTotalPaid();
                student.getFeesAccount().setTotalDues(totalFees - totalPaid);
            }
        });

        // DOMAIN_ADMIN -> everything
        if (roles.contains("ROLE_DOMAIN_ADMIN")) {
            return students;
        }

        // FEES_ADMIN -> only fees related fields
        if (roles.contains("ROLE_FEES_ADMIN")) {
            return students;
        }

        // FACULTY / SUB_ADMIN -> fees fields hide
        if (roles.contains("ROLE_FACULTY")
                || roles.contains("ROLE_SUB_ADMIN")) {

            students.forEach(student -> {

                student.setFeesAccount(null);

            });

            return students;
        }

        // Any other role -> hide sensitive fields
        students.forEach(student -> {

            student.setFeesAccount(null);
        });

        return students;
    }

    // Get student with profile
    @Transactional(readOnly = true)
    public StudentResponseWithProfileDTO getStudentsWithProfile(String domain , String email , Set<String> roles) {

            Student studentEntity = studentRepository.findByDomainAndEmail(domain, email);
            if (studentEntity == null) {
                throw new RuntimeException("Student not found");
            }

            StudentResponseWithProfileDTO student = toStudentProfileResponse(studentEntity, domain);

            // DOMAIN_ADMIN -> everything
            if (roles.contains("ROLE_DOMAIN_ADMIN")) {
                return student;
            }

            if (roles.contains("ROLE_FEES_ADMIN")) {
                student.setCertifications(null);
                student.setStudentAttendanceRecordDayWises(null);
                student.setStudentTotalAttendanceSubjectWise(null);
                return student;
            }

            if (roles.contains("ROLE_FACULTY") || roles.contains("ROLE_SUB_ADMIN")) {
                student.setFeesPayments(null);
                student.setFeesAccount(null);
                return student;
            }

            student.setCertifications(null);
            student.setFeesPayments(null);
            student.setFeesAccount(null);
            student.setStudentAttendanceRecordDayWises(null);
            student.setStudentTotalAttendanceSubjectWise(null);
            return student;
    }

    private StudentResponseWithProfileDTO toStudentProfileResponse(Student entity, String domain) {
            StudentResponseWithProfileDTO response = new StudentResponseWithProfileDTO();
            response.setName(entity.getName());
            response.setEmail(entity.getEmail());
            response.setMobileNumber(entity.getMobileNumber());
            response.setCreatedDateTime(entity.getCreatedDateTime());
            response.setLastUpdateDateTime(entity.getLastUpdateDateTime());
            response.setLastLoginDateTime(entity.getLastLoginDateTime());
            response.setProfilePic(entity.getProfilePic());
            response.setRole(entity.getRole() == null ? null : entity.getRole().name());
            response.setRollNumber(entity.getRollNumber());
            response.setCourse(entity.getCourse());
            response.setBatch(entity.getBatch());
            response.setAcademicSession(entity.getAcademicSession());
            response.setStudyBatch(entity.getStudyBatch());
            response.setStudySubjects(entity.getStudySubjects());
            response.setFatherName(entity.getFatherName());
            response.setFatherMobNo(entity.getFatherMobNo());

            response.setCertifications((entity.getCertifications() == null
                    ? List.<Certifications>of()
                    : entity.getCertifications()).stream()
                    .map(certification -> toCertificationResponse(certification, domain))
                    .toList());
            response.setFeesPayments((entity.getFeesPayments() == null
                    ? List.<StudentFeesPayment>of()
                    : entity.getFeesPayments()).stream()
                    .map(this::toFeesPaymentResponse)
                    .toList());
            response.setStudentAttendanceRecordDayWises((entity.getStudentAttendanceRecordDayWises() == null
                    ? List.<StudentAttendanceRecordDayWise>of()
                    : entity.getStudentAttendanceRecordDayWises()).stream()
                    .map(this::toAttendanceDayResponse)
                    .toList());
            response.setStudentTotalAttendanceSubjectWise(
                    (entity.getStudentTotalAttendanceSubjectWise() == null
                            ? List.<StudentTotalAttendanceSubjectWise>of()
                            : entity.getStudentTotalAttendanceSubjectWise())
                            .stream()
                            .map(this::toAttendanceSubjectResponse)
                            .toList()
            );

            if (entity.getFeesAccount() != null) {
                StudentFeesAccountDTO account = new StudentFeesAccountDTO();
                double totalFees = entity.getFeesAccount().getTotalFees() == null
                        ? 0.0
                        : entity.getFeesAccount().getTotalFees();
                double totalPaid = entity.getFeesAccount().getTotalPaid() == null
                        ? 0.0
                        : entity.getFeesAccount().getTotalPaid();
                account.setTotalFees(totalFees);
                account.setTotalPaid(totalPaid);
                account.setTotalDues(totalFees - totalPaid);
                response.setFeesAccount(account);
            }
            return response;
    }

    private CertificationResponseDTO toCertificationResponse(Certifications entity, String domain) {
            CertificationResponseDTO response = new CertificationResponseDTO();
            response.setId(entity.getId());
            response.setTitle(entity.getTitle());
            response.setDescription(entity.getDescription());
            response.setDownloadUrl("/" + domain + "/student/certifications/" + entity.getId() + "/file");
            return response;
    }

    private StudentFeesPaymentResponseDTO toFeesPaymentResponse(StudentFeesPayment entity) {
            StudentFeesPaymentResponseDTO response = new StudentFeesPaymentResponseDTO();
            response.setAcademicSession(entity.getAcademicSession());
            response.setAmount(entity.getAmount());
            response.setUpiTransactionId(entity.getUpiTransactionId());
            response.setPaymentType(entity.getPaymentType() == null ? null : entity.getPaymentType().name());
            response.setStatus(entity.getStatus() == null ? null : entity.getStatus().name());
            response.setRejectionReason(entity.getRejectionReason());
            response.setVerifyByFeesAdmin(entity.getVerifyByFeesAdmin() == null
                    ? null
                    : entity.getVerifyByFeesAdmin().getName());
            response.setVerifiedAt(entity.getVerifiedAt());
            return response;
    }

    private StudentAttendanceDayResponseDTO toAttendanceDayResponse(StudentAttendanceRecordDayWise entity) {
            StudentAttendanceDayResponseDTO response = new StudentAttendanceDayResponseDTO();
            response.setId(entity.getId());
            response.setDomain(entity.getDomain());
            response.setSubject(entity.getSubject());
            response.setAttendanceDate(entity.getAttendanceDate());
            response.setAttendanceTime(entity.getAttendanceTime());
            response.setPeriodNumber(entity.getPeriodNumber());
            response.setStatus(entity.getStatus());
            response.setMarkedByFacultyEmail(entity.getMarkedBy() == null
                    ? null
                    : entity.getMarkedBy().getEmail());
            return response;
    }

    private StudentAttendanceSubjectResponseDTO toAttendanceSubjectResponse(
                StudentTotalAttendanceSubjectWise entity
    ) {
            StudentAttendanceSubjectResponseDTO response = new StudentAttendanceSubjectResponseDTO();
            response.setId(entity.getId());
            response.setDomain(entity.getDomain());
            response.setSubject(entity.getSubject());
            response.setTotalClasses(entity.getTotalClasses());
            response.setPresentCount(entity.getPresentCount());
            response.setAbsentCount(entity.getAbsentCount());
            return response;
    }

    // =========================================================
    // UPDATE PASSWORD
    // =========================================================

    public boolean updatePasswordByEmail(
            String domain,
            String email,
            String newPass) {

        Student student =
                studentRepository
                        .findByEmailAndDomain(
                                email,
                                domain
                        )
                        .orElse(null);

        if (student == null) {
            return false;
        }

        student.setPassword(
                passwordEncoder.encode(newPass)
        );

        studentRepository.save(student);

        return true;
    }


    // =========================================================
    // UPDATE STUDENT
    // =========================================================

    public boolean updateStudentByEmail(
            String domain,
            Student newData) {

        if (newData == null ||
                newData.getEmail() == null) {

            return false;
        }

        Student old =
                studentRepository
                        .findByEmailAndDomain(
                                newData.getEmail(),
                                domain
                        )
                        .orElse(null);

        if (old == null) {
            return false;
        }

        if (newData.getName() != null) {
            old.setName(
                    newData.getName()
            );
        }

        if (newData.getRollNumber() != null) {
            old.setRollNumber(
                    newData.getRollNumber()
            );
        }

        if (newData.getAcademicSession() != null) {
            old.setAcademicSession(
                    newData.getAcademicSession()
            );
        }

        if (newData.getCourse() != null) {
            old.setCourse(
                    newData.getCourse()
            );
        }

        if (newData.getBatch() != null) {
            old.setBatch(
                    newData.getBatch()
            );
        }

        if (newData.getStudyBatch() != null) {

            old.setStudyBatch(
                    newData.getStudyBatch()
                            .trim()
                            .toUpperCase()
            );
        }

        if (newData.getStudySubjects() != null) {
            old.setStudySubjects(
                    newData.getStudySubjects()
            );
        }

        if (newData.getMobileNumber() != null) {

            old.setMobileNumber(
                    newData.getMobileNumber()
            );
        }

        if (newData.getFatherName() != null) {

            old.setFatherName(
                    newData.getFatherName()
            );
        }

        if (newData.getFatherMobNo() != null) {

            old.setFatherMobNo(
                    newData.getFatherMobNo()
            );
        }

        studentRepository.save(old);

        return true;
    }




    // =========================================================
    // DELETE STUDENT
    // =========================================================

    public String deleteStudentByEmail(
            String domain,
            String email) {

        Student student =
                studentRepository
                        .findByEmailAndDomain(
                                domain,
                                email
                        )
                        .orElse(null);

        if (student == null) {
            return "Invalid student";
        }

        String rollNumber =
                student.getRollNumber();

        studentRepository.delete(student);

        return "Deleted student with Roll Number "
                + rollNumber;
    }


    // =========================================================
    // GET STUDENTS BY COURSE
    // =========================================================

    public List<StudentResponseDTO>
    getStudentsByCourse(
            String domain,
            String course) {

        return studentRepository
                .findByCourseAndDomain(
                        course,
                        domain
                )
                .stream()
                .map(student ->
                        modelMapper.map(
                                student,
                                StudentResponseDTO.class
                        )
                )
                .toList();
    }


    // =========================================================
    // GET STUDENTS BY STUDY BATCH
    // =========================================================

    public List<StudentResponseDTO>
    getStudentsByTeachingBatch(
            String domain,
            String studyBatch) {

        String normalizedBatch =
                studyBatch
                        .trim()
                        .toUpperCase();

        return studentRepository
                .findByStudyBatchAndDomain(
                        normalizedBatch,
                        domain
                )
                .stream()
                .map(student ->
                        modelMapper.map(
                                student,
                                StudentResponseDTO.class
                        )
                )
                .toList();
    }


}