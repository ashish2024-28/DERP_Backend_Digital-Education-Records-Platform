package com.demoproject.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.demoproject.Entity.*;
import com.demoproject.Repository.StudentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.demoproject.DTO.FacultyDTO.FacultyResponseDTO;
import com.demoproject.DTO.FacultyDTO.FacultySignupDTO;
import com.demoproject.DTO.StudentDTO.StudentResponseDTO;
import com.demoproject.Repository.FacultyRepository;
import com.demoproject.Repository.UniversityRepo;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FacultyService {


    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private StudentService studentService;
    @Autowired
    private FacultyRepository frepo;
    @Autowired
    private UniversityRepo universityRepo;
    @Autowired
    private BaseUserService baseUserService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    @Qualifier("bcryptEncoder")
    private PasswordEncoder passwordEncoder;


    // private final FacultyRepository frepo;
    // public FacultyService(FacultyRepository frepo) {
    //     this.frepo = frepo;
    // }

    // //  Login by domain + Gmail + Password
    // public Faculty LoginFaculty(LoginRequestDTO loginRequestDTO){
    //     Faculty facultyLogin = frepo.findByEmailAndDomain(loginRequestDTO.getEmail() ,loginRequestDTO.getDomain()).orElse(null);
    //     boolean passwordMatch = passwordEncoder.matches(loginRequestDTO.getPassword() ,facultyLogin.getPassword());
        
    //     if (passwordMatch) {
    //         facultyLogin.setLastLoginDateTime(LocalDateTime.now());
    //         return frepo.save(facultyLogin);
            
    //     } else {    return null;    }
    // }


    // login when frontend send jwt token
    public FacultyResponseDTO getFacultyByEmailAndDomain(String email, String domain) {
        Faculty facultyLogin = frepo.findByEmailAndDomain(email,domain).orElseThrow();

        // set lastLoginDateTime
        Instant lastLogin = facultyLogin.getLastLoginDateTime();

        facultyLogin.setLastLoginDateTime(Instant.now());
        facultyLogin =  frepo.save(facultyLogin);
        
        facultyLogin.setLastLoginDateTime(lastLogin);
        FacultyResponseDTO responseDTO = modelMapper.map(facultyLogin, FacultyResponseDTO.class) ;
        
        responseDTO.setLastLoginDateTime(lastLogin);
        return responseDTO;
    }


    //    updata profile picture
    public String updateProfilePic(String domain,String email, MultipartFile file) throws IOException {

        Faculty faculty= frepo.findByDomainAndEmail(domain ,email);

        String uploadDir = "uploads/profile/";
        Files.createDirectories(Paths.get(uploadDir));

        String fileName = System.currentTimeMillis()+"_"+file.getOriginalFilename();

        Path path = Paths.get(uploadDir,fileName);

        Files.write(path,file.getBytes());

        faculty.setProfilePic(uploadDir+fileName);

        frepo.save(faculty);

        return uploadDir+fileName;
    }



    // ---- CREATE ------
    public String addFaculty(String domain, FacultySignupDTO facultySignupDTO) {
        
        Faculty requesFaculty = modelMapper.map(facultySignupDTO, Faculty.class);

        boolean emailExist = baseUserService.existsUserByEmail(facultySignupDTO.getEmail());
        if(emailExist){
            throw new RuntimeException("User Exist Please Try Another Email Id.");
        }

        University university = universityRepo.findByDomain(domain)
        .orElseThrow(() -> new RuntimeException("Invalid domain"));
        requesFaculty.setDomain(domain);
        requesFaculty.setUniversity(university);

        if( frepo.existsByFacultyIdAndDomain(requesFaculty.getFacultyId(),requesFaculty.getDomain()) ){    throw new RuntimeException("Faculty's Id field are already exist. ");  }
        if( frepo.existsByDomainAndEmail(requesFaculty.getDomain(),requesFaculty.getEmail()) ){    throw new RuntimeException("Faculty's Email field are already exist. ");  }
        if( frepo.existsByEmail(requesFaculty.getEmail())){ throw new RuntimeException("Enter Unique Email Id or Another Email Id . ");  }
       
        // for security use passwordEncoder
        requesFaculty.setPassword(passwordEncoder.encode(requesFaculty.getPassword()));

        requesFaculty.setRole(Role.FACULTY);
        Faculty save = frepo.save(requesFaculty);
        return save.getName() + ",\nYou Account is Created Successfully.\nFaculty Id : " + save.getFacultyId() ;
      
    }

    public long getFacultyCount(String domain) {
    return frepo.countByUniversity_Domain(domain);
    }



    // ------ READ ALL faculty for specific university ------
    public List<FacultyResponseDTO> getAllFaculty(String domain) {
        List<Faculty> facultyList = frepo.findByDomain(domain);
        
        return facultyList.stream()
            .map(faculty -> modelMapper.map(faculty, FacultyResponseDTO.class))
            .collect(Collectors.toList());
    }

    // ------ READ ONE by domain + id  ------
    // **** this is for official use only no others  ***** 
    public Faculty getById(String domain, Long id) {
        return frepo.findByIdAndDomain(id, domain);
    }
    
    //  READ ONE by domain + DomainId means (Id which provide by University or collage)
    public Faculty getFacultyByFacultyId(String domain, String facultyId ) {
        return frepo.findByFacultyIdAndDomain(facultyId, domain);
    }


    // Update Password or Forget Password
     public boolean updatePasswordByEmail(String domain, String email, String newPass ) {
        Faculty old = frepo.findByEmailAndDomain(email, domain).orElse(null);
        if (old == null) return false;

        old.setPassword(newPass);
        frepo.save(old);
        return true;

    }

    // ------ UPDATE by id ------
    // **** this is for official use only no others  ***** 
    public Faculty updateFaculty(String domain, Long id, Faculty f) {
        Faculty old = frepo.findByIdAndDomain(id, domain);
        if (old == null) return null;
        
        old.setName(f.getName());
        old.setCourse(f.getCourse());
        old.setTeachingBatch(f.getTeachingBatch());
        old.setMobileNumber(f.getMobileNumber());
        
        return frepo.save(old);
    }

    // ------ UPDATE by  ------
    public Boolean updateFacultyByFacultyEmail(String domain, Faculty newData) {
        Faculty old = frepo.findByEmailAndDomain(newData.getEmail(), domain).orElse(null);
        if (old == null) return false;

        if (newData.getName() != null)
            old.setName(newData.getName());

        if (newData.getCourse() != null)
            old.setCourse(newData.getCourse());

        if (newData.getTeachingBatch() != null)
            old.setTeachingBatch(newData.getTeachingBatch());

        if (newData.getMobileNumber() != null)
            old.setMobileNumber(newData.getMobileNumber());


        frepo.save(old);
        return true;
    }

    
    // ------ DELETE by id  ------
    // **** this is for official use only no others  ***** 
    public String deleteFacultyById(String domain, Long id) {
        Faculty f = frepo.findByIdAndDomain(id, domain);
        if (f == null) return "Invalid faculty ID";
        frepo.delete(f);
        return "Deleted faculty with id " + id;
    }

    // ------ DELETE by Email  ------
    public String deleteFacultyByEmail(String domain, String email) {
        Faculty f = frepo.findByEmailAndDomain(email, domain).orElse(null);
        if (f != null) {
            frepo.delete(f);
            return "Deleted faculty with email id " + email ;
        }
         return "Invalid Please try again ";

    }

    
    // ------ READ ALL student for specific university ------

    public List<StudentResponseDTO> getStudentsByFacultyCourse(String domain, String email) {

        Faculty faculty = frepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));

        String course = faculty.getCourse();

        return studentRepository.findByCourseAndDomain(course, domain)
                .stream()
                .map(student -> modelMapper.map(student, StudentResponseDTO.class))
                .toList();
    }

    // ------ READ ONE by domain + rollNo ------
    public Student getStudentByRollNo(String domain, String rollNo) {
        return studentService.getStudentByRollNo(domain, rollNo);
    }

    // ------ READ ONE by domain + Name ------
    public List<Student> getAllStudentByName(String domain, String name) {
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
