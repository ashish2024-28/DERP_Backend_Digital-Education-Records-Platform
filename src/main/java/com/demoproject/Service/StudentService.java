package com.demoproject.Service;

import com.demoproject.DTO.LoginRequestDTO;
import com.demoproject.DTO.StudentDTO.StudentResponseDTO;
import com.demoproject.DTO.StudentDTO.StudentSignupDTO;
import com.demoproject.Entity.Role;
import com.demoproject.Entity.Student;
import com.demoproject.Entity.University;
import com.demoproject.Repository.StudentRepository;
import com.demoproject.Repository.UniversityRepo;

import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final BaseUserService baseUserService;
    private final UniversityRepo universityRepo;
    private final ModelMapper modelMapper;

    @Qualifier("bcryptEncoder")
    private final PasswordEncoder passwordEncoder;


    // =========================================================
    // GET LOGGED-IN STUDENT
    // =========================================================

    public StudentResponseDTO getStudentByEmailAndDomain(
            String email,
            String domain) {

        Student student =
                studentRepository
                        .findByEmailAndDomain(
                                email,
                                domain
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student not found"
                                )
                        );

        Instant lastLogin =
                student.getLastLoginDateTime();

        student.setLastLoginDateTime(
                Instant.now()
        );

        studentRepository.save(student);

        student.setLastLoginDateTime(
                lastLogin
        );

        return modelMapper.map(
                student,
                StudentResponseDTO.class
        );
    }


    // =========================================================
    // LOGIN
    // =========================================================

    public StudentResponseDTO LoginStudent(
            LoginRequestDTO loginRequestDTO) {

        Student student =
                studentRepository
                        .findByEmailAndDomain(
                                loginRequestDTO.getEmail(),
                                loginRequestDTO.getDomain()
                        )
                        .orElse(null);

        if (student == null) {
            return null;
        }

        boolean passwordMatch =
                passwordEncoder.matches(
                        loginRequestDTO.getPassword(),
                        student.getPassword()
                );

        if (!passwordMatch) {
            return null;
        }

        student.setLastLoginDateTime(
                Instant.now()
        );

        studentRepository.save(student);

        return modelMapper.map(
                student,
                StudentResponseDTO.class
        );
    }


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

    public String addStudent(
            String domain,
            StudentSignupDTO dto) {

        if (dto == null) {
            throw new RuntimeException(
                    "Student data is required"
            );
        }

        if (baseUserService.existsUserByEmail(
                dto.getEmail())) {

            throw new RuntimeException(
                    "User already exists with this email."
            );
        }

        University university =
                universityRepo.findByDomain(domain)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid domain"
                                )
                        );

        Student student =
                modelMapper.map(
                        dto,
                        Student.class
                );

        student.setDomain(domain);
        student.setUniversity(university);
        student.setRole(Role.STUDENT);

        /*
         * Normalize study batch.
         *
         * 2a → 2A
         */
        if (student.getStudyBatch() != null) {

            student.setStudyBatch(
                    student.getStudyBatch()
                            .trim()
                            .toUpperCase()
            );
        }

        /*
         * If frontend doesn't provide subjects,
         * store [].
         */
        if (student.getStudyBatch() != null) {
            student.setStudyBatch(
                    student.getStudyBatch()
                            .trim()
                            .toUpperCase()
            );
        }

        /*
         * Password encryption.
         */
        student.setPassword(
                passwordEncoder.encode(
                        student.getPassword()
                )
        );

        if (studentRepository
                .existsByRollNumberAndDomain(
                        student.getRollNumber(),
                        domain)) {

            throw new RuntimeException(
                    "Student Roll Number already exists."
            );
        }

        if (studentRepository
                .existsByDomainAndEmail(
                        domain,
                        student.getEmail())) {

            throw new RuntimeException(
                    "Student email already exists."
            );
        }

        Student saved =
                studentRepository.save(student);

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

    public List<StudentResponseDTO>
    getAllStudent(String domain) {

        return studentRepository
                .findAllByDomain(domain)
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

        if (newData.getBranch() != null) {
            old.setBranch(
                    newData.getBranch()
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
            old.setStudySubjectsList(
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
    // UPDATE STUDENT SUBJECTS
    // =========================================================

    public boolean updateStudySubjects(
            String domain,
            String email,
            List<String> subjects) {

        Student student =
                studentRepository
                        .findByEmailAndDomain(
                                email,
                                domain
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student not found"
                                )
                        );

        student.setStudySubjectsList(
                subjects
        );

        studentRepository.save(student);

        return true;
    }


    // =========================================================
    // ADD STUDENT SUBJECT
    // =========================================================

    public boolean addStudySubject(
            String domain,
            String email,
            String subjectCode) {

        Student student =
                studentRepository
                        .findByEmailAndDomain(
                                email,
                                domain
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student not found"
                                )
                        );

        student.addStudySubject(
                subjectCode
        );

        studentRepository.save(student);

        return true;
    }


    // =========================================================
    // REMOVE STUDENT SUBJECT
    // =========================================================

    public boolean removeStudySubject(
            String domain,
            String email,
            String subjectCode) {

        Student student =
                studentRepository
                        .findByEmailAndDomain(
                                email,
                                domain
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student not found"
                                )
                        );

        student.removeStudySubject(
                subjectCode
        );

        studentRepository.save(student);

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

    // =========================================================
    // PRIVATE RESPONSE MAPPER
    // =========================================================

    private StudentResponseDTO toResponse(
            Student student,
            Instant lastLogin) {

        StudentResponseDTO response =
                new StudentResponseDTO();

        response.setName(student.getName());

        response.setEmail(student.getEmail());

        response.setMobileNumber(
                student.getMobileNumber()
        );

        response.setCreatedDateTime(
                student.getCreatedDateTime()
        );

        response.setLastLoginDateTime(
                lastLogin != null
                        ? lastLogin
                        : student.getLastLoginDateTime()
        );

        response.setProfilePic(
                student.getProfilePic()
        );

        response.setRollNumber(
                student.getRollNumber()
        );

        response.setCourse(
                student.getCourse()
        );

        response.setBranch(
                student.getBranch()
        );

        response.setBatch(
                student.getBatch()
        );

        response.setStudyBatch(
                student.getStudyBatch()
        );

        response.setStudySubjects(
                student.getStudySubjectsList()
        );

        response.setFatherName(
                student.getFatherName()
        );

        response.setFatherMobNo(
                student.getFatherMobNo()
        );

        if (student.getUniversity() != null) {
            response.setUniversityName(
                    student.getUniversity().getUniversityName()
            );
        }

        return response;
    }

}