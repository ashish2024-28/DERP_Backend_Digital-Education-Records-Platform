package com.demoproject.Service;

import com.demoproject.DTO.FacultyDTO.FacultyResponseDTO;
import com.demoproject.DTO.LoginRequestDTO;
import com.demoproject.DTO.StudentDTO.StudentResponseDTO;
import com.demoproject.DTO.SubAdminDTO.SubAdminResponseDTO;
import com.demoproject.DTO.SubAdminDTO.SubAdminSignupDTO;
import com.demoproject.Entity.Role;
import com.demoproject.Entity.SubAdmin;
import com.demoproject.Entity.University;
import com.demoproject.Repository.SubAdminRepository;
import com.demoproject.Repository.UniversityRepo;

import jakarta.validation.constraints.NotNull;

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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubAdminService {

    private final StudentService studentService;

    private final FacultyService facultyService;

    private final SubAdminRepository SArepo;

    private final UniversityRepo universityRepo;

    private final BaseUserService baseUserService;

    private final ModelMapper modelMapper;

    @Qualifier("bcryptEncoder")
    private final PasswordEncoder passwordEncoder;


    // =========================================================
    // LOGIN
    // =========================================================

    public SubAdmin LoginSubAdmin(
            LoginRequestDTO request) {

        SubAdmin subAdmin =
                SArepo.findByEmailAndDomain(
                        request.getEmail(),
                        request.getDomain()
                ).orElse(null);

        if (subAdmin == null) {
            return null;
        }

        boolean passwordMatch =
                passwordEncoder.matches(
                        request.getPassword(),
                        subAdmin.getPassword()
                );

        if (!passwordMatch) {
            return null;
        }

        subAdmin.setLastLoginDateTime(
                Instant.now()
        );

        return SArepo.save(subAdmin);
    }


    // =========================================================
    // CURRENT SUBADMIN
    // =========================================================

    public SubAdminResponseDTO
    getSubAdminByEmailAndDomain(
            String email,
            String domain) {

        SubAdmin subAdmin =
                SArepo.findByEmailAndDomain(
                        email,
                        domain
                ).orElseThrow(() ->
                        new RuntimeException(
                                "SubAdmin not found"
                        ));

        Instant previousLogin =
                subAdmin.getLastLoginDateTime();

        subAdmin.setLastLoginDateTime(
                Instant.now()
        );

        SArepo.save(subAdmin);

        return toResponse(
                subAdmin,
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

        SubAdmin subAdmin =
                SArepo.findByDomainAndEmail(
                        domain,
                        email
                );

        if (subAdmin == null) {
            throw new RuntimeException(
                    "SubAdmin not found"
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

        subAdmin.setProfilePic(
                uploadDir + fileName
        );

        SArepo.save(subAdmin);

        return uploadDir + fileName;
    }


    // =========================================================
    // CREATE
    // =========================================================

    public String addSubAdmin(
            @NotNull String domain,
            @NotNull SubAdminSignupDTO dto) {

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
                                        "University not found"
                                ));

        SubAdmin subAdmin =
                new SubAdmin();

        subAdmin.setDomain(domain);

        subAdmin.setUniversity(university);

        subAdmin.setSubAdminId(
                dto.getSubAdminId()
        );

        subAdmin.setName(
                dto.getName()
        );

        subAdmin.setEmail(
                dto.getEmail()
        );

        subAdmin.setMobileNumber(
                dto.getMobileNumber()
        );

        subAdmin.setCourse(
                dto.getCourse()
        );

        subAdmin.setPassword(
                passwordEncoder.encode(
                        dto.getPassword()
                )
        );

        subAdmin.setRole(
                Role.SUB_ADMIN
        );

        subAdmin.setTeachingAssignmentsMap(
                dto.getTeachingAssignments()
        );


        if (SArepo.existsBySubAdminIdAndDomain(
                subAdmin.getSubAdminId(),
                domain)) {

            throw new RuntimeException(
                    "SubAdmin ID already exists."
            );
        }

        SubAdmin saved =
                SArepo.save(subAdmin);

        return saved.getName()
                + ",\nYour Account is Created Successfully."
                + "\nSub Admin Id : "
                + saved.getSubAdminId();
    }


    // =========================================================
    // COUNT
    // =========================================================

    public long getSubAdminCount(
            String domain) {

        return SArepo
                .countByUniversity_Domain(domain);
    }


    // =========================================================
    // GET ALL
    // =========================================================

    public List<SubAdminResponseDTO>
    getAllSubAdmin(
            String domain) {

        return SArepo
                .findByDomain(domain)
                .stream()
                .map(subAdmin ->
                        toResponse(
                                subAdmin,
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

        SubAdmin subAdmin =
                SArepo.findByEmailAndDomain(
                        email,
                        domain
                ).orElse(null);

        if (subAdmin == null) {
            return false;
        }

        subAdmin.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );

        SArepo.save(subAdmin);

        return true;
    }


    // =========================================================
// UPDATE SUBADMIN
// =========================================================

    public SubAdminResponseDTO updateSubAdminByEmail(
            String domain,
            SubAdmin newData) {

        SubAdmin old = SArepo.findByDomainAndEmail(
                domain,
                newData.getEmail()
        );

        if (old == null) {
            throw new RuntimeException(
                    "SubAdmin not found with email: "
                            + newData.getEmail()
            );
        }

        // ---------------------------------------------------------
        // Basic fields
        // ---------------------------------------------------------

        if (newData.getName() != null &&
                !newData.getName().isBlank()) {

            old.setName(newData.getName());
        }

        if (newData.getSubAdminId() != null &&
                !newData.getSubAdminId().isBlank()) {

            old.setSubAdminId(newData.getSubAdminId());
        }

        if (newData.getCourse() != null &&
                !newData.getCourse().isBlank()) {

            old.setCourse(newData.getCourse());
        }

        if (newData.getMobileNumber() != null &&
                !newData.getMobileNumber().isBlank()) {

            old.setMobileNumber(newData.getMobileNumber());
        }

        // ---------------------------------------------------------
        // Teaching assignments JSON
        //
        // Example:
        //
        // {
        //     "2A": ["JAVA", "DSA"],
        //     "2B": ["OS"],
        //     "3A": ["DBMS"]
        // }
        // ---------------------------------------------------------

        if (newData.getTeachingAssignmentsJson() != null &&
                !newData.getTeachingAssignmentsJson().isBlank()) {

            old.setTeachingAssignmentsJson(
                    newData.getTeachingAssignmentsJson()
            );
        }

        // ---------------------------------------------------------
        // Save
        // ---------------------------------------------------------

        SubAdmin savedSubAdmin = SArepo.save(old);

        // ---------------------------------------------------------
        // Convert Entity → DTO
        // ---------------------------------------------------------

        return modelMapper.map(
                savedSubAdmin,
                SubAdminResponseDTO.class
        );
    }

    // =========================================================
    // ADD ASSIGNMENT
    // =========================================================

    public boolean addTeachingAssignment(
            String domain,
            String email,
            String batch,
            String subject) {

        SubAdmin subAdmin =
                SArepo.findByEmailAndDomain(
                        email,
                        domain
                ).orElseThrow(() ->
                        new RuntimeException(
                                "SubAdmin not found"
                        ));

        subAdmin.addTeachingAssignment(
                batch,
                subject
        );

        SArepo.save(subAdmin);

        return true;
    }


    // =========================================================
    // REMOVE ASSIGNMENT
    // =========================================================

    public boolean removeTeachingAssignment(
            String domain,
            String email,
            String batch,
            String subject) {

        SubAdmin subAdmin =
                SArepo.findByEmailAndDomain(
                        email,
                        domain
                ).orElseThrow(() ->
                        new RuntimeException(
                                "SubAdmin not found"
                        ));

        subAdmin.removeTeachingAssignment(
                batch,
                subject
        );

        SArepo.save(subAdmin);

        return true;
    }


    // =========================================================
    // GET ASSIGNMENTS
    // =========================================================

    public Map<String, List<String>>
    getTeachingAssignments(
            String domain,
            String email) {

        SubAdmin subAdmin =
                SArepo.findByEmailAndDomain(
                        email,
                        domain
                ).orElseThrow(() ->
                        new RuntimeException(
                                "SubAdmin not found"
                        ));

        return subAdmin
                .getTeachingAssignmentsMap();
    }


    // =========================================================
    // GET FACULTY BY COURSE
    // =========================================================

    public List<FacultyResponseDTO>
    getAllFacultyBySubAdminCourse(
            String domain,
            String email) {

        SubAdmin subAdmin =
                SArepo.findByEmailAndDomain(
                        email,
                        domain
                ).orElseThrow(() ->
                        new RuntimeException(
                                "SubAdmin not found"
                        ));

        return facultyService
                .getAllFacultyBySubAdminCourse(
                        domain,
                        subAdmin.getCourse()
                );
    }


    // =========================================================
    // GET STUDENTS BY COURSE
    // =========================================================

    public List<StudentResponseDTO>
    getStudentBySubAdminCourse(
            String domain,
            String email) {

        SubAdmin subAdmin =
                SArepo.findByEmailAndDomain(
                        email,
                        domain
                ).orElseThrow(() ->
                        new RuntimeException(
                                "SubAdmin not found"
                        ));

        return studentService
                .getStudentsByCourse(
                        domain,
                        subAdmin.getCourse()
                );
    }


    // =========================================================
    // GET STUDENTS BY BATCH
    // =========================================================

    public List<StudentResponseDTO>
    getStudentsByBatch(
            String domain,
            String email,
            String studyBatch) {

        SubAdmin subAdmin =
                SArepo.findByEmailAndDomain(
                        email,
                        domain
                ).orElseThrow(() ->
                        new RuntimeException(
                                "SubAdmin not found"
                        ));

        /*
         * Optional security:
         * If SubAdmin has assignment data,
         * verify the batch.
         */
        if (!subAdmin
                .getTeachingAssignmentsMap()
                .containsKey(
                        studyBatch
                                .trim()
                                .toUpperCase()
                )) {

            throw new RuntimeException(
                    "SubAdmin is not assigned to batch "
                            + studyBatch
            );
        }

        return studentService
                .getStudentsByTeachingBatch(
                        domain,
                        studyBatch
                );
    }


    // =========================================================
    // DELETE
    // =========================================================

    public String deleteSubAdminByEmail(
            String domain,
            String email) {

        SubAdmin subAdmin =
                SArepo.findByDomainAndEmail(
                        domain,
                        email
                );

        if (subAdmin == null) {
            return "Not found";
        }

        SArepo.delete(subAdmin);

        return "Deleted SubAdmin with email id : "
                + email;
    }


    // =========================================================
    // RESPONSE
    // =========================================================

    private SubAdminResponseDTO toResponse(
            SubAdmin subAdmin,
            Instant lastLogin) {

        SubAdminResponseDTO response =
                new SubAdminResponseDTO();

        response.setName(
                subAdmin.getName()
        );

        response.setEmail(
                subAdmin.getEmail()
        );

        response.setMobileNumber(
                subAdmin.getMobileNumber()
        );

        response.setProfilePic(
                subAdmin.getProfilePic()
        );

        response.setCreatedDateTime(
                subAdmin.getCreatedDateTime()
        );

        response.setLastLoginDateTime(
                lastLogin != null
                        ? lastLogin
                        : subAdmin.getLastLoginDateTime()
        );

        response.setSubAdminId(
                subAdmin.getSubAdminId()
        );

        response.setCourse(
                subAdmin.getCourse()
        );

        response.setTeachingAssignments(
                subAdmin.getTeachingAssignmentsMap()
        );

        if (subAdmin.getUniversity() != null) {

            response.setUniversityName(
                    subAdmin.getUniversity().getUniversityName()
            );
        }

        return response;
    }
}