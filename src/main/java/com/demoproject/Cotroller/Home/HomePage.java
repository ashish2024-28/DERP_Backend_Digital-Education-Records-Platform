package com.demoproject.Cotroller.Home;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.demoproject.DTO.University.UniversityDomainAdminDTO;
import com.demoproject.Entity.DomainAdmin;
import com.demoproject.Entity.University;
import com.demoproject.Service.UniversityService;



@RestController
@RequestMapping("/home_page")
public class HomePage {
    
    @Autowired
    private UniversityService universityService;


    @GetMapping
    public String greet(){
        return "welcome to my digitalEducationRecord Platform \nAdd Universitiy and DomanAdmin ";
    }

    // CREATE
        @PostMapping("/register_university")
            public ResponseEntity<?> add(@RequestBody UniversityDomainAdminDTO requestDTO){
        try {

            String response = universityService.registerUniversityWithDomainAdmin(
                requestDTO.getUniversity(),
                requestDTO.getDomainAdmin()
            );
            // return new ResponseEntity<>(response,HttpStatus.CREATED);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "success", true,
                        "message", response
                ));
            
        } catch (Exception e) {
        //     return new ResponseEntity<>(e.getMessage() ,HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "success", false,
                        "message", e.getMessage()
                ));
        }

    }
    





    
    // @PostMapping(value = "/register_university", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // public ResponseEntity<?> register(
    //         @RequestPart("university") University university,
    //         @RequestPart("domainAdmin") DomainAdmin domainAdmin,
    //         @RequestPart("logo") MultipartFile logoFile) {

    //     String response = universityService.registerUniversityWithDomainAdmin(
    //             university,
    //             domainAdmin,
    //             logoFile
    //     );

    //     return new ResponseEntity<>(response, HttpStatus.CREATED);
    // }



}
