package com.demoproject.Service;

import com.demoproject.DTO.FacultyDTO.FacultyResponseDTO;
import com.demoproject.DTO.FacultyDTO.FacultySignupDTO;
import com.demoproject.DTO.StudentDTO.StudentResponseDTO;
import com.demoproject.Entity.Faculty;
import com.demoproject.Entity.Role;
import com.demoproject.Entity.University;
import com.demoproject.Repository.FacultyRepository;
import com.demoproject.Repository.UniversityRepo;

import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyRepository frepo;

    private final StudentService studentService;

    private final UniversityRepo universityRepo;

    private final BaseUserService baseUserService;

    @Qualifier("bcryptEncoder")
    private final PasswordEncoder passwordEncoder;


    // =========================================================
    // CURRENT FACULTY
    // =========================================================

    public FacultyResponseDTO
    getFacultyByEmailAndDomain(
            String email,
            String domain) {

        Faculty faculty =
                frepo.findByEmailAndDomain(
                        email,
                        domain
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Faculty not found"
                        ));

        Instant previousLogin =
                faculty.getLastLoginDateTime();

        faculty.setLastLoginDateTime(
                Instant.now()
        );

        frepo.save(faculty);

        return toResponse(
                faculty,
                previousLogin
        );
    }


    // =========================================================
    // PROFILE IMAGE
    // =========================================================

    public String updateProfilePic(
            String domain,
            String email,
            MultipartFile file)
            throws IOException {

        Faculty faculty =
                frepo.findByDomainAndEmail(
                        domain,
                        email
                );

        if (faculty == null) {
            throw new RuntimeException(
                    "Faculty not found"
            );
        }

        String uploadDir =
                "uploads/profile/";

        Files.createDirectories(
                Paths.get(uploadDir)
        );

        String original =
                file.getOriginalFilename();

        String safeName =
                original == null
                        ? "profile"
                        : Paths.get(original)
                        .getFileName()
                        .toString();

        String fileName =
                System.currentTimeMillis()
                        + "_"
                        + safeName;

        Path path =
                Paths.get(
                        uploadDir,
                        fileName
                );

        Files.write(
                path,
                file.getBytes()
        );

        faculty.setProfilePic(
                uploadDir + fileName
        );

        frepo.save(faculty);

        return uploadDir + fileName;
    }


    // =========================================================
    // CREATE FACULTY
    // =========================================================

    public String addFaculty(
            @NotNull String domain,
            @NotNull FacultySignupDTO dto) {

        if (baseUserService.existsUserByEmail(
                dto.getEmail())) {

            throw new RuntimeException(
                    "User already exists with this email."
            );
        }

        University university =
                universityRepo
                        .findByDomain(domain)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid domain"
                                ));

        Faculty faculty =
                new Faculty();

        faculty.setDomain(domain);

        faculty.setUniversity(university);

        faculty.setFacultyId(
                dto.getFacultyId()
        );

        faculty.setName(
                dto.getName()
        );

        faculty.setEmail(
                dto.getEmail()
        );

        faculty.setMobileNumber(
                dto.getMobileNumber()
        );

        faculty.setCourse(
                dto.getCourse()
        );

        faculty.setPassword(
                passwordEncoder.encode(
                        dto.getPassword()
                )
        );

        faculty.setRole(
                Role.FACULTY
        );


        faculty.setTeachingAssignmentsMap(
                dto.getTeachingAssignments()
        );


        if (frepo.existsByFacultyIdAndDomain(
                faculty.getFacultyId(),
                domain)) {

            throw new RuntimeException(
                    "Faculty ID already exists."
            );
        }

        if (frepo.existsByDomainAndEmail(
                domain,
                faculty.getEmail())) {

            throw new RuntimeException(
                    "Faculty email already exists."
            );
        }

        Faculty saved =
                frepo.save(faculty);

        return saved.getName()
                + ",\nYour Account is Created Successfully."
                + "\nFaculty Id : "
                + saved.getFacultyId();
    }


    // =========================================================
    // COUNT
    // =========================================================

    public long getFacultyCount(
            String domain) {

        return frepo
                .countByUniversity_Domain(domain);
    }


    // =========================================================
    // GET ALL FACULTY
    // =========================================================

    public List<FacultyResponseDTO>
    getAllFaculty(
            String domain) {

        return frepo
                .findByDomain(domain)
                .stream()
                .map(faculty ->
                        toResponse(
                                faculty,
                                null
                        ))
                .toList();
    }


    // =========================================================
    // UPDATE PASSWORD
    // =========================================================

    public boolean updatePasswordByEmail(
            String domain,
            String email,
            String newPassword) {

        Faculty faculty =
                frepo.findByEmailAndDomain(
                        email,
                        domain
                ).orElse(null);

        if (faculty == null) {
            return false;
        }

        faculty.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );

        frepo.save(faculty);

        return true;
    }


    // =========================================================
    // UPDATE FACULTY
    // =========================================================

    public boolean updateFacultyByEmail(
            String domain,
            Faculty newData) {

        Faculty old =
                frepo.findByEmailAndDomain(
                        newData.getEmail(),
                        domain
                ).orElse(null);

        if (old == null) {
            return false;
        }

        if (newData.getName() != null)
            old.setName(
                    newData.getName()
            );

        if (newData.getFacultyId() != null)
            old.setFacultyId(
                    newData.getFacultyId()
            );

        if (newData.getMobileNumber() != null)
            old.setMobileNumber(
                    newData.getMobileNumber()
            );

        if (newData.getCourse() != null)
            old.setCourse(
                    newData.getCourse()
            );

        /*
         * NEW:
         * Update complete batch -> subjects map.
         */
        if (newData.getTeachingAssignmentsJson()
                != null) {

            old.setTeachingAssignmentsJson(
                    newData.getTeachingAssignmentsJson()
            );
        }

        frepo.save(old);

        return true;
    }


    // =========================================================
    // ADD ONE TEACHING ASSIGNMENT
    // =========================================================

    public boolean addTeachingAssignment(
            String domain,
            String email,
            String batch,
            String subject) {

        Faculty faculty =
                frepo.findByEmailAndDomain(
                        email,
                        domain
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Faculty not found"
                        ));

        faculty.addTeachingAssignment(
                batch,
                subject
        );

        frepo.save(faculty);

        return true;
    }


    // =========================================================
    // REMOVE ONE TEACHING ASSIGNMENT
    // =========================================================

    public boolean removeTeachingAssignment(
            String domain,
            String email,
            String batch,
            String subject) {

        Faculty faculty =
                frepo.findByEmailAndDomain(
                        email,
                        domain
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Faculty not found"
                        ));

        faculty.removeTeachingAssignment(
                batch,
                subject
        );

        frepo.save(faculty);

        return true;
    }


    // =========================================================
    // GET FACULTY ASSIGNMENTS
    // =========================================================

    public Map<String, List<String>>
    getTeachingAssignments(
            String domain,
            String email) {

        Faculty faculty =
                frepo.findByEmailAndDomain(
                        email,
                        domain
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Faculty not found"
                        ));

        return faculty.getTeachingAssignmentsMap();
    }


    // =========================================================
    // GET STUDENTS BY COURSE
    // =========================================================

    public List<StudentResponseDTO>
    getStudentsByFacultyCourse(
            String domain,
            String email) {

        Faculty faculty =
                frepo.findByEmailAndDomain(
                        email,
                        domain
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Faculty not found"
                        ));

        return studentService
                .getStudentsByCourse(
                        domain,
                        faculty.getCourse()
                );
    }


    // =========================================================
    // GET STUDENTS FOR ONE BATCH
    // =========================================================

    public List<StudentResponseDTO>
    getStudentsByFacultyTeachingBatch(
            String domain,
            String email,
            String teachingBatch) {

        Faculty faculty =
                frepo.findByEmailAndDomain(
                        email,
                        domain
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Faculty not found"
                        ));

        /*
         * Security:
         *
         * Faculty cannot request a batch
         * that they don't teach.
         */
        boolean teachesBatch =
                faculty.getTeachingAssignmentsMap()
                        .containsKey(
                                teachingBatch
                                        .trim()
                                        .toUpperCase()
                        );

        if (!teachesBatch) {

            throw new RuntimeException(
                    "Faculty is not assigned to batch "
                            + teachingBatch
            );
        }

        return studentService
                .getStudentsByTeachingBatch(
                        domain,
                        teachingBatch
                );
    }


    // =========================================================
    // GET STUDENTS FOR BATCH + SUBJECT
    // =========================================================

    public List<StudentResponseDTO>
    getStudentsForAttendance(
            String domain,
            String email,
            String batch,
            String subject) {

        Faculty faculty =
                frepo.findByEmailAndDomain(
                        email,
                        domain
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Faculty not found"
                        ));

        /*
         * CRITICAL SECURITY CHECK
         *
         * Faculty must actually teach
         * this batch + subject.
         */
        if (!faculty.teaches(
                batch,
                subject)) {

            throw new RuntimeException(
                    "Faculty is not assigned to "
                            + batch
                            + " - "
                            + subject
            );
        }

        /*
         * Students are found by their
         * studyBatch.
         */
        List<StudentResponseDTO> students =
                studentService
                        .getStudentsByTeachingBatch(
                                domain,
                                batch
                        );

        /*
         * Optional additional validation:
         *
         * only students who actually study
         * the subject.
         */
        return students.stream()
                .filter(student ->
                        student.getStudySubjects() != null
                                &&
                                student.getStudySubjects()
                                        .stream()
                                        .anyMatch(
                                                s -> s.equalsIgnoreCase(
                                                        subject
                                                )
                                        ))
                .toList();
    }


    // =========================================================
    // FACULTY BY SUBADMIN COURSE
    // =========================================================

    public List<FacultyResponseDTO>
    getAllFacultyBySubAdminCourse(
            String domain,
            String course) {

        return frepo
                .findByCourseAndDomain(
                        course,
                        domain
                )
                .stream()
                .map(faculty ->
                        toResponse(
                                faculty,
                                null
                        ))
                .toList();
    }


    // =========================================================
    // DELETE
    // =========================================================

    public String deleteFacultyByEmail(
            String domain,
            String email) {

        Faculty faculty =
                frepo.findByEmailAndDomain(
                        email,
                        domain
                ).orElse(null);

        if (faculty == null) {
            return "Invalid faculty email";
        }

        frepo.delete(faculty);

        return "Deleted faculty with email id "
                + email;
    }


    // =========================================================
    // RESPONSE MAPPER
    // =========================================================

    private FacultyResponseDTO toResponse(
            Faculty faculty,
            Instant lastLogin) {

        FacultyResponseDTO response =
                new FacultyResponseDTO();

        response.setName(
                faculty.getName()
        );

        response.setEmail(
                faculty.getEmail()
        );

        response.setMobileNumber(
                faculty.getMobileNumber()
        );

        response.setProfilePic(
                faculty.getProfilePic()
        );

        response.setCreatedDateTime(
                faculty.getCreatedDateTime()
        );

        response.setLastLoginDateTime(
                lastLogin != null
                        ? lastLogin
                        : faculty.getLastLoginDateTime()
        );

        response.setFacultyId(
                faculty.getFacultyId()
        );

        response.setCourse(
                faculty.getCourse()
        );

        response.setTeachingAssignments(
                faculty.getTeachingAssignmentsMap()
        );

        if (faculty.getUniversity() != null) {

            response.setUniversityName(
                    faculty.getUniversity().getUniversityName()
            );
        }

        return response;
    }
}