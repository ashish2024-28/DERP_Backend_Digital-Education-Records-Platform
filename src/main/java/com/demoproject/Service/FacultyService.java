package com.demoproject.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;

import com.demoproject.Entity.*;
import com.demoproject.Repository.StudentRepository;
import org.jspecify.annotations.NonNull;
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
        LocalDateTime lastLogin = facultyLogin.getLastLoginDateTime();

        facultyLogin.setLastLoginDateTime(LocalDateTime.now());
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
    public String addFaculty(@NonNull String domain, @NonNull FacultySignupDTO facultySignupDTO) {

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

    // ------ READ ALL faculty count for specific university ------
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


    // Update Password or Forget Password
    public boolean updatePasswordByEmail(String domain, String email, String newPass ) {
        Faculty old = frepo.findByEmailAndDomain(email, domain).orElse(null);
        if (old == null) return false;

        old.setPassword(newPass);
        frepo.save(old);
        return true;

    }

    // ------ UPDATE Faculty Profile by Email  ------
    public Boolean updateFacultyByFacultyEmail(String domain, Faculty newData) {
        Faculty old = frepo.findByEmailAndDomain(newData.getEmail(), domain).orElse(null);
        if (old == null) return false;

        if (newData.getName() != null)
            old.setName(newData.getName());

        if (newData.getFacultyId() != null)
            old.setFacultyId(newData.getFacultyId());

        if (newData.getMobileNumber() != null)
            old.setMobileNumber(newData.getMobileNumber());

        if (newData.getCourse() != null)
            old.setCourse(newData.getCourse());

        if (newData.getTeachingAssignments() != null)
            old.setTeachingAssignments(newData.getTeachingAssignments());


        frepo.save(old);
        return true;
    }


    // ------ DELETE by Email  ------
    public String deleteFacultyByEmail(String domain, String email) {
        Faculty f = frepo.findByEmailAndDomain(email, domain).orElse(null);
        if (f != null) {
            frepo.delete(f);
            return "Deleted faculty with email id " + email ;
        }
        return "Invalid faculty email";

    }

    // ------ READ ALL faculty for specific university ------
    public List<FacultyResponseDTO> getAllFacultyBySubAdminCourse(String domain, String course) {

        return frepo.findByCourseAndDomain(course, domain)
                .stream()
                .map(faculty -> modelMapper.map(faculty, FacultyResponseDTO.class))
                .toList();
    }

    // ------ READ ALL student by faculty course for specific university  ------

    public List<StudentResponseDTO> getStudentsByFacultyCourse(String domain, String email) {

        Faculty faculty = frepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));

        String course = faculty.getCourse();

        return studentService.getStudentsByCourse(course, domain);
    }

    // ------ READ ALL student by TeachingBatch (studyBatch for student) for specific university  ------
//
//    public List<StudentResponseDTO> getStudentsByFacultyTeachingBatch(String domain, String email) {
//
//        Faculty faculty = frepo.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("Faculty not found"));
//
//        String teachingBatch = faculty.getTeachingBatch();
//
//        return studentService.getStudentsByTeachingBatch(domain , teachingBatch );
//    }



}
