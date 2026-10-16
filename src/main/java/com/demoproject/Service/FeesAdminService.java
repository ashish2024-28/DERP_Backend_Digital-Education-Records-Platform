package com.demoproject.Service;


import com.demoproject.DTO.FacultyDTO.FacultyResponseDTO;
import com.demoproject.DTO.FeesAdminDto.FeesAdminResponseDTO;
import com.demoproject.DTO.FeesAdminDto.FeesAdminSignupDTO;
import com.demoproject.DTO.LoginRequestDTO;
import com.demoproject.DTO.SubAdminDTO.SubAdminResponseDTO;
import com.demoproject.Entity.*;
import com.demoproject.Repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class FeesAdminService {

    @Autowired
    private UniversityRepo universityRepo;
    @Autowired
    private FeesAdminRepository feesAdminRepository;
    @Autowired
    private BaseUserService baseUserService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    @Qualifier("bcryptEncoder")
    private PasswordEncoder passwordEncoder ;

    

    //  Login by domain + email + Password
    public FeesAdmin LoginFessAdmin(LoginRequestDTO loginRequestDTO){
        FeesAdmin feesAdminLogin = feesAdminRepository.findByEmailAndDomain(loginRequestDTO.getEmail(), loginRequestDTO.getDomain()).orElse(null);
        if (feesAdminLogin == null) {
            return null;
        }
        boolean passwordMatch = passwordEncoder.matches(loginRequestDTO.getPassword() ,feesAdminLogin.getPassword());

        if (passwordMatch) {
            feesAdminLogin.setLastLoginDateTime(LocalDateTime.now());
            return feesAdminRepository.save(feesAdminLogin);
            
        } else {     return null;    }
    }


    // login when frontend send jwt token
    public FeesAdminResponseDTO getFeesAdminByEmailAndDomain(String email, String domain) {
        FeesAdmin feesAdminLogin = feesAdminRepository.findByEmailAndDomain(email,domain).orElseThrow();

        // set lastLoginDateTime
        LocalDateTime lastLogin = feesAdminLogin.getLastLoginDateTime();

        feesAdminLogin.setLastLoginDateTime(LocalDateTime.now());
        feesAdminLogin =  feesAdminRepository.save(feesAdminLogin);

        feesAdminLogin.setLastLoginDateTime(lastLogin);

        FeesAdminResponseDTO responseDTO =  modelMapper.map(feesAdminLogin, FeesAdminResponseDTO.class) ;
        responseDTO.setLastLoginDateTime(lastLogin);

        return responseDTO;
    }




    // CREATE
    public String addFeesAdmin(String domain, FeesAdminSignupDTO signupDTO){

        if(baseUserService.existsUserByEmail(signupDTO.getEmail())){
            throw new RuntimeException("User already exists with this email.");
        }

        FeesAdmin requestFessAdmin = modelMapper.map(signupDTO, FeesAdmin.class);

        University university = universityRepo.findByDomain(domain)
            .orElseThrow(() -> new RuntimeException("University not found"));
        requestFessAdmin.setDomain(domain);
        requestFessAdmin.setUniversity(university);
       
        if( feesAdminRepository.existsByFeesAdminIdAndDomain(requestFessAdmin.getFeesAdminId(),requestFessAdmin.getDomain())){ throw new RuntimeException("Fees Admin ID already exists.");  }
        if( feesAdminRepository.existsByDomainAndEmail(requestFessAdmin.getDomain(),requestFessAdmin.getEmail())){ throw new RuntimeException("Fees Admin's field Email are already exist for this university. ");  }
        if( feesAdminRepository.existsByEmail(requestFessAdmin.getEmail())){ throw new RuntimeException("Enter Unique Email Id. ");  }

        // for security use passwordEncoder
        requestFessAdmin.setPassword(passwordEncoder.encode(requestFessAdmin.getPassword()));
        requestFessAdmin.setRole(Role.FEES_ADMIN);
        FeesAdmin save = feesAdminRepository.save(requestFessAdmin);
        return save.getName() + ",\nYour Account is Created Successfully.\nFees Admin Id : " + save.getFeesAdminId() ;
            
    }

    // ------ READ ALL FeesAdmin count for specific university ------
    public long getFeesAdminCount(String domain) {
        return feesAdminRepository.countByUniversity_Domain(domain);
    }


    // ------ READ ALL FeesAdmin for specific university ------
    public List<FeesAdminResponseDTO> getAllFeesAdmin(String domain) {

        List<FeesAdmin> feesAdminList =
                feesAdminRepository.findByDomain(domain);

        return feesAdminList.stream()
                .map(feesAdmin -> {

                    FeesAdminResponseDTO dto =
                            modelMapper.map(
                                    feesAdmin,
                                    FeesAdminResponseDTO.class
                            );

                    // Set university name explicitly
                    if (feesAdmin.getUniversity() != null) {
                        dto.setUniversityName(
                                feesAdmin.getUniversity().getUniversityName()
                        );
                    } else {
                        dto.setUniversityName("Unknown University");
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }




    // Update Password or Forget Password
     public boolean updatePasswordByEmail(String domain, String email, String newPass ) {
        FeesAdmin old = feesAdminRepository.findByEmailAndDomain(email, domain).orElse(null);
        if (old == null) return false;

        old.setPassword(passwordEncoder.encode(newPass));
         feesAdminRepository.save(old);
        return true;

    }

    // ------ UPDATE FeesAdmin Profile by Email  ------
    public FeesAdminResponseDTO updateFeesAdminByEmail(String domain, String email, FeesAdmin newData){
        FeesAdmin old = feesAdminRepository.findByEmailAndDomain(email, domain).orElse(null);
        if (old == null) {
            throw new IllegalArgumentException("Fees Admin not found.");
        }

        if (newData.getName() != null)
            old.setName(newData.getName());

        if (newData.getMobileNumber() != null)
            old.setMobileNumber(newData.getMobileNumber());

        FeesAdmin saved = feesAdminRepository.save(old);
        FeesAdminResponseDTO response = modelMapper.map(saved, FeesAdminResponseDTO.class);
        if (saved.getUniversity() != null) {
            response.setUniversityName(saved.getUniversity().getUniversityName());
        }
        return response;
    }

    public FeesAdminResponseDTO updateFeesAdminByEmail(String domain, FeesAdmin newData) {
        if (newData.getEmail() == null || newData.getEmail().isBlank()) {
            return null;
        }
        FeesAdmin old = feesAdminRepository.findByEmailAndDomain(newData.getEmail(), domain).orElse(null);
        if (old == null) {
            return null;
        }

        if (newData.getName() != null) {
            old.setName(newData.getName());
        }
        if (newData.getMobileNumber() != null) {
            old.setMobileNumber(newData.getMobileNumber());
        }

        FeesAdmin saved = feesAdminRepository.save(old);
        FeesAdminResponseDTO response = modelMapper.map(saved, FeesAdminResponseDTO.class);
        if (saved.getUniversity() != null) {
            response.setUniversityName(saved.getUniversity().getUniversityName());
        }
        return response;
    }


    // DELETE
    public String deleteFeesAdminByEmail(String domain, String email){
        FeesAdmin feesAdmin= feesAdminRepository.findByDomainAndEmail(domain, email);
        if (feesAdmin == null ) return "Not found";

        feesAdminRepository.delete(feesAdmin);
        return "Deleted FeesAdmin with email id : " + email;
    }



    // Get subAdmin with profile
    @Transactional(readOnly = true)
    public FeesAdminResponseDTO getFeesWithProfile(String domain , String email , Set<String> roles) {
        FeesAdmin feesAdmin = feesAdminRepository.findByEmailIgnoreCaseAndDomainIgnoreCase(email, domain)
                .orElseThrow(() -> new IllegalArgumentException("Fees Admin profile not found."));
        FeesAdminResponseDTO feesAdminResponseDTO = new FeesAdminResponseDTO();
        feesAdminResponseDTO.setName(feesAdmin.getName());
        feesAdminResponseDTO.setEmail(feesAdmin.getEmail());
        feesAdminResponseDTO.setMobileNumber(feesAdmin.getMobileNumber());
        feesAdminResponseDTO.setProfilePic(feesAdmin.getProfilePic());
        feesAdminResponseDTO.setRole(feesAdmin.getRole() == null ? null : feesAdmin.getRole().name());
        feesAdminResponseDTO.setCreatedDateTime(feesAdmin.getCreatedDateTime());
        feesAdminResponseDTO.setLastUpdateDateTime(feesAdmin.getLastUpdateDateTime());
        feesAdminResponseDTO.setLastLoginDateTime(feesAdmin.getLastLoginDateTime());
        feesAdminResponseDTO.setFeesAdminId(feesAdmin.getFeesAdminId());
        if (feesAdmin.getUniversity() != null) {
            feesAdminResponseDTO.setUniversityName(feesAdmin.getUniversity().getUniversityName());
        }

        // DOMAIN_ADMIN -> everything
        if (roles.contains("ROLE_DOMAIN_ADMIN")) {
            return feesAdminResponseDTO;
        }

        // FEES_ADMIN -> only fees related fields
        if (roles.contains("ROLE_FEES_ADMIN")) {


            // Certificate hide
//            student.setCertifications(null);

            // Attendance hide
//            student.setStudentAttendanceRecordDayWises(null);
//            student.setStudentTotalAttendanceSubjectWise(null);

            // Fees remain available
            // student.getFeesPayments()
            // student.getFeesAccount()

            return feesAdminResponseDTO;
        }

        // FACULTY / SUB_ADMIN -> fees fields hide
        if (roles.contains("ROLE_FACULTY")
                || roles.contains("ROLE_SUB_ADMIN")) {


//            // Fees hide
//            student.setFeesPayments(null);
//            student.setFeesAccount(null);

            return feesAdminResponseDTO;
        }
//
//        // Any other role -> hide sensitive fields
//
//        student.setCertifications(null);
//
//        student.setFeesPayments(null);
//        student.setFeesAccount(null);
//
//        student.setStudentAttendanceRecordDayWises(null);
//        student.setStudentTotalAttendanceSubjectWise(null);

        return feesAdminResponseDTO;
    }


}
