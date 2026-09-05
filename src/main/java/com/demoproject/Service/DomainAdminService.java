package com.demoproject.Service;

import com.demoproject.DTO.BulkUpload.BulkUploadResultDTO;
import com.demoproject.DTO.DomainAdminDTO.DomainAdminResponseDTO;
import com.demoproject.DTO.FacultyDTO.FacultySignupDTO;
import com.demoproject.DTO.FeesAdminDto.FeesAdminSignupDTO;
import com.demoproject.DTO.StudentDTO.StudentSignupDTO;
import com.demoproject.DTO.SubAdminDTO.SubAdminSignupDTO;
import com.demoproject.DTO.University.UniversityAndDomainAdminResponseDTO;
import com.demoproject.DTO.University.UniversityResponseDTO;
import com.demoproject.Entity.*;

import com.demoproject.Repository.*;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jspecify.annotations.NonNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DomainAdminService {


    // method 1
    @Autowired
    private StudentService studentService;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private FacultyRepository facultyRepository;
    @Autowired
    private SubAdminRepository subAdminRepository;
    @Autowired
    private FeesAdminRepository feesAdminRepository;
    @Autowired
    private FacultyService facultyService;
    @Autowired
    private SubAdminService subAdminService;
    @Autowired
    private FeesAdminService feesAdminService;
    @Autowired
    private DomainAdminRepository dAdminRepo;
    @Autowired
    private UniversityService universityService;
    @Autowired
    @Qualifier("bcryptEncoder")
    PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper modelMapper;


    //  Login by domain + Email + Password
    // public DomainAdmin LoginDomainAdmin(LoginRequestDTO loginRequestDTO){
    //     DomainAdmin dAdminLogin = dAdminRepo.findByDomainAndEmail(loginRequestDTO.getDomain(),loginRequestDTO.getEmail());
    //     boolean passwordMatch = passwordEncoder.matches(loginRequestDTO.getPassword() ,dAdminLogin.getPassword());

    //     if (passwordMatch) {
    //         dAdminLogin.setLastLoginDateTime(LocalDateTime.now());
    //         return dAdminRepo.save(dAdminLogin);
    //     }
    //     else {    return null;  }
    // }

    // login when frontend send jwt token
    public UniversityAndDomainAdminResponseDTO getDomainAdminAndUniversity( String email, String domain) {

        // 1. Find Domain Admin using authenticated email + domain
        DomainAdmin dAdminLogin = dAdminRepo.findByDomainAndEmail(domain, email);

        if (dAdminLogin == null) {
            throw new RuntimeException("Domain Admin not found");
        }

        // 2. Find University using domain
        University university = universityService.getByDomain(domain);

        if (university == null) {
            throw new RuntimeException("University not found for domain: " + domain);
        }

        // 3. Convert DomainAdmin entity -> DTO
        DomainAdminResponseDTO domainAdminDTO =  modelMapper.map( dAdminLogin, DomainAdminResponseDTO.class );

        // 4. Convert University entity -> DTO
        UniversityResponseDTO universityDTO = modelMapper.map( university, UniversityResponseDTO.class );

        // 5. Combine both DTOs
        UniversityAndDomainAdminResponseDTO response = new UniversityAndDomainAdminResponseDTO();

        response.setDomainAdminResponseDTO(domainAdminDTO);
        response.setUniversityResponseDTO(universityDTO);

//        //save last login date time
//        LocalDateTime lastLogin = dAdminLogin.getLastLoginDateTime();
//        dAdminLogin.setLastLoginDateTime(LocalDateTime.now());
//
//        dAdminRepo.save(dAdminLogin);
//
//        domainAdminDTO.setLastLoginDateTime(lastLogin);
//
        return response;
    }

    // Update Password or Forget Password
     public boolean updatePasswordByEmail(String domain, String email, String newPass ) {
        DomainAdmin old = dAdminRepo.findByDomainAndEmail(domain, email);
        if (old == null) return false;

        old.setPassword(passwordEncoder.encode(newPass));
        dAdminRepo.save(old);
        return true;

    }

// ------ UPDATE DomainAdmin Profile by Email ------

    public Boolean updateDomainAdminByEmail(@NonNull String domain, @NonNull DomainAdmin newData) {

        DomainAdmin old = dAdminRepo.findByEmailAndDomain(newData.getEmail(), domain).orElse(null);

        if (old == null) return false;

        if (newData.getName() != null)
            old.setName(newData.getName());

        if (newData.getMobileNumber() != null)
            old.setMobileNumber(newData.getMobileNumber());

        dAdminRepo.save(old);

        return true;
    }

    //    updata profile picture
    public String updateProfilePic(String domain,String email, MultipartFile file) throws IOException {

        DomainAdmin domainAdmin= dAdminRepo.findByDomainAndEmail(domain ,email);

        String uploadDir = "uploads/profile/";
        Files.createDirectories(Paths.get(uploadDir));

        String fileName = System.currentTimeMillis()+"_"+file.getOriginalFilename();

        Path path = Paths.get(uploadDir,fileName);

        Files.write(path,file.getBytes());

        domainAdmin.setProfilePic(uploadDir+fileName);

        dAdminRepo.save(domainAdmin);

        return uploadDir+fileName;
    }


// These all are use by Main Admin or Application Admin
// **** this is for official use only no others  *****
    //  READ ONE by domain
    public DomainAdmin getDomainAdminByDomain(String domain){
        return dAdminRepo.findByDomain(domain).orElse(null);

    }

    // ------ READ ALL DomainAdmin  ------
    public List<DomainAdmin> getAllDomainAdmin() {
        return dAdminRepo.findAll();
    }

    // ------ Delete DomainAdmin for specific university ------
    public String deleteDomainAdminByGmail(String domain, String gmail){
        String delete = dAdminRepo.deleteByDomainAndEmail(domain,gmail);
        return "Deleted Domain Admin with gmail " + gmail + " \n" + delete;
    }












    // ============================================================
    // STUDENT EXCEL UPLOAD
    // ============================================================

    public BulkUploadResultDTO uploadStudentsFromExcel(
            String domain,
            MultipartFile[] files
    ) {

        validateFiles(files);

        BulkUploadResultDTO result = new BulkUploadResultDTO();
        Set<String> uploadedEmails = new HashSet<>();

        for (MultipartFile file : files) {

            if (file == null || file.isEmpty()) {
                continue;
            }

            validateExcelFile(file);

            try (
                    InputStream inputStream = file.getInputStream();
                    Workbook workbook = new XSSFWorkbook(inputStream)
            ) {

                Sheet sheet = workbook.getSheetAt(0);
                DataFormatter formatter = new DataFormatter();

                validateHeader(
                        sheet.getRow(0),
                        new String[]{
                                "rollNumber",
                                "name",
                                "email",
                                "mobileNumber",
                                "course",
                                "branch",
                                "batch",
                                "studyBatch",
                                "studySubjects",
                                "fatherName",
                                "fatherMobNo",
                                "password"
                        },
                        file.getOriginalFilename(),
                        formatter
                );

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                    Row row = sheet.getRow(i);

                    if (isEmptyRow(row, formatter)) {
                        continue;
                    }

                    int rowNumber = i + 1;

                    result.setTotalRows(
                            result.getTotalRows() + 1
                    );

                    try {

                        StudentSignupDTO student = new StudentSignupDTO();

                        student.setRollNumber(
                                getCell(row, 0, formatter)
                        );

                        student.setName(
                                getCell(row, 1, formatter)
                        );

                        student.setEmail(
                                normalizeEmail(
                                        getCell(row, 2, formatter)
                                )
                        );

                        student.setMobileNumber(
                                getCell(row, 3, formatter)
                        );

                        student.setCourse(
                                getCell(row, 4, formatter)
                        );

                        student.setBranch(
                                getCell(row, 5, formatter)
                        );

                        student.setBatch(
                                getCell(row, 6, formatter)
                        );

                        student.setStudyBatch(
                                getCell(row, 7, formatter)
                        );

                        // Normal String
                        student.setStudySubjects(
                                getCell(row, 8, formatter)
                        );

                        student.setFatherName(
                                getCell(row, 9, formatter)
                        );

                        student.setFatherMobNo(
                                getCell(row, 10, formatter)
                        );

                        student.setPassword(
                                getCell(row, 11, formatter)
                        );


                        String error = validateStudent(student);

                        if (error != null) {

                            result.addInvalid(
                                    file.getOriginalFilename(),
                                    rowNumber,
                                    student.getEmail(),
                                    error
                            );

                            continue;
                        }


                        String email = student.getEmail();

                        if (!uploadedEmails.add(email)) {

                            result.addDuplicate(
                                    file.getOriginalFilename(),
                                    rowNumber,
                                    email,
                                    "Duplicate email in uploaded files"
                            );

                            continue;
                        }


                        if (studentRepository.existsByEmailIgnoreCase(email)) {

                            result.addDuplicate(
                                    file.getOriginalFilename(),
                                    rowNumber,
                                    email,
                                    "Student already exists with this email"
                            );

                            continue;
                        }


                        studentService.addStudent(
                                domain,
                                student
                        );

                        result.setSavedRows(
                                result.getSavedRows() + 1
                        );

                    } catch (Exception e) {

                        result.addInvalid(
                                file.getOriginalFilename(),
                                rowNumber,
                                getCell(row, 2, formatter),
                                getErrorMessage(
                                        e,
                                        "Failed to save student"
                                )
                        );
                    }
                }

            } catch (Exception e) {

                throw new IllegalArgumentException(
                        "Failed to process file "
                                + file.getOriginalFilename()
                                + ": "
                                + getErrorMessage(
                                e,
                                "Invalid Excel file"
                        ),
                        e
                );
            }
        }

        return result;
    }


    // ============================================================
    // FACULTY EXCEL UPLOAD
    // ============================================================

    public BulkUploadResultDTO uploadFacultyFromExcel(
            String domain,
            MultipartFile[] files
    ) {

        validateFiles(files);

        BulkUploadResultDTO result = new BulkUploadResultDTO();
        Set<String> uploadedEmails = new HashSet<>();

        for (MultipartFile file : files) {

            if (file == null || file.isEmpty()) {
                continue;
            }

            validateExcelFile(file);

            try (
                    InputStream inputStream = file.getInputStream();
                    Workbook workbook = new XSSFWorkbook(inputStream)
            ) {

                Sheet sheet = workbook.getSheetAt(0);
                DataFormatter formatter = new DataFormatter();

                validateHeader(
                        sheet.getRow(0),
                        new String[]{
                                "facultyId",
                                "name",
                                "email",
                                "mobileNumber",
                                "course",
                                "teachingAssignments",
                                "password"
                        },
                        file.getOriginalFilename(),
                        formatter
                );


                for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                    Row row = sheet.getRow(i);

                    if (isEmptyRow(row, formatter)) {
                        continue;
                    }

                    int rowNumber = i + 1;

                    result.setTotalRows(
                            result.getTotalRows() + 1
                    );

                    try {

                        FacultySignupDTO faculty =
                                new FacultySignupDTO();

                        faculty.setFacultyId(
                                getCell(row, 0, formatter)
                        );

                        faculty.setName(
                                getCell(row, 1, formatter)
                        );

                        faculty.setEmail(
                                normalizeEmail(
                                        getCell(row, 2, formatter)
                                )
                        );

                        faculty.setMobileNumber(
                                getCell(row, 3, formatter)
                        );

                        faculty.setCourse(
                                getCell(row, 4, formatter)
                        );


                        /*
                         * Keep as normal String.
                         *
                         * Example:
                         *
                         * 1A:JAVA,C,DSA;2A:AI,ML,OS
                         */
                        faculty.setTeachingAssignments(
                                getCell(row, 5, formatter)
                        );

                        faculty.setPassword(
                                getCell(row, 6, formatter)
                        );


                        String error =
                                validateFaculty(faculty);

                        if (error != null) {

                            result.addInvalid(
                                    file.getOriginalFilename(),
                                    rowNumber,
                                    faculty.getEmail(),
                                    error
                            );

                            continue;
                        }


                        String email = faculty.getEmail();

                        if (!uploadedEmails.add(email)) {

                            result.addDuplicate(
                                    file.getOriginalFilename(),
                                    rowNumber,
                                    email,
                                    "Duplicate email in uploaded files"
                            );

                            continue;
                        }


                        if (facultyRepository.existsByEmailIgnoreCase(email)) {

                            result.addDuplicate(
                                    file.getOriginalFilename(),
                                    rowNumber,
                                    email,
                                    "Faculty already exists with this email"
                            );

                            continue;
                        }


                        facultyService.addFaculty(
                                domain,
                                faculty
                        );

                        result.setSavedRows(
                                result.getSavedRows() + 1
                        );

                    } catch (Exception e) {

                        result.addInvalid(
                                file.getOriginalFilename(),
                                rowNumber,
                                getCell(row, 2, formatter),
                                getErrorMessage(
                                        e,
                                        "Failed to save faculty"
                                )
                        );
                    }
                }

            } catch (Exception e) {

                throw new IllegalArgumentException(
                        "Failed to process file "
                                + file.getOriginalFilename()
                                + ": "
                                + getErrorMessage(
                                e,
                                "Invalid Excel file"
                        ),
                        e
                );
            }
        }

        return result;
    }


    // ============================================================
    // SUB ADMIN EXCEL UPLOAD
    // ============================================================

    public BulkUploadResultDTO uploadSubAdminFromExcel(
            String domain,
            MultipartFile[] files
    ) {

        validateFiles(files);

        BulkUploadResultDTO result = new BulkUploadResultDTO();
        Set<String> uploadedEmails = new HashSet<>();

        for (MultipartFile file : files) {

            if (file == null || file.isEmpty()) {
                continue;
            }

            validateExcelFile(file);

            try (
                    InputStream inputStream = file.getInputStream();
                    Workbook workbook = new XSSFWorkbook(inputStream)
            ) {

                Sheet sheet = workbook.getSheetAt(0);
                DataFormatter formatter = new DataFormatter();

                validateHeader(
                        sheet.getRow(0),
                        new String[]{
                                "subAdminId",
                                "name",
                                "email",
                                "mobileNumber",
                                "course",
                                "teachingAssignments",
                                "password"
                        },
                        file.getOriginalFilename(),
                        formatter
                );


                for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                    Row row = sheet.getRow(i);

                    if (isEmptyRow(row, formatter)) {
                        continue;
                    }

                    int rowNumber = i + 1;

                    result.setTotalRows(
                            result.getTotalRows() + 1
                    );

                    try {

                        SubAdminSignupDTO subAdmin =
                                new SubAdminSignupDTO();

                        subAdmin.setSubAdminId(
                                getCell(row, 0, formatter)
                        );

                        subAdmin.setName(
                                getCell(row, 1, formatter)
                        );

                        subAdmin.setEmail(
                                normalizeEmail(
                                        getCell(row, 2, formatter)
                                )
                        );

                        subAdmin.setMobileNumber(
                                getCell(row, 3, formatter)
                        );

                        subAdmin.setCourse(
                                getCell(row, 4, formatter)
                        );


                        // Normal String
                        subAdmin.setTeachingAssignments(
                                getCell(row, 5, formatter)
                        );

                        subAdmin.setPassword(
                                getCell(row, 6, formatter)
                        );


                        String error =
                                validateSubAdmin(subAdmin);

                        if (error != null) {

                            result.addInvalid(
                                    file.getOriginalFilename(),
                                    rowNumber,
                                    subAdmin.getEmail(),
                                    error
                            );

                            continue;
                        }


                        String email = subAdmin.getEmail();

                        if (!uploadedEmails.add(email)) {

                            result.addDuplicate(
                                    file.getOriginalFilename(),
                                    rowNumber,
                                    email,
                                    "Duplicate email in uploaded files"
                            );

                            continue;
                        }


                        if (subAdminRepository.existsByEmailIgnoreCase(email)) {

                            result.addDuplicate(
                                    file.getOriginalFilename(),
                                    rowNumber,
                                    email,
                                    "SubAdmin already exists with this email"
                            );

                            continue;
                        }


                        subAdminService.addSubAdmin(
                                domain,
                                subAdmin
                        );

                        result.setSavedRows(
                                result.getSavedRows() + 1
                        );

                    } catch (Exception e) {

                        result.addInvalid(
                                file.getOriginalFilename(),
                                rowNumber,
                                getCell(row, 2, formatter),
                                getErrorMessage(
                                        e,
                                        "Failed to save SubAdmin"
                                )
                        );
                    }
                }

            } catch (Exception e) {

                throw new IllegalArgumentException(
                        "Failed to process file "
                                + file.getOriginalFilename()
                                + ": "
                                + getErrorMessage(
                                e,
                                "Invalid Excel file"
                        ),
                        e
                );
            }
        }

        return result;
    }


    // ============================================================
    // FEES ADMIN EXCEL UPLOAD
    // ============================================================

    public BulkUploadResultDTO uploadFeesAdminFromExcel(
            String domain,
            MultipartFile[] files
    ) {

        validateFiles(files);

        BulkUploadResultDTO result = new BulkUploadResultDTO();
        Set<String> uploadedEmails = new HashSet<>();

        for (MultipartFile file : files) {

            if (file == null || file.isEmpty()) {
                continue;
            }

            validateExcelFile(file);

            try (
                    InputStream inputStream = file.getInputStream();
                    Workbook workbook = new XSSFWorkbook(inputStream)
            ) {

                Sheet sheet = workbook.getSheetAt(0);
                DataFormatter formatter = new DataFormatter();

                validateHeader(
                        sheet.getRow(0),
                        new String[]{
                                "feesAdminId",
                                "name",
                                "email",
                                "mobileNumber",
                                "password"
                        },
                        file.getOriginalFilename(),
                        formatter
                );


                for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                    Row row = sheet.getRow(i);

                    if (isEmptyRow(row, formatter)) {
                        continue;
                    }

                    int rowNumber = i + 1;

                    result.setTotalRows(
                            result.getTotalRows() + 1
                    );

                    try {

                        FeesAdminSignupDTO feesAdmin =
                                new FeesAdminSignupDTO();

                        feesAdmin.setFeesAdminId(
                                getCell(row, 0, formatter)
                        );

                        feesAdmin.setName(
                                getCell(row, 1, formatter)
                        );

                        feesAdmin.setEmail(
                                normalizeEmail(
                                        getCell(row, 2, formatter)
                                )
                        );

                        feesAdmin.setMobileNumber(
                                getCell(row, 3, formatter)
                        );

                        feesAdmin.setPassword(
                                getCell(row, 4, formatter)
                        );


                        String error =
                                validateFeesAdmin(feesAdmin);

                        if (error != null) {

                            result.addInvalid(
                                    file.getOriginalFilename(),
                                    rowNumber,
                                    feesAdmin.getEmail(),
                                    error
                            );

                            continue;
                        }


                        String email = feesAdmin.getEmail();

                        if (!uploadedEmails.add(email)) {

                            result.addDuplicate(
                                    file.getOriginalFilename(),
                                    rowNumber,
                                    email,
                                    "Duplicate email in uploaded files"
                            );

                            continue;
                        }


                        if (feesAdminRepository.existsByEmailIgnoreCase(email)) {

                            result.addDuplicate(
                                    file.getOriginalFilename(),
                                    rowNumber,
                                    email,
                                    "FeesAdmin already exists with this email"
                            );

                            continue;
                        }


                        feesAdminService.addFeesAdmin(
                                domain,
                                feesAdmin
                        );

                        result.setSavedRows(
                                result.getSavedRows() + 1
                        );

                    } catch (Exception e) {

                        result.addInvalid(
                                file.getOriginalFilename(),
                                rowNumber,
                                getCell(row, 2, formatter),
                                getErrorMessage(
                                        e,
                                        "Failed to save FeesAdmin"
                                )
                        );
                    }
                }

            } catch (Exception e) {

                throw new IllegalArgumentException(
                        "Failed to process file "
                                + file.getOriginalFilename()
                                + ": "
                                + getErrorMessage(
                                e,
                                "Invalid Excel file"
                        ),
                        e
                );
            }
        }

        return result;
    }


    // ============================================================
    // STUDENT VALIDATION
    // ============================================================

    private String validateStudent(
            StudentSignupDTO student
    ) {

        if (isBlank(student.getRollNumber()))
            return "Roll number is required";

        if (isBlank(student.getName()))
            return "Student name is required";

        if (!isValidEmail(student.getEmail()))
            return "Invalid email";

        if (!isValidMobile(student.getMobileNumber()))
            return "Mobile number must contain 10 digits";

        if (isBlank(student.getCourse()))
            return "Course is required";

        if (isBlank(student.getBranch()))
            return "Branch is required";

        if (isBlank(student.getBatch()))
            return "Batch is required";

        if (isBlank(student.getStudyBatch()))
            return "Study batch is required";

        if (isBlank(student.getStudySubjects()))
            return "Study subjects are required";

        if (isBlank(student.getFatherName()))
            return "Father name is required";

        if (!isValidMobile(student.getFatherMobNo()))
            return "Father mobile number must contain 10 digits";

        if (isBlank(student.getPassword()))
            return "Password is required";

        return null;
    }


    // ============================================================
    // FACULTY VALIDATION
    // ============================================================

    private String validateFaculty(
            FacultySignupDTO faculty
    ) {

        if (isBlank(faculty.getFacultyId()))
            return "Faculty ID is required";

        if (isBlank(faculty.getName()))
            return "Faculty name is required";

        if (!isValidEmail(faculty.getEmail()))
            return "Invalid email";

        if (!isValidMobile(faculty.getMobileNumber()))
            return "Mobile number must contain 10 digits";

        if (isBlank(faculty.getCourse()))
            return "Course is required";

        if (isBlank(faculty.getTeachingAssignments()))
            return "Teaching assignments are required";

        if (!isValidTeachingAssignments(
                faculty.getTeachingAssignments()
        )) {
            return "Invalid teaching assignments. Example: 1A:JAVA,C,DSA;2A:AI,ML,OS";
        }

        if (isBlank(faculty.getPassword()))
            return "Password is required";

        return null;
    }


    // ============================================================
    // SUB ADMIN VALIDATION
    // ============================================================

    private String validateSubAdmin(
            SubAdminSignupDTO subAdmin
    ) {

        if (isBlank(subAdmin.getSubAdminId()))
            return "SubAdmin ID is required";

        if (isBlank(subAdmin.getName()))
            return "SubAdmin name is required";

        if (!isValidEmail(subAdmin.getEmail()))
            return "Invalid email";

        if (!isValidMobile(subAdmin.getMobileNumber()))
            return "Mobile number must contain 10 digits";

        if (isBlank(subAdmin.getCourse()))
            return "Course is required";

        if (isBlank(subAdmin.getTeachingAssignments()))
            return "Teaching assignments are required";

        if (!isValidTeachingAssignments(
                subAdmin.getTeachingAssignments()
        )) {
            return "Invalid teaching assignments. Example: 1A:JAVA,C,DSA;2A:AI,ML,OS";
        }

        if (isBlank(subAdmin.getPassword()))
            return "Password is required";

        return null;
    }


    // ============================================================
    // FEES ADMIN VALIDATION
    // ============================================================

    private String validateFeesAdmin(
            FeesAdminSignupDTO feesAdmin
    ) {

        if (isBlank(feesAdmin.getFeesAdminId()))
            return "FeesAdmin ID is required";

        if (isBlank(feesAdmin.getName()))
            return "FeesAdmin name is required";

        if (!isValidEmail(feesAdmin.getEmail()))
            return "Invalid email";

        if (!isValidMobile(feesAdmin.getMobileNumber()))
            return "Mobile number must contain 10 digits";

        if (isBlank(feesAdmin.getPassword()))
            return "Password is required";

        return null;
    }


    // ============================================================
    // TEACHING ASSIGNMENT VALIDATION
    // ============================================================

    private boolean isValidTeachingAssignments(
            String value
    ) {

        if (isBlank(value)) {
            return false;
        }

        String[] batches = value.split(";");

        for (String batchData : batches) {

            batchData = batchData.trim();

            if (batchData.isEmpty()) {
                continue;
            }

            String[] parts = batchData.split(":", 2);

            if (parts.length != 2) {
                return false;
            }

            String batch = parts[0].trim();
            String subjects = parts[1].trim();

            if (batch.isEmpty() || subjects.isEmpty()) {
                return false;
            }

            String[] subjectList = subjects.split(",");

            boolean hasSubject = false;

            for (String subject : subjectList) {

                if (!subject.trim().isEmpty()) {
                    hasSubject = true;
                }
            }

            if (!hasSubject) {
                return false;
            }
        }

        return true;
    }


    // ============================================================
    // EXCEL HELPERS
    // ============================================================

    private String getCell(
            Row row,
            int column,
            DataFormatter formatter
    ) {

        if (row == null) {
            return "";
        }

        return formatter
                .formatCellValue(
                        row.getCell(column)
                )
                .trim();
    }


    private boolean isEmptyRow(
            Row row,
            DataFormatter formatter
    ) {

        if (row == null) {
            return true;
        }

        for (int i = 0; i < row.getLastCellNum(); i++) {

            if (!getCell(row, i, formatter).isEmpty()) {
                return false;
            }
        }

        return true;
    }


    private void validateHeader(
            Row header,
            String[] expectedHeaders,
            String fileName,
            DataFormatter formatter
    ) {

        if (header == null) {

            throw new IllegalArgumentException(
                    "Header row is missing in " + fileName
            );
        }

        for (int i = 0; i < expectedHeaders.length; i++) {

            String actual =
                    getCell(header, i, formatter);

            if (!expectedHeaders[i].equalsIgnoreCase(actual)) {

                throw new IllegalArgumentException(
                        "Invalid header in "
                                + fileName
                                + ". Column "
                                + (i + 1)
                                + " should be "
                                + expectedHeaders[i]
                );
            }
        }
    }


    private void validateExcelFile(
            MultipartFile file
    ) {

        String name = file.getOriginalFilename();

        if (
                name == null ||
                        !name.toLowerCase(Locale.ROOT).endsWith(".xlsx")
        ) {

            throw new IllegalArgumentException(
                    "Only .xlsx files are supported"
            );
        }
    }


    private void validateFiles(
            MultipartFile[] files
    ) {

        if (files == null || files.length == 0) {

            throw new IllegalArgumentException(
                    "Please upload at least one Excel file"
            );
        }
    }


    // ============================================================
    // COMMON HELPERS
    // ============================================================

    private boolean isBlank(String value) {

        return value == null ||
                value.trim().isEmpty();
    }


    private String normalizeEmail(String email) {

        if (email == null) {
            return "";
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }


    private boolean isValidEmail(String email) {

        if (isBlank(email)) {
            return false;
        }

        return email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        );
    }


    private boolean isValidMobile(String mobile) {

        if (isBlank(mobile)) {
            return false;
        }

        return mobile.matches("\\d{10}");
    }


    private String getErrorMessage(
            Exception e,
            String defaultMessage
    ) {

        if (
                e.getMessage() == null ||
                        e.getMessage().isBlank()
        ) {
            return defaultMessage;
        }

        return e.getMessage();
    }






























//////read Excel files (.xlsx)
////    public void uploadStudentsFromExcel(String domain, MultipartFile file) throws Exception {
////
////        InputStream is = file.getInputStream();
////        Workbook workbook = new XSSFWorkbook(is);
////        Sheet sheet = workbook.getSheetAt(0);
////
////        for (Row row : sheet) {
////
////            if (row.getRowNum() == 0) continue; // skip header
////
////            StudentSignupDTO student = new StudentSignupDTO();
////
////            student.setRollNumber(row.getCell(0).getStringCellValue());
////            student.setName(row.getCell(1).getStringCellValue());
////            student.setEmail(row.getCell(2).getStringCellValue());
////            student.setMobileNumber(row.getCell(3).getStringCellValue());
////            student.setCourse(row.getCell(4).getStringCellValue());
////            student.setBranch(row.getCell(5).getStringCellValue());
////            student.setBatch(row.getCell(6).getStringCellValue());
////            student.setFatherName(row.getCell(7).getStringCellValue());
////            student.setFatherMobNo(row.getCell(8).getStringCellValue());
////            student.setPassword(row.getCell(9).getStringCellValue());
////
////            // Save using existing method
////            addStudent(domain, student);
////        }
////
////        workbook.close();
////    }
////
//////
//////    📊 5. Excel Format (VERY IMPORTANT) ->  Your Excel file should look like this:
//////
//////    Roll   Name	        Email	     Mobile	   	Course	Branch	  Batch	    Father Name	 Father Mobile  Password
//////    101   Ashish	ashish@gmail.com   9876543210	 BTech	  CSE	 2024-2028	  Ram	      9876543210    12345
//////
//////            👉 Column order MUST match code
////
////
////    //read Excel files (.xlsx)
////    public void uploadFacultyFromExcel(String domain, MultipartFile file) throws Exception {
////
////        InputStream is = file.getInputStream();
////        Workbook workbook = new XSSFWorkbook(is);
////        Sheet sheet = workbook.getSheetAt(0);
////
////        for (Row row : sheet) {
////
////            if (row.getRowNum() == 0) continue; // skip header
////
////            FacultySignupDTO faculty = new FacultySignupDTO();
////
////            faculty.setFacultyId(row.getCell(0).getStringCellValue());
////            faculty.setName(row.getCell(1).getStringCellValue());
////            faculty.setEmail(row.getCell(2).getStringCellValue());
////            faculty.setMobileNumber(row.getCell(3).getStringCellValue());
////            faculty.setCourse(row.getCell(4).getStringCellValue());
////            faculty.setTeachingBatch(row.getCell(5).getStringCellValue());
////            faculty.setPassword(row.getCell(6).getStringCellValue());
////
////            // Save using existing method
////            addFaculty(domain, faculty);
////        }
////
////        workbook.close();
////    }
////
//////
//////    📊 5. Excel Format (VERY IMPORTANT) ->  Your Excel file should look like this:
//////
//////   FacultyId   Name	        Email	         Mobile	   	 Course 	TeachingBatch	  Password
//////    101        Ashish	    ashish@gmail.com    9876543210	  BTech	     2A,2C,...	        12345
//////
//////            👉 Column order MUST match code
////
////
////    //read Excel files (.xlsx)
//////    public void uploadSubAdminFromExcel(String domain, MultipartFile file) throws Exception {
//////
//////        InputStream is = file.getInputStream();
//////        Workbook workbook = new XSSFWorkbook(is);
//////        Sheet sheet = workbook.getSheetAt(0);
//////
//////        for (Row row : sheet) {
//////
//////            if (row.getRowNum() == 0) continue; // skip header
//////
//////            SubAdminSignupDTO subAdmin = new SubAdminSignupDTO();
//////
//////            subAdmin.setSubAdminId(row.getCell(0).getStringCellValue());
//////            subAdmin.setName(row.getCell(1).getStringCellValue());
//////            subAdmin.setEmail(row.getCell(2).getStringCellValue());
//////            subAdmin.setMobileNumber(row.getCell(3).getStringCellValue());
//////            subAdmin.setCourse(row.getCell(4).getStringCellValue());
//////            subAdmin.setPassword(row.getCell(5).getStringCellValue());
//////
//////            // Save using existing method
//////            addSubAdmin(domain, subAdmin);
//////        }
//////
//////        workbook.close();
//////    }
////
////
////
////    public void uploadSubAdminFromExcel(String domain, MultipartFile file) throws Exception {
////
////        InputStream is = file.getInputStream();
////        Workbook workbook = new XSSFWorkbook(is);
////        Sheet sheet = workbook.getSheetAt(0);
////
////        DataFormatter formatter = new DataFormatter(); // ⭐ important
////
////        for (Row row : sheet) {
////
////            if (row.getRowNum() == 0) continue; // skip header
////
////            SubAdminSignupDTO subAdmin = new SubAdminSignupDTO();
////
////            subAdmin.setSubAdminId(formatter.formatCellValue(row.getCell(0)));
////            subAdmin.setName(formatter.formatCellValue(row.getCell(1)));
////            subAdmin.setEmail(formatter.formatCellValue(row.getCell(2)));
////            subAdmin.setMobileNumber(formatter.formatCellValue(row.getCell(3)));
////            subAdmin.setCourse(formatter.formatCellValue(row.getCell(4)));
////            subAdmin.setPassword(formatter.formatCellValue(row.getCell(5)));
////
////            addSubAdmin(domain, subAdmin);
////        }
////
////        workbook.close();
////    }
////
//////
//////    📊 5. Excel Format (VERY IMPORTANT) ->  Your Excel file should look like this:
//////
//////   FacultyId   Name	        Email	         Mobile	   	 Course 	TeachingBatch	  Password
//////    101        Ashish	    ashish@gmail.com    9876543210	  BTech	     2A,2C,...	        12345
//////
//////            👉 Column order MUST match code
////
////
//
//
//// ─── Shared helper ───────────────────────────────────────────
//private String cellVal(DataFormatter fmt, Row row, int col) {
//    org.apache.poi.ss.usermodel.Cell cell = row.getCell(col);
//    if (cell == null) return "";
//    return fmt.formatCellValue(cell).trim();
//}
//
////    // ─── Student ─────────────────────────────────────────────────
////    public void uploadStudentsFromExcel(String domain, MultipartFile file) throws Exception {
////        InputStream is = file.getInputStream();
////        Workbook workbook = new XSSFWorkbook(is);
////        Sheet sheet = workbook.getSheetAt(0);
////        DataFormatter fmt = new DataFormatter();
////
////        for (Row row : sheet) {
////            if (row.getRowNum() == 0) continue;
////
////            StudentSignupDTO student = new StudentSignupDTO();
////            student.setRollNumber(cellVal(fmt, row, 0));
////            student.setName(cellVal(fmt, row, 1));
////            student.setEmail(cellVal(fmt, row, 2));
////            student.setMobileNumber(cellVal(fmt, row, 3));
////            student.setCourse(cellVal(fmt, row, 4));
////            student.setBranch(cellVal(fmt, row, 5));
////            student.setBatch(cellVal(fmt, row, 6));
////            student.setStudyBatch(cellVal(fmt, row, 7));
////            /*
////             * Column 8 = Study Subjects
////             *
////             * Example:
////             *
////             * JAVA,DSA,OS,DBMS
////             */
////            String studySubjects =  cellVal(fmt, row, 8);
////
////            List<String> subjects = new ArrayList<>();
////
////            if (studySubjects != null &&
////                    !studySubjects.isBlank()) {
////
////                subjects = Arrays.stream(
////                                studySubjects.split(",")
////                        )
////                        .map(String::trim)
////                        .filter(s -> !s.isBlank())
////                        .map(String::toUpperCase)
////                        .distinct()
////                        .toList();
////            }
////
////            student.setStudySubjects(subjects);
////
////            student.setFatherName(
////                    cellVal(fmt, row, 9)
////            );
////
////            student.setFatherMobNo(
////                    cellVal(fmt, row, 10)
////            );
////
////            student.setPassword(
////                    cellVal(fmt, row, 11)
////            );
////
////            studentService.addStudent(domain, student);
////        }
////
////        workbook.close();
////    }
//
////    // ─── Faculty ─────────────────────────────────────────────────
////    public void uploadFacultyFromExcel(String domain, MultipartFile file) throws Exception {
////        InputStream is = file.getInputStream();
////        Workbook workbook = new XSSFWorkbook(is);
////        Sheet sheet = workbook.getSheetAt(0);
////        DataFormatter fmt = new DataFormatter();
////
////        for (Row row : sheet) {
////            if (row.getRowNum() == 0) continue;
////
////            FacultySignupDTO faculty = new FacultySignupDTO();
////            faculty.setFacultyId(cellVal(fmt, row, 0));
////            faculty.setName(cellVal(fmt, row, 1));
////            faculty.setEmail(cellVal(fmt, row, 2));
////            faculty.setMobileNumber(cellVal(fmt, row, 3));
////            faculty.setCourse(cellVal(fmt, row, 4));
////            /*
////             * Excel:
////             *
////             * Column 5 = Teaching Batch
////             * Example: 2A
////             *
////             * Column 6 = Teaching Subjects
////             * Example: JAVA,DSA
////             */
////            String teachingBatch = cellVal(fmt, row, 5);
////            String teachingSubjects = cellVal(fmt, row, 6);
////
////            Map<String, List<String>> teachingAssignments =
////                    new HashMap<>();
////
////            if (teachingBatch != null && !teachingBatch.isBlank()) {
////
////                List<String> subjects = Arrays.stream(
////                                teachingSubjects.split(",")
////                        )
////                        .map(String::trim)
////                        .filter(s -> !s.isBlank())
////                        .map(String::toUpperCase)
////                        .toList();
////
////                teachingAssignments.put(
////                        teachingBatch.trim().toUpperCase(),
////                        subjects
////                );
////            }
////
////            faculty.setTeachingAssignments(teachingAssignments);
////
////            faculty.setPassword(cellVal(fmt, row, 7));
////
////            facultyService.addFaculty(domain, faculty);
////        }
////
////        workbook.close();
////    }
//
//
//
////    new by cp
//
//    // ─── Student ─────────────────────────────────────────────────
//    public BulkUploadResultDTO uploadStudentsFromExcel(
//            String domain,
//            MultipartFile[] files) throws Exception {
//
//        BulkUploadResultDTO result = new BulkUploadResultDTO();
//
//        // Keep emails from all uploaded Excel files.
//         Set<String> uploadedEmails = new HashSet<>();
//
//        for (MultipartFile file : files) {
//
//            try (
//                InputStream is = file.getInputStream();
//                Workbook workbook = new XSSFWorkbook(is)
//        ) {
//
//                Sheet sheet = workbook.getSheetAt(0);
//                DataFormatter fmt = new DataFormatter();
//
//                // Check Excel sheet
//                if (sheet.getLastRowNum() < 1) {
//                    throw new IllegalArgumentException(
//                            "Excel file does not contain any student data."
//                    );
//                }
//
//                for (Row row : sheet) {
//
//                    // Skip header
//                    if (row.getRowNum() == 0) {
//                        continue;
//                    }
//
//                    // Skip completely empty rows
//                    if (isRowEmpty(row, fmt)) {
//                        continue;
//                    }
//
//                    int excelRow = row.getRowNum() + 1; result.setTotalRows( result.getTotalRows() + 1 );
//
//                    StudentSignupDTO student =
//                            new StudentSignupDTO();
//
//                    // ─── Column 0 ─────────────────────────────
//                    student.setRollNumber(
//                            cellVal(fmt, row, 0)
//                    );
//
//                    // ─── Column 1 ─────────────────────────────
//                    student.setName(
//                            cellVal(fmt, row, 1)
//                    );
//
//                    // ─── Column 2 ─────────────────────────────
//                    student.setEmail(
//                            cellVal(fmt, row, 2)
//                    );
//
//                    // ─── Column 3 ─────────────────────────────
//                    student.setMobileNumber(
//                            cellVal(fmt, row, 3)
//                    );
//
//                    // ─── Column 4 ─────────────────────────────
//                    student.setCourse(
//                            cellVal(fmt, row, 4)
//                    );
//
//                    // ─── Column 5 ─────────────────────────────
//                    student.setBranch(
//                            cellVal(fmt, row, 5)
//                    );
//
//                    // ─── Column 6 ─────────────────────────────
//                    student.setBatch(
//                            cellVal(fmt, row, 6)
//                    );
//
//                    // ─── Column 7 ─────────────────────────────
//                    student.setStudyBatch(
//                            cellVal(fmt, row, 7)
//                    );
//
//                    // ─── Column 8 ─────────────────────────────
//                    /*
//                     * Example:
//                     *
//                     * JAVA,DSA,OS,DBMS
//                     */
//                    String studySubjects =
//                            cellVal(fmt, row, 8);
//
//
//                    student.setStudySubjects(studySubjects);
//
//                    // ─── Column 9 ─────────────────────────────
//                    student.setFatherName(
//                            cellVal(fmt, row, 9)
//                    );
//
//                    // ─── Column 10 ────────────────────────────
//                    student.setFatherMobNo(
//                            cellVal(fmt, row, 10)
//                    );
//
//                    // ─── Column 11 ────────────────────────────
//                    student.setPassword(
//                            cellVal(fmt, row, 11)
//                    );
//
//
//                    String email = student.getEmail() == null ? "" : student.getEmail() .trim() .toLowerCase();
//
//                    // ══════════════════════════════════════════
//                    //              VALIDATION
//                    // ══════════════════════════════════════════
//
//
//                    // Roll Number
//                    if (student.getRollNumber() == null ||
//                            student.getRollNumber().isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(), excelRow, email,
//                                "Roll number is required" );
//                        continue;
//                    }
//
//
//                    // Name
//                    if (student.getName() == null ||
//                            student.getName().isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(), excelRow, email,
//                                "Student name is required" );
//                        continue;
//                    }
//
//
//                    // Email
//                    if (email.isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(), excelRow, email,
//                                "Email is required" );
//                        continue;
//                    }
//
//
//                    // Mobile Number
//                    if (student.getMobileNumber() == null ||
//                            student.getMobileNumber().isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(), excelRow, email,
//                                "Mobile number is required" );
//                        continue;
//                    }
//
//
//                    // Course
//                    if (student.getCourse() == null ||
//                            student.getCourse().isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(), excelRow, email,
//                                "Course is required" );
//                        continue;
//                    }
//
//
//                    // Branch
//                    if (student.getBranch() == null ||
//                            student.getBranch().isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(), excelRow, email,
//                                "Branch is required" );
//                        continue;                    }
//
//
//                    // Batch
//                    if (student.getBatch() == null ||
//                            student.getBatch().isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(), excelRow, email,
//                                "Batch is required" );
//                        continue;
//                    }
//
//
//                    // Study Batch
//                    if (student.getStudyBatch() == null ||
//                            student.getStudyBatch().isBlank()) {
//
//                        result.addInvalid( file.getOriginalFilename(), excelRow, email,
//                                "Study batch is required" );
//                        continue;                    }
//
//
//                    // Study Subjects
//                    if (studySubjects.isEmpty()) {
//
//                        result.addInvalid( file.getOriginalFilename(), excelRow, email,
//                                "Study subjects are required" );
//                        continue;
//                    }
//
//
//                    // Father Name
//                    if (student.getFatherName() == null ||
//                            student.getFatherName().isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(), excelRow, email,
//                                "Father name is required" );
//                        continue;
//                    }
//
//
//                    // Father Mobile
//                    if (student.getFatherMobNo() == null ||
//                            student.getFatherMobNo().isBlank()) {
//
//                        result.addInvalid(file.getOriginalFilename(), excelRow, email,
//                                "Father mobile number is required");
//                        continue;
//                    }
//
//                    // Password
//                    if (student.getPassword() == null ||
//                            student.getPassword().isBlank()) {
//                        result.addInvalid( file.getOriginalFilename(), excelRow, email,
//                                "Password is required" );
//                        continue;
//                    }
//
//                    // =================================================
//                    // DUPLICATE INSIDE UPLOADED FILES
//                    // =================================================
//
//                    if (!uploadedEmails.add(email)) {
//
//                        result.addDuplicate( file.getOriginalFilename(),
//                                excelRow, email, "Duplicate email inside uploaded Excel files" );
//                        continue;
//                    }
//
//                    // =================================================
//                    // CHECK DATABASE
//                    // =================================================
//                    if (studentRepository .existsByEmailIgnoreCase(email)) {
//
//                        result.addDuplicate( file.getOriginalFilename(), excelRow, email,
//                                "User already exists with this email" );
//                        continue;
//
//                    }
//
//                    // ══════════════════════════════════════════
//                    //          SAVE STUDENT
//                    // ══════════════════════════════════════════
//
//                    try {
//
//                        studentService.addStudent( domain, student );
//
//                        result.setSavedRows( result.getSavedRows() + 1 );
//
//                    } catch (Exception e) {
//                        /* * Important: * One failed record should NOT stop * the remaining Excel files. */
//                         result.addInvalid(
//                                 file.getOriginalFilename(),
//                                 excelRow, email,
//                                 e.getMessage() != null
//                                         ? e.getMessage()
//                                         : "Failed to save student"
//                        );
//                    }
//                }
//            }
//        }
//        return result;
//    }
//
//
//    private Map<String, List<String>> parseTeachingAssignments(
//            String value) {
//
//        Map<String, List<String>> assignments =
//                new LinkedHashMap<>();
//
//        if (value == null || value.isBlank()) {
//            return assignments;
//        }
//
//        /*
//         * Expected:
//         *
//         * 1A:JAVA,C,DSA;
//         * 2A:AI,ML,OS;
//         * 2C:OS,AI;
//         * 3A:MATH
//         */
//
//        String[] batchEntries =
//                value.split(";");
//
//        for (String entry : batchEntries) {
//
//            if (entry == null || entry.isBlank()) {
//                continue;
//            }
//
//            entry = entry.trim();
//
//            /*
//             * Split only at the first :
//             *
//             * 1A:JAVA,C,DSA
//             *  ^
//             */
//            String[] parts =
//                    entry.split(":", 2);
//
//            if (parts.length != 2) {
//
//                throw new IllegalArgumentException(
//                        "Invalid teaching assignment format: "
//                                + entry
//                                + ". Expected BATCH:SUBJECT1,SUBJECT2"
//                );
//            }
//
//            String batch =
//                    parts[0]
//                            .trim()
//                            .toUpperCase();
//
//            String subjectsText =
//                    parts[1].trim();
//
//            if (batch.isBlank()) {
//
//                throw new IllegalArgumentException(
//                        "Batch cannot be empty in assignment: "
//                                + entry
//                );
//            }
//
//            if (subjectsText.isBlank()) {
//
//                throw new IllegalArgumentException(
//                        "Subjects cannot be empty for batch: "
//                                + batch
//                );
//            }
//
//            List<String> subjects =
//                    Arrays.stream(
//                                    subjectsText.split(",")
//                            )
//                            .map(String::trim)
//                            .filter(s -> !s.isBlank())
//                            .map(String::toUpperCase)
//                            .distinct()
//                            .collect(Collectors.toList());
//
//            if (subjects.isEmpty()) {
//
//                throw new IllegalArgumentException(
//                        "No subjects found for batch: "
//                                + batch
//                );
//            }
//
//            /*
//             * If the same batch occurs twice:
//             *
//             * 1A:JAVA,C;1A:DSA
//             *
//             * result:
//             *
//             * 1A -> JAVA,C,DSA
//             */
//
//            assignments.merge(
//                    batch,
//                    subjects,
//                    (existing, newSubjects) -> {
//
//                        List<String> merged =
//                                new ArrayList<>(existing);
//
//                        for (String subject : newSubjects) {
//
//                            if (!merged.contains(subject)) {
//                                merged.add(subject);
//                            }
//                        }
//
//                        return merged;
//                    }
//            );
//        }
//
//        return assignments;
//    }
//
//    private boolean isRowEmpty(
//            Row row,
//            DataFormatter fmt) {
//
//        for (int i = 0; i < row.getLastCellNum(); i++) {
//
//            if (!cellVal(fmt, row, i).isBlank()) {
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//// ─── Faculty Bulk Upload ─────────────────────────────────────────────
//
//    public BulkUploadResultDTO uploadFacultyFromExcel(
//            String domain,
//            MultipartFile[] files) throws Exception {
//
//        BulkUploadResultDTO result =
//                new BulkUploadResultDTO();
//
//        // Duplicate emails between all uploaded Faculty Excel files
//        Set<String> uploadedEmails =
//                new HashSet<>();
//
//        for (MultipartFile file : files) {
//
//            try (
//                    InputStream is = file.getInputStream();
//                    Workbook workbook = new XSSFWorkbook(is)
//            ) {
//
//                Sheet sheet = workbook.getSheetAt(0);
//                DataFormatter fmt = new DataFormatter();
//
//                if (sheet.getLastRowNum() < 1) {
//                    continue;
//                }
//
//                for (Row row : sheet) {
//
//                    // Skip header
//                    if (row.getRowNum() == 0) {
//                        continue;
//                    }
//
//                    // Skip empty row
//                    if (isRowEmpty(row, fmt)) {
//                        continue;
//                    }
//
//                    int excelRow =
//                            row.getRowNum() + 1;
//
//                    result.setTotalRows(
//                            result.getTotalRows() + 1
//                    );
//
//                    FacultySignupDTO faculty =
//                            new FacultySignupDTO();
//
//                    // =================================================
//                    // COLUMN 0 - FACULTY ID
//                    // =================================================
//
//                    faculty.setFacultyId(
//                            cellVal(fmt, row, 0)
//                    );
//
//                    // =================================================
//                    // COLUMN 1 - NAME
//                    // =================================================
//
//                    faculty.setName(
//                            cellVal(fmt, row, 1)
//                    );
//
//                    // =================================================
//                    // COLUMN 2 - EMAIL
//                    // =================================================
//
//                    faculty.setEmail(
//                            cellVal(fmt, row, 2)
//                    );
//
//                    // =================================================
//                    // COLUMN 3 - MOBILE
//                    // =================================================
//
//                    faculty.setMobileNumber(
//                            cellVal(fmt, row, 3)
//                    );
//
//                    // =================================================
//                    // COLUMN 4 - COURSE
//                    // =================================================
//
//                    faculty.setCourse(
//                            cellVal(fmt, row, 4)
//                    );
//
//                    // =================================================
//                    // COLUMN 5 - TEACHING ASSIGNMENTS
//                    // =================================================
//
//                    String teachingAssignments =
//                            cellVal(fmt, row, 5);
//
//
//                    faculty.setTeachingAssignments( teachingAssignments );
//
//                    // =================================================
//                    // COLUMN 6 - PASSWORD
//                    // =================================================
//
//                    faculty.setPassword(
//                            cellVal(fmt, row, 6)
//                    );
//
//                    String email =
//                            faculty.getEmail() == null
//                                    ? ""
//                                    : faculty.getEmail()
//                                    .trim()
//                                    .toLowerCase();
//
//                    // =================================================
//                    // VALIDATION
//                    // =================================================
//
//                    if (faculty.getFacultyId() == null ||
//                            faculty.getFacultyId().isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "Faculty ID is required"
//                        );
//
//                        continue;
//                    }
//
//                    if (faculty.getName() == null ||
//                            faculty.getName().isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "Faculty name is required"
//                        );
//
//                        continue;
//                    }
//
//                    if (email.isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "Faculty email is required"
//                        );
//
//                        continue;
//                    }
//
//                    if (faculty.getMobileNumber() == null ||
//                            faculty.getMobileNumber().isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "Mobile number is required"
//                        );
//
//                        continue;
//                    }
//
//                    if (faculty.getCourse() == null ||
//                            faculty.getCourse().isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "Course is required"
//                        );
//
//                        continue;
//                    }
//
//                    if (teachingAssignments.isEmpty()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "Teaching assignments are required. Expected format: BATCH:SUBJECT1,SUBJECT2"
//                        );
//
//                        continue;
//                    }
//
//                    if (faculty.getPassword() == null ||
//                            faculty.getPassword().isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "Password is required"
//                        );
//
//                        continue;
//                    }
//
//                    // =================================================
//                    // DUPLICATE IN UPLOADED FILES
//                    // =================================================
//
//                    if (!uploadedEmails.add(email)) {
//
//                        result.addDuplicate(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "Duplicate email inside uploaded Faculty Excel files"
//                        );
//
//                        continue;
//                    }
//
//                    // =================================================
//                    // DATABASE DUPLICATE
//                    // =================================================
//
//                    if (facultyRepository
//                            .existsByEmailIgnoreCase(email)) {
//
//                        result.addDuplicate(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "User already exists with this email"
//                        );
//
//                        continue;
//                    }
//
//                    // =================================================
//                    // SAVE
//                    // =================================================
//
//                    try {
//
//                        facultyService.addFaculty(
//                                domain,
//                                faculty
//                        );
//
//                        result.setSavedRows(
//                                result.getSavedRows() + 1
//                        );
//
//                    } catch (Exception e) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                e.getMessage() != null
//                                        ? e.getMessage()
//                                        : "Failed to save Faculty"
//                        );
//                    }
//                }
//            }
//        }
//
//        return result;
//    }
//
//
//// ─── SubAdmin Bulk Upload ────────────────────────────────────────────
//
//    public BulkUploadResultDTO uploadSubAdminFromExcel(
//            String domain,
//            MultipartFile[] files) throws Exception {
//
//        BulkUploadResultDTO result =
//                new BulkUploadResultDTO();
//
//        // Duplicate emails across all uploaded files
//        Set<String> uploadedEmails =
//                new HashSet<>();
//
//        for (MultipartFile file : files) {
//
//            try (
//                    InputStream is = file.getInputStream();
//                    Workbook workbook = new XSSFWorkbook(is)
//            ) {
//
//                Sheet sheet = workbook.getSheetAt(0);
//                DataFormatter fmt = new DataFormatter();
//
//                if (sheet.getLastRowNum() < 1) {
//                    continue;
//                }
//
//                for (Row row : sheet) {
//
//                    // Skip header
//                    if (row.getRowNum() == 0) {
//                        continue;
//                    }
//
//                    // Skip empty row
//                    if (isRowEmpty(row, fmt)) {
//                        continue;
//                    }
//
//                    int excelRow =
//                            row.getRowNum() + 1;
//
//                    result.setTotalRows(
//                            result.getTotalRows() + 1
//                    );
//
//                    SubAdminSignupDTO subAdmin =
//                            new SubAdminSignupDTO();
//
//                    // =================================================
//                    // COLUMN 0 - SUB ADMIN ID
//                    // =================================================
//
//                    subAdmin.setSubAdminId(
//                            cellVal(fmt, row, 0)
//                    );
//
//                    // =================================================
//                    // COLUMN 1 - NAME
//                    // =================================================
//
//                    subAdmin.setName(
//                            cellVal(fmt, row, 1)
//                    );
//
//                    // =================================================
//                    // COLUMN 2 - EMAIL
//                    // =================================================
//
//                    subAdmin.setEmail(
//                            cellVal(fmt, row, 2)
//                    );
//
//                    // =================================================
//                    // COLUMN 3 - MOBILE
//                    // =================================================
//
//                    subAdmin.setMobileNumber(
//                            cellVal(fmt, row, 3)
//                    );
//
//                    // =================================================
//                    // COLUMN 4 - COURSE
//                    // =================================================
//
//                    subAdmin.setCourse(
//                            cellVal(fmt, row, 4)
//                    );
//
//                    // =================================================
//                    // COLUMN 5 - TEACHING ASSIGNMENTS
//                    // =================================================
//
//                    String teachingAssignments =
//                            cellVal(fmt, row, 5);
//
//                    subAdmin.setTeachingAssignments(teachingAssignments);
//
//                    // =================================================
//                    // COLUMN 6 - PASSWORD
//                    // =================================================
//
//                    subAdmin.setPassword(
//                            cellVal(fmt, row, 6)
//                    );
//
//                    String email =
//                            subAdmin.getEmail() == null
//                                    ? ""
//                                    : subAdmin.getEmail()
//                                    .trim()
//                                    .toLowerCase();
//
//                    // =================================================
//                    // VALIDATION
//                    // =================================================
//
//                    if (subAdmin.getSubAdminId() == null ||
//                            subAdmin.getSubAdminId().isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "SubAdmin ID is required"
//                        );
//
//                        continue;
//                    }
//
//                    if (subAdmin.getName() == null ||
//                            subAdmin.getName().isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "SubAdmin name is required"
//                        );
//
//                        continue;
//                    }
//
//                    if (email.isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "SubAdmin email is required"
//                        );
//
//                        continue;
//                    }
//
//                    if (subAdmin.getMobileNumber() == null ||
//                            subAdmin.getMobileNumber().isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "Mobile number is required"
//                        );
//
//                        continue;
//                    }
//
//                    if (subAdmin.getCourse() == null ||
//                            subAdmin.getCourse().isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "Course is required"
//                        );
//
//                        continue;
//                    }
//
//                    if (teachingAssignments.isEmpty()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "Teaching assignments are required. Expected format: BATCH:SUBJECT1,SUBJECT2"
//                        );
//
//                        continue;
//                    }
//
//                    if (subAdmin.getPassword() == null ||
//                            subAdmin.getPassword().isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "Password is required"
//                        );
//
//                        continue;
//                    }
//
//                    // =================================================
//                    // DUPLICATE INSIDE UPLOAD
//                    // =================================================
//
//                    if (!uploadedEmails.add(email)) {
//
//                        result.addDuplicate(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "Duplicate email inside uploaded SubAdmin Excel files"
//                        );
//
//                        continue;
//                    }
//
//                    // =================================================
//                    // DATABASE DUPLICATE
//                    // =================================================
//
//                    if (subAdminRepository
//                            .existsByEmailIgnoreCase(email)) {
//
//                        result.addDuplicate(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "User already exists with this email"
//                        );
//
//                        continue;
//                    }
//
//                    // =================================================
//                    // SAVE
//                    // =================================================
//
//                    try {
//
//                        subAdminService.addSubAdmin(
//                                domain,
//                                subAdmin
//                        );
//
//                        result.setSavedRows(
//                                result.getSavedRows() + 1
//                        );
//
//                    } catch (Exception e) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                e.getMessage() != null
//                                        ? e.getMessage()
//                                        : "Failed to save SubAdmin"
//                        );
//                    }
//                }
//            }
//        }
//
//        return result;
//    }
//
//
//
//
//
//
///*
//Final structure
//
//Your Excel system should now work like this:
//
//                 EXCEL
//                   │
//                   ▼
//     ┌───────────────────────────┐
//     │ teachingAssignments       │
//     │                           │
//     │ 1A:JAVA,C,DSA;            │
//     │ 2A:AI,ML,OS;              │
//     │ 2C:OS,AI;                 │
//     │ 3A:MATH                   │
//     └─────────────┬─────────────┘
//                   │
//                   ▼
//          parseTeachingAssignments()
//                   │
//                   ▼
//       Map<String, List<String>>
//                   │
//                   ▼
//     ┌───────────────────────────┐
//     │ 1A → JAVA,C,DSA            │
//     │ 2A → AI,ML,OS              │
//     │ 2C → OS,AI                 │
//     │ 3A → MATH                  │
//     └─────────────┬─────────────┘
//                   │
//                   ▼
//       setTeachingAssignmentsMap()
//                   │
//                   ▼
//              PostgreSQL
//                   │
//                   ▼
//     JSONB/JSON teaching_assignments
//* */
//
//
//
//
//
//
//
////    // ─── SubAdmin ─────────────────────────────────────────────────
////    public void uploadSubAdminFromExcel(String domain, MultipartFile file) throws Exception {
////        InputStream is = file.getInputStream();
////        Workbook workbook = new XSSFWorkbook(is);
////        Sheet sheet = workbook.getSheetAt(0);
////        DataFormatter fmt = new DataFormatter();
////
////        for (Row row : sheet) {
////            if (row.getRowNum() == 0) continue;
////
////            SubAdminSignupDTO subAdmin = new SubAdminSignupDTO();
////            subAdmin.setSubAdminId(cellVal(fmt, row, 0));
////            subAdmin.setName(cellVal(fmt, row, 1));
////            subAdmin.setEmail(cellVal(fmt, row, 2));
////            subAdmin.setMobileNumber(cellVal(fmt, row, 3));
////            subAdmin.setCourse(cellVal(fmt, row, 4));
////            /*
////             * Excel:
////             *
////             * Column 5 = Teaching Batch
////             * Example: 2A
////             *
////             * Column 6 = Teaching Subjects
////             * Example: JAVA,DSA
////             */
////            String teachingBatch = cellVal(fmt, row, 5);
////            String teachingSubjects = cellVal(fmt, row, 6);
////
////            Map<String, List<String>> teachingAssignments =
////                    new HashMap<>();
////
////            if (teachingBatch != null && !teachingBatch.isBlank()) {
////
////                List<String> subjects = Arrays.stream(
////                                teachingSubjects.split(",")
////                        )
////                        .map(String::trim)
////                        .filter(s -> !s.isBlank())
////                        .map(String::toUpperCase)
////                        .toList();
////
////                teachingAssignments.put(
////                        teachingBatch.trim().toUpperCase(),
////                        subjects
////                );
////            }
////
////            subAdmin.setTeachingAssignments(teachingAssignments);
////
////            subAdmin.setPassword(cellVal(fmt, row, 7));
////
////            subAdminService.addSubAdmin(domain, subAdmin);
////        }
////
////        workbook.close();
////    }
//
//
//
//// ─── FeesAdmin Bulk Upload ───────────────────────────────────────────
//
//    public BulkUploadResultDTO uploadFeesAdminFromExcel(
//            String domain,
//            MultipartFile[] files) throws Exception {
//
//        BulkUploadResultDTO result =
//                new BulkUploadResultDTO();
//
//        // Duplicate emails across all uploaded files
//        Set<String> uploadedEmails =
//                new HashSet<>();
//
//        for (MultipartFile file : files) {
//
//            try (
//                    InputStream is = file.getInputStream();
//                    Workbook workbook = new XSSFWorkbook(is)
//            ) {
//
//                Sheet sheet = workbook.getSheetAt(0);
//                DataFormatter fmt = new DataFormatter();
//
//                if (sheet.getLastRowNum() < 1) {
//                    continue;
//                }
//
//                for (Row row : sheet) {
//
//                    // Skip header
//                    if (row.getRowNum() == 0) {
//                        continue;
//                    }
//
//                    // Skip empty rows
//                    if (isRowEmpty(row, fmt)) {
//                        continue;
//                    }
//
//                    int excelRow =
//                            row.getRowNum() + 1;
//
//                    result.setTotalRows(
//                            result.getTotalRows() + 1
//                    );
//
//                    FeesAdminSignupDTO feesAdmin =
//                            new FeesAdminSignupDTO();
//
//                    // =================================================
//                    // COLUMN 0 - FEES ADMIN ID
//                    // =================================================
//
//                    feesAdmin.setFeesAdminId(
//                            cellVal(fmt, row, 0)
//                    );
//
//                    // =================================================
//                    // COLUMN 1 - NAME
//                    // =================================================
//
//                    feesAdmin.setName(
//                            cellVal(fmt, row, 1)
//                    );
//
//                    // =================================================
//                    // COLUMN 2 - EMAIL
//                    // =================================================
//
//                    feesAdmin.setEmail(
//                            cellVal(fmt, row, 2)
//                    );
//
//                    // =================================================
//                    // COLUMN 3 - MOBILE
//                    // =================================================
//
//                    feesAdmin.setMobileNumber(
//                            cellVal(fmt, row, 3)
//                    );
//
//                    // =================================================
//                    // COLUMN 4 - PASSWORD
//                    // =================================================
//
//                    feesAdmin.setPassword(
//                            cellVal(fmt, row, 4)
//                    );
//
//                    String email =
//                            feesAdmin.getEmail() == null
//                                    ? ""
//                                    : feesAdmin.getEmail()
//                                    .trim()
//                                    .toLowerCase();
//
//                    // =================================================
//                    // VALIDATION
//                    // =================================================
//
//                    if (feesAdmin.getFeesAdminId() == null ||
//                            feesAdmin.getFeesAdminId().isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "FeesAdmin ID is required"
//                        );
//
//                        continue;
//                    }
//
//                    if (feesAdmin.getName() == null ||
//                            feesAdmin.getName().isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "FeesAdmin name is required"
//                        );
//
//                        continue;
//                    }
//
//                    if (email.isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "FeesAdmin email is required"
//                        );
//
//                        continue;
//                    }
//
//                    if (feesAdmin.getMobileNumber() == null ||
//                            feesAdmin.getMobileNumber().isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "Mobile number is required"
//                        );
//
//                        continue;
//                    }
//
//                    if (feesAdmin.getPassword() == null ||
//                            feesAdmin.getPassword().isBlank()) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "Password is required"
//                        );
//
//                        continue;
//                    }
//
//                    // =================================================
//                    // DUPLICATE INSIDE UPLOADED FILES
//                    // =================================================
//
//                    if (!uploadedEmails.add(email)) {
//
//                        result.addDuplicate(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "Duplicate email inside uploaded FeesAdmin Excel files"
//                        );
//
//                        continue;
//                    }
//
//                    // =================================================
//                    // DATABASE DUPLICATE
//                    // =================================================
//
//                    if (feesAdminRepository
//                            .existsByEmailIgnoreCase(email)) {
//
//                        result.addDuplicate(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                "User already exists with this email"
//                        );
//
//                        continue;
//                    }
//
//                    // =================================================
//                    // SAVE
//                    // =================================================
//
//                    try {
//
//                        feesAdminService.addFeesAdmin(
//                                domain,
//                                feesAdmin
//                        );
//
//                        result.setSavedRows(
//                                result.getSavedRows() + 1
//                        );
//
//                    } catch (Exception e) {
//
//                        result.addInvalid(
//                                file.getOriginalFilename(),
//                                excelRow,
//                                email,
//                                e.getMessage() != null
//                                        ? e.getMessage()
//                                        : "Failed to save FeesAdmin"
//                        );
//                    }
//                }
//            }
//        }
//
//        return result;
//    }
//



}
