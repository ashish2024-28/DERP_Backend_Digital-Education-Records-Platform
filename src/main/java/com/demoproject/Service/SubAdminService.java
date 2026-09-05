package com.demoproject.Service;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.demoproject.Entity.*;
import jakarta.validation.constraints.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.demoproject.DTO.LoginRequestDTO;
import com.demoproject.DTO.FacultyDTO.FacultyResponseDTO;
import com.demoproject.DTO.StudentDTO.StudentResponseDTO;
import com.demoproject.DTO.SubAdminDTO.SubAdminResponseDTO;
import com.demoproject.DTO.SubAdminDTO.SubAdminSignupDTO;
import com.demoproject.Repository.SubAdminRepository;
import com.demoproject.Repository.UniversityRepo;
import org.springframework.web.multipart.MultipartFile;


@Service
public class SubAdminService {

    @Autowired
    private StudentService studentService;
    @Autowired
    private FacultyService facultyService;
    @Autowired
    private SubAdminRepository SArepo;
    @Autowired
    private UniversityRepo universityRepo;
    @Autowired
    private BaseUserService baseUserService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    @Qualifier("bcryptEncoder")
    private PasswordEncoder passwordEncoder ;



    //  Login by domain + email + Password
    public SubAdmin LoginSubAdmin(LoginRequestDTO loginRequestDTO){
        SubAdmin subAdminLogin = SArepo.findByEmailAndDomain(loginRequestDTO.getEmail(), loginRequestDTO.getDomain()).orElse(null);
        boolean passwordMatch = passwordEncoder.matches(loginRequestDTO.getPassword() ,subAdminLogin.getPassword());

        if (passwordMatch) {
            subAdminLogin.setLastLoginDateTime(LocalDateTime.now());
            return SArepo.save(subAdminLogin);

        } else {     return null;    }
    }


    // login when frontend send jwt token
    public SubAdminResponseDTO getSubAdminByEmailAndDomain(String email, String domain) {
        SubAdmin subAdminLogin = SArepo.findByEmailAndDomain(email,domain).orElseThrow();

        // set lastLoginDateTime
        LocalDateTime lastLogin = subAdminLogin.getLastLoginDateTime();

        subAdminLogin.setLastLoginDateTime(LocalDateTime.now());
        subAdminLogin =  SArepo.save(subAdminLogin);

        subAdminLogin.setLastLoginDateTime(lastLogin);

        SubAdminResponseDTO responseDTO =  modelMapper.map(subAdminLogin, SubAdminResponseDTO.class) ;
        responseDTO.setLastLoginDateTime(lastLogin);

        return responseDTO;
    }


    //    updata profile picture
    public String updateProfilePic(String domain,String email, MultipartFile file) throws IOException {

        SubAdmin subAdmin = SArepo.findByDomainAndEmail(domain,email);

        String uploadDir = "uploads/profile/";
        Files.createDirectories(Paths.get(uploadDir));

        String fileName = System.currentTimeMillis()+"_"+file.getOriginalFilename();

        Path path = Paths.get(uploadDir,fileName);

        Files.write(path,file.getBytes());

        subAdmin.setProfilePic(uploadDir+fileName);

        SArepo.save(subAdmin);

        return uploadDir+fileName;
    }


    // CREATE
    public String addSubAdmin(@NotNull  String domain, @NotNull SubAdminSignupDTO signupDTO){

        if(baseUserService.existsUserByEmail(signupDTO.getEmail())){
            throw new RuntimeException("User already exists with this email.");
        }

        SubAdmin requestSubAdmin = modelMapper.map(signupDTO, SubAdmin.class);

        University university = universityRepo.findByDomain(domain)
                .orElseThrow(() -> new RuntimeException("University not found"));
        requestSubAdmin.setDomain(domain);
        requestSubAdmin.setUniversity(university);

        if( SArepo.existsBySubAdminIdAndDomain(requestSubAdmin.getSubAdminId(),requestSubAdmin.getDomain())){ throw new RuntimeException("Sub Admin ID already exists.");  }
        if( SArepo.existsByDomainAndEmail(requestSubAdmin.getDomain(),requestSubAdmin.getEmail())){ throw new RuntimeException("Sub Admin's field Email are already exist for this university. ");  }
        if( SArepo.existsByEmail(requestSubAdmin.getEmail())){ throw new RuntimeException("Enter Unique Email Id. ");  }

        // for security use passwordEncoder
        requestSubAdmin.setPassword(passwordEncoder.encode(requestSubAdmin.getPassword()));
        requestSubAdmin.setRole(Role.SUB_ADMIN);
        SubAdmin save = SArepo.save(requestSubAdmin);
        return save.getName() + ",\nYour Account is Created Successfully.\nSub Admin Id : " + save.getSubAdminId() ;

    }

    // ------ READ ALL SubAdmin count for specific university ------
    public long getSubAdminCount(String domain) {
        return SArepo.countByUniversity_Domain(domain);
    }



    // ------ READ ALL domain for specific university ------
    public List<SubAdminResponseDTO> getAllSubAdmin(String domain){
        List<SubAdmin> subAdminList = SArepo.findByDomain(domain);

        return subAdminList.stream()
                .map(subAdmin -> modelMapper.map(subAdmin, SubAdminResponseDTO.class))
                .collect(Collectors.toList());
    }

    // Update Password or Forget Password
    public boolean updatePasswordByEmail(String domain, String email, String newPass ) {
        SubAdmin old = SArepo.findByEmailAndDomain(email, domain).orElse(null);
        if (old == null) return false;

        old.setPassword(passwordEncoder.encode(newPass));
        SArepo.save(old);
        return true;

    }

    // ------ UPDATE SubAdmin Profile by Email  ------
    public Boolean updateSubAdminByEmail(String domain, SubAdmin newData){
        SubAdmin old = SArepo.findByDomainAndEmail(domain, newData.getEmail());
        if (old == null) return null;

        if (newData.getName() != null)
            old.setName(newData.getName());

        if (newData.getSubAdminId() != null)
            old.setSubAdminId(newData.getSubAdminId());

        if (newData.getCourse() != null)
            old.setCourse(newData.getCourse());

        if (newData.getTeachingAssignments() != null)
            old.setTeachingAssignments(newData.getTeachingAssignments());

        if (newData.getMobileNumber() != null)
            old.setMobileNumber(newData.getMobileNumber());

        SArepo.save(old);

        return true;
    }

    // DELETE
    public String deleteSubAdminByEmail(String domain, String email){
        SubAdmin subAdmin= SArepo.findByDomainAndEmail(domain, email);
        if (subAdmin == null ) return "Not found";

        SArepo.delete(subAdmin);
        return "Deleted SubAdmin with email id : " + email;
    }

    // ------ READ ALL faculty for specific university ------
    public List<FacultyResponseDTO> getAllFacultyBySubAdminCourse(String domain, String email) {

        SubAdmin subAdmin = SArepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));

        String course = subAdmin.getCourse();

        return facultyService.getAllFacultyBySubAdminCourse(domain , course);
    }

    // ------ READ ALL student by subAdmin course for specific university  ------
    public List<StudentResponseDTO> getStudentBySubAdminCourse(String domain, String email) {

        SubAdmin subAdmin = SArepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));

        String course = subAdmin.getCourse();

        return studentService.getStudentsByCourse(domain , course);
    }



}

