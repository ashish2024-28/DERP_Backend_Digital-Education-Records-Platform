package com.demoproject.Service;

import com.demoproject.DTO.DomainAdminDTO.DomainAdminResponseDTO;
import com.demoproject.DTO.FacultyDTO.FacultyResponseDTO;
import com.demoproject.DTO.FacultyDTO.FacultySignupDTO;
import com.demoproject.DTO.StudentDTO.StudentResponseDTO;
import com.demoproject.DTO.StudentDTO.StudentSignupDTO;
import com.demoproject.DTO.SubAdminDTO.SubAdminResponseDTO;
import com.demoproject.DTO.SubAdminDTO.SubAdminSignupDTO;
import com.demoproject.Entity.*;

import com.demoproject.Repository.UniversityRepo;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.demoproject.Repository.DomainAdminRepository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class DomainAdminService {


    // method 1
    @Autowired
    private StudentService studentService;
    @Autowired
    private FacultyService facultyService;
    @Autowired
    private SubAdminService subAdminService;
    @Autowired
    private DomainAdminRepository dAdminRepo;
    @Autowired
    private UniversityService universityService;
    @Autowired
    private UniversityRepo universityRepo;
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
    public DomainAdminResponseDTO getDomainAdminByEmailAndDomain(String email, String domain){
        DomainAdmin dAdminLogin = dAdminRepo.findByDomainAndEmail(domain, email);
        University university = universityService.getByDomain(domain);

        // set lastLoginDateTime
        Instant lastLogin = dAdminLogin.getLastLoginDateTime();

        dAdminLogin.setLastLoginDateTime(Instant.now());
        dAdminLogin = dAdminRepo.save(dAdminLogin);

        dAdminLogin.setLastLoginDateTime(lastLogin);

        DomainAdminResponseDTO responseDTO = modelMapper.map(dAdminLogin, DomainAdminResponseDTO.class) ;
        responseDTO.setUniversityId(university.getId());
        responseDTO.setLastLoginDateTime(lastLogin);

        String univName = university.getUniversityName();
        if(univName.isBlank() || univName == null){
            univName = university.getInstitutionName();
        }
        responseDTO.setUniversityName(univName);
        return responseDTO;

    }

//    updata profile picture
    public String updateProfilePic(String domain,String email, MultipartFile file) throws IOException {

        DomainAdmin domainAdmin = dAdminRepo.findByDomainAndEmail(domain,email);

        String uploadDir = "uploads/profile/";
        Files.createDirectories(Paths.get(uploadDir));

        String fileName = System.currentTimeMillis()+"_"+file.getOriginalFilename();

        Path path = Paths.get(uploadDir,fileName);

        Files.write(path,file.getBytes());

        domainAdmin.setProfilePic(uploadDir+fileName);

        dAdminRepo.save(domainAdmin);

        return uploadDir+fileName;
    }

    // Update Password or Forget Password
     public boolean updatePasswordByEmail(String domain, String email, String newPass ) {
        DomainAdmin old = dAdminRepo.findByDomainAndEmail(domain, email);
        if (old == null) return false;

        old.setPassword(passwordEncoder.encode(newPass));
        dAdminRepo.save(old);
        return true;

    }


// ======== University profile Update ========

    //    updata university logo
    public String updateUniversityLogo(String domain,String email, MultipartFile file) throws IOException {

        boolean isDomainAdminExist = dAdminRepo.existsByEmailAndDomain(email, domain);
        if(!isDomainAdminExist){
            throw new RuntimeException("Invalid ! University or domain");
        }

        University university = universityRepo.findByDomain(domain);

        String uploadDir = "uploads/universityLogo/";
        Files.createDirectories(Paths.get(uploadDir));

        String fileName = System.currentTimeMillis()+"_"+file.getOriginalFilename();

        Path path = Paths.get(uploadDir,fileName);

        Files.write(path,file.getBytes());

        university.setUniversityLogoPath(uploadDir+fileName);

        universityRepo.save(university);

        return uploadDir+fileName;
    }


    // count
    public long getStudentCount(String domain) { return studentService.getStudentCount(domain);}
    public long getFacultyCount(String domain) {
        return facultyService.getFacultyCount(domain);
    }
    public long getSubAdminCount(String domain) {
        return subAdminService.getSubAdminCount(domain);
    }



// These all are use by Main Admin
    //  READ ONE by domain
    public DomainAdmin getDomainAdmin(String domain){
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

////
////    📊 5. Excel Format (VERY IMPORTANT) ->  Your Excel file should look like this:
////
////   FacultyId   Name	        Email	         Mobile	   	 Course 	TeachingBatch	  Password
////    101        Ashish	    ashish@gmail.com    9876543210	  BTech	     2A,2C,...	        12345
////
////            👉 Column order MUST match code
//
//


// ─── Shared helper ───────────────────────────────────────────
private String cellVal(DataFormatter fmt, Row row, int col) {
    org.apache.poi.ss.usermodel.Cell cell = row.getCell(col);
    if (cell == null) return "";
    return fmt.formatCellValue(cell).trim();
}

    // ─── Student ─────────────────────────────────────────────────
    public void uploadStudentsFromExcel(String domain, MultipartFile file) throws Exception {
        InputStream is = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter fmt = new DataFormatter();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            StudentSignupDTO student = new StudentSignupDTO();
            student.setRollNumber(cellVal(fmt, row, 0));
            student.setName(cellVal(fmt, row, 1));
            student.setEmail(cellVal(fmt, row, 2));
            student.setMobileNumber(cellVal(fmt, row, 3));
            student.setCourse(cellVal(fmt, row, 4));
            student.setBranch(cellVal(fmt, row, 5));
            student.setBatch(cellVal(fmt, row, 6));
            student.setFatherName(cellVal(fmt, row, 7));
            student.setFatherMobNo(cellVal(fmt, row, 8));
            student.setPassword(cellVal(fmt, row, 9));

            studentService.addStudent(domain, student);
        }
        workbook.close();
    }

    // ─── Faculty ─────────────────────────────────────────────────
    public void uploadFacultyFromExcel(String domain, MultipartFile file) throws Exception {
        InputStream is = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter fmt = new DataFormatter();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            FacultySignupDTO faculty = new FacultySignupDTO();
            faculty.setFacultyId(cellVal(fmt, row, 0));
            faculty.setName(cellVal(fmt, row, 1));
            faculty.setEmail(cellVal(fmt, row, 2));
            faculty.setMobileNumber(cellVal(fmt, row, 3));
            faculty.setCourse(cellVal(fmt, row, 4));
            faculty.setTeachingBatch(cellVal(fmt, row, 5));
            faculty.setPassword(cellVal(fmt, row, 6));

            facultyService.addFaculty(domain, faculty);
        }
        workbook.close();
    }

    // ─── SubAdmin ─────────────────────────────────────────────────
    public void uploadSubAdminFromExcel(String domain, MultipartFile file) throws Exception {
        InputStream is = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter fmt = new DataFormatter();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            SubAdminSignupDTO subAdmin = new SubAdminSignupDTO();
            subAdmin.setSubAdminId(cellVal(fmt, row, 0));
            subAdmin.setName(cellVal(fmt, row, 1));
            subAdmin.setEmail(cellVal(fmt, row, 2));
            subAdmin.setMobileNumber(cellVal(fmt, row, 3));
            subAdmin.setCourse(cellVal(fmt, row, 4));
            subAdmin.setPassword(cellVal(fmt, row, 5));

            subAdminService.addSubAdmin(domain, subAdmin);
        }
        workbook.close();
    }





}
