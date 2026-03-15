package com.demoproject.Service;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import com.demoproject.Entity.*;
import com.demoproject.Repository.StudentRepository;
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
    private StudentRepository studentRepository;
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
            subAdminLogin.setLastLoginDateTime(Instant.now());
            return SArepo.save(subAdminLogin);
            
        } else {     return null;    }
    }


    // login when frontend send jwt token
    public SubAdminResponseDTO getSubAdminByEmailAndDomain(String email, String domain) {
        SubAdmin subAdminLogin = SArepo.findByEmailAndDomain(email,domain).orElseThrow();

        // set lastLoginDateTime
        Instant lastLogin = subAdminLogin.getLastLoginDateTime();

        subAdminLogin.setLastLoginDateTime(Instant.now());
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
    public String addSubAdmin(String domain, SubAdminSignupDTO signupDTO){

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

    // READ ONE by domain + id
    // **** this is for official use only no others  ***** 
    public SubAdmin getSubAdminById(String domain, Long id){
        return SArepo.findByIdAndDomain(id ,domain);
    }
    
    // //  READ ONE by domain + DomainId means (Id which provide by University or collage)
    public SubAdmin getSubAdminBySubAdminId(String domain, String subAdminId){
        return SArepo.findBySubAdminIdAndDomain(subAdminId, domain);
    }
    
    //  READ ONE by domain + Email
    public SubAdmin getFacultyByEmail(String domain, String email ) {

        SubAdmin subAdmin = SArepo.findByEmailAndDomain(email, domain).orElse(null); 
        return SArepo.save(subAdmin);
    }

    // Update Password or Forget Password
     public boolean updatePasswordByEmail(String domain, String email, String newPass ) {
        SubAdmin old = SArepo.findByEmailAndDomain(email, domain).orElse(null);
        if (old == null) return false;

        old.setPassword(passwordEncoder.encode(newPass));
        SArepo.save(old);
        return true;

    }

    // UPDATE 
    // **** this is for official use only no others  ***** 
    public SubAdmin updateSubAdminById(String domain, Long id, SubAdmin newData){
        SubAdmin old = SArepo.findByIdAndDomain(id, domain);
        if (old == null) return null;

        old.setName(newData.getName());
        old.setCourse(newData.getCourse());
        old.setMobileNumber(newData.getMobileNumber());
        old.setEmail(newData.getEmail());
        
        return SArepo.save(old);
    }

    // UPDATE  + SubAdminId means (subAdminId which provide by University or collage)
    public SubAdmin updateSubAdminBySubAdminId(String domain, SubAdmin newData){
        SubAdmin old = SArepo.findBySubAdminIdAndDomain(newData.getSubAdminId(), domain);
        if (old == null) return null;
        
        old.setName(newData.getName());
        old.setCourse(newData.getCourse());
        old.setMobileNumber(newData.getMobileNumber());
        
        return SArepo.save(old);
    }

    // DELETE
    // **** this is for official use only no others  ***** 
    public String deleteSubAdminbyId(String domain, Long id){
        SubAdmin sa = SArepo.findByIdAndDomain(id, domain);
        if (sa == null) return "Not found";

        SArepo.delete(sa);
        return "SubAdmin deleted: " + id;
    }

    // DELETE
    public String deleteSubAdminBySubAdminId(String domain, String subAdminId){
        SubAdmin sa = SArepo.findBySubAdminIdAndDomain(subAdminId, domain);;
        if (sa == null) return "Not found";

        SArepo.delete(sa);
        return "Deleted SubAdmin with DId (ID) : " + subAdminId;
    }



    // ------ READ ALL faculty for specific university ------
    public List<StudentResponseDTO> getStudentsByFacultyCourse(String domain, String email) {

        SubAdmin subAdmin = SArepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));

        String course = subAdmin.getCourse();

        return studentRepository.findByCourseAndDomain(course, domain)
                .stream()
                .map(student -> modelMapper.map(student, StudentResponseDTO.class))
                .toList();
    }
    //  READ ONE by domain + DomainId(Did) means (Id which provide by University or collage)
    public Faculty getFacultyByFacultyId(String domain, String facultyId ) {
        return facultyService.getFacultyByFacultyId(domain, facultyId);
    }
   



    // ------ READ ALL student for specific university ------
    public List<StudentResponseDTO> getAllStudents(String domain) {
        return studentService.getAllStudent(domain);
    }

    // ------ READ ONE by domain + rollNo ------
    public Student getStudentByRollNo(String domain, String rollNo) {
        return studentService.getStudentByRollNo(domain, rollNo);        
    }
    
    // ------ READ ONE by domain + Name ------
    public List<Student> getStudentByName(String domain, String name) {
        return studentService.getAllStudentByName(domain, name);        
    }

    // ------ READ All by domain + Branch ------
    public List<Student> getStudentByBranch(String domain,String branch) {
        return studentService.getAllStudentByBranch(domain,branch);
    }

    // ------ READ All by domain + Course ------
    public List<Student> getStudentByCourse(String domain,String course) {
        return studentService.getAllStudentByCourse(domain,course);
    }

    // ------ READ All by domain + Batch ------
    public List<Student> getStudentByBatch(String domain, String batch) {
        return studentService.getAllStudentByBatch(domain, batch);
    }



}

