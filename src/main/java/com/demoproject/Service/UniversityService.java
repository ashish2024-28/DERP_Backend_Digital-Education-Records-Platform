package com.demoproject.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.demoproject.Repository.DomainAdminRepository;
import com.demoproject.Repository.UniversityRepo;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.demoproject.Entity.Role;
import com.demoproject.Entity.University;
import com.demoproject.DTO.University.UniversityNameDomainLogoPathDTO;
import com.demoproject.Entity.DomainAdmin;

import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
public class UniversityService {


    @Autowired
    private UniversityRepo universityRepo;
    @Autowired
    private DomainAdminRepository dAdminRepo;
    @Autowired
    private BaseUserService baseUserService;
    @Autowired
    @Qualifier("bcryptEncoder")
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ModelMapper modelMapper;
    private final String UPLOAD_DIR = "uploads/universityLogo/";


    public List<UniversityNameDomainLogoPathDTO> getAllUniversityNameDomainLogo(){
        List<University> universities = universityRepo.findAll();
        return universities.stream()
                .map(university -> modelMapper.map(university, UniversityNameDomainLogoPathDTO.class))
                .collect(Collectors.toList());
    }

//
    /**
     * Pure validation — no side effects, no DB writes.
     * Called by /validate_before_otp BEFORE OTPs are sent.
     * Throws RuntimeException with a user-friendly message if any duplicate is found.
     */
    public void validateUniversityAndAdmin(University university, DomainAdmin domainAdmin) {

        // University checks
        if (universityRepo.existsByPermanentId(university.getPermanentId())) {
            throw new RuntimeException("Permanent ID already exists. Enter the correct Permanent ID.");
        }
        if (universityRepo.existsByDomain(university.getDomain())) {
            throw new RuntimeException("University domain already exists. Enter a unique domain.");
        }
        if (baseUserService.existsUserByEmail(university.getEmail())) {
            throw new RuntimeException("University email already exists.");
        }
        if (universityRepo.existsByMobileNumber(university.getMobileNumber())) {
            throw new RuntimeException("University mobile number already exists.");
        }

        // DomainAdmin checks
        if (baseUserService.existsUserByEmail(domainAdmin.getEmail())) {
            throw new RuntimeException("Domain Admin email already exists.");
        }
        if (dAdminRepo.existsByMobileNumber(domainAdmin.getMobileNumber())) {
            throw new RuntimeException("Domain Admin mobile number already exists.");
        }
    }

    /**
     * Called AFTER both OTPs are verified.
     * Runs validation again (safety net) then persists everything.
     */
    @Transactional
    public String registerUniversityWithDomainAdmin(University university, DomainAdmin domainAdmin, MultipartFile logo) throws IOException {

        // Re-run validation as a safety net (race-condition protection)
        validateUniversityAndAdmin(university, domainAdmin);

        Files.createDirectories(Paths.get(UPLOAD_DIR));

        if (logo != null && !logo.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + logo.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.write(filePath, logo.getBytes());
            university.setUniversityLogoPath("uploads/universityLogo/" + fileName);
        }

        // Encode password
        domainAdmin.setPassword(passwordEncoder.encode(domainAdmin.getPassword()));

        // Set relationships (both sides)
        domainAdmin.setUniversity(university);
        domainAdmin.setRole(Role.DOMAIN_ADMIN);
        domainAdmin.setDomain(university.getDomain());
        university.setDomainAdmin(domainAdmin);

        University saved = universityRepo.save(university);

        return "University created with ID: " + saved.getId()
                + "\nDomain: " + saved.getDomain()
                + "\nDomainAdmin ID: " + saved.getDomainAdmin().getId();
    }



//    @Transactional
//    public String registerUniversityWithDomainAdmin(University university, DomainAdmin domainAdmin, MultipartFile logo) throws IOException {
//
//    // University validations
//        if (universityRepo.existsByDomain(university.getDomain())) {
//            throw new RuntimeException("University domain already exists. Enter unique domain.");
//        }
//
//        if (universityRepo.existsByPermanentId(university.getPermanentId())) {
//            throw new RuntimeException("Permanent ID already exists. Enter correct Permanent ID.");
//        }
//
//        if (universityRepo.existsByEmail(university.getEmail())) {
//            throw new RuntimeException("University email already exists.");
//        }
//
//        if (universityRepo.existsByMobileNumber(university.getMobileNumber())) {
//            throw new RuntimeException("University mobile number already exists.");
//        }
//
//        // DomainAdmin validations
//        boolean emailExist = baseUserService.existsUserByEmail(domainAdmin.getEmail());
//        if(emailExist){
//            throw new RuntimeException("Domain Admin email already exists.");
//        }
//
//        if (dAdminRepo.existsByMobileNumber(domainAdmin.getMobileNumber())) {
//            throw new RuntimeException("Domain Admin mobile number already exists.");
//        }
//
//
//        Files.createDirectories(Paths.get(UPLOAD_DIR));
//
//        String filePathString = null;
//
//        if (logo != null && !logo.isEmpty()) {
//            String fileName = System.currentTimeMillis() + "_" + logo.getOriginalFilename();
//            Path filePath = Paths.get(UPLOAD_DIR, fileName);
//            Files.write(filePath, logo.getBytes());
//            filePathString = "uploads/universityLogo/" + fileName;
//            // ⭐ IMPORTANT
//            university.setUniversityLogoPath(filePathString);
//        }
//
//
//        // Encode password
//        domainAdmin.setPassword(passwordEncoder.encode(domainAdmin.getPassword()));
//
//        // set relationship (BOTH SIDES)
//        domainAdmin.setUniversity(university);
//        domainAdmin.setRole(Role.DOMAIN_ADMIN);
//        domainAdmin.setDomain(university.getDomain());
//        university.setDomainAdmin(domainAdmin);
//
//        University saved = universityRepo.save(university);
//
//        return "University created with ID: " + saved.getId() + "\ndomain : " + saved.getDomain() + "\nDomainAdmin ID: " + saved.getDomainAdmin().getId();
//    }


    public UniversityNameDomainLogoPathDTO getUniversityName_Logo(String domain) throws Exception {

        University university = universityRepo.findByDomain(domain);
        if(university == null ){
            throw new RuntimeException("Invalid domain");
        }

        String name = Optional.ofNullable(university.getUniversityName())
                .filter(n -> !n.isBlank())
                .orElse(university.getInstitutionName());
        return new UniversityNameDomainLogoPathDTO(name, university.getDomain(),university.getUniversityLogoPath());


        
    }






// **** these all are for official use only no others  ***** 
    // READ ALL
    public List<University> getAll(){
        return universityRepo.findAll();
    }

    // Get By Id  // This will match /123 (numeric id)
    public University getById(Long id){
        return universityRepo.findById(id).orElse(null);
    }

    // Get By Domain   This will match /hu, /dtu, /aku etc.
    public University getByDomain(String domain) {
        return universityRepo.findByDomain(domain);
    }
    
    // UPDATE id and domain
    public University updateUniversity(String domain, Long id, University univ) {
        University old = universityRepo.findByDomainAndId(domain, id)
                .orElseThrow(() -> new RuntimeException("University not found for domain=" + domain + " id=" + id));
        old.setUniversityName(univ.getUniversityName());
        return universityRepo.save(old);
    }
    
    // delete by id and domain
    public String deleteUniversity(Long id, String domain){
        universityRepo.deleteByIdAndDomain(id,domain);
        return "University deleted successfully with id " + id + "And domain " + domain;
    }


    
}
