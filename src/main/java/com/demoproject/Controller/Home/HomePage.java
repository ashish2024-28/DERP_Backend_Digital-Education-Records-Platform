package com.demoproject.Controller.Home;

import java.util.List;

import com.demoproject.DTO.University.UniversityNameDomainLogoPathDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demoproject.DTO.ApiResponse;
import com.demoproject.DTO.University.UniversityDomainAdminDTO;
import com.demoproject.Service.UniversityService;



@RestController
@RequestMapping("/home_page")
public class HomePage {
    
    @Autowired
    private UniversityService universityService;


    @GetMapping
    public List<UniversityNameDomainLogoPathDTO> greet(){

        return universityService.getAllUniversityNameDomainLogo() ;
    }

    // CREATE
        @PostMapping("/register_university")
            public ResponseEntity<ApiResponse<?>> add(@RequestBody UniversityDomainAdminDTO requestDTO){

            String response = universityService.registerUniversityWithDomainAdmin(
                requestDTO.getUniversity(),
                requestDTO.getDomainAdmin()
            );
            return new ResponseEntity<>(new ApiResponse<>(true,response,null),HttpStatus.CREATED);
    }

}
