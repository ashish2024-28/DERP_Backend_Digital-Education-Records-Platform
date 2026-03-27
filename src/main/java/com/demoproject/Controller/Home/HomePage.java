package com.demoproject.Controller.Home;

import java.io.IOException;
import java.util.List;

import com.demoproject.DTO.University.UniversityNameDomainLogoPathDTO;
import com.demoproject.Entity.DomainAdmin;
import com.demoproject.Entity.University;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.demoproject.DTO.ApiResponse;
import com.demoproject.DTO.University.UniversityDomainAdminDTO;
import com.demoproject.Service.UniversityService;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/home_page")
public class HomePage {
    
    @Autowired
    private UniversityService universityService;


    @GetMapping
    public List<UniversityNameDomainLogoPathDTO> greet(){

        return universityService.getAllUniversityNameDomainLogo() ;
    }

//
    /**
     * Step 1: Validate all fields BEFORE sending OTPs.
     * Frontend calls this first. If any duplicate exists → error returned.
     * If all clear → frontend proceeds to send OTPs.
     */
    @PostMapping("/validate_before_otp")
    public ResponseEntity<ApiResponse<?>> validateBeforeOtp(
            @RequestPart("university") University university,
            @RequestPart("domainAdmin") DomainAdmin domainAdmin
    ) {
        try {
            universityService.validateUniversityAndAdmin(university, domainAdmin);
            return ResponseEntity.ok(new ApiResponse<>(true, "Validation passed", null));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Step 2: Final registration — called only after both OTPs are verified.
     */
    @PostMapping("/register_university")
    public ResponseEntity<ApiResponse<?>> add(

            @RequestPart("university") University university,
            @RequestPart("domainAdmin") DomainAdmin domainAdmin,
            @RequestPart(value = "logo",required = false) MultipartFile logo

    ) throws IOException {

        String response = universityService.registerUniversityWithDomainAdmin(
                university,
                domainAdmin,
                logo
        );

        return new ResponseEntity<>(
                new ApiResponse<>(true, response, null),
                HttpStatus.CREATED
        );
    }


//    // CREATE
//        @PostMapping("/register_university")
//            public ResponseEntity<ApiResponse<?>> add(@RequestBody UniversityDomainAdminDTO requestDTO){
//
//            String response = universityService.registerUniversityWithDomainAdmin(
//                requestDTO.getUniversity(),
//                requestDTO.getDomainAdmin()
//            );
//            return new ResponseEntity<>(new ApiResponse<>(true,response,null),HttpStatus.CREATED);
//    }

}
