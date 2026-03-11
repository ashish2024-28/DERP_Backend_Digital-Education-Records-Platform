package com.demoproject.Controller.Home;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demoproject.DTO.ApiResponse;
import com.demoproject.DTO.LoginRequestDTO;
import com.demoproject.DTO.LoginResponseDTO;
import com.demoproject.DTO.University.UniversityNameDomainLogoPathDTO;
import com.demoproject.Service.BaseUserService;

import com.demoproject.Service.UniversityService;

@RestController
@RequestMapping("/{domain}/login_profile")
public class Login {
    
    
    @Autowired
    private UniversityService universityService;
    @Autowired
    private BaseUserService baseUserService;



    @GetMapping
    public ResponseEntity<?> getUniversityNameLogoPath(@PathVariable String domain){
        try{
            UniversityNameDomainLogoPathDTO dto = universityService.getUniversityName_Logo(domain);
            return ResponseEntity.ok( dto );

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }


    // process of jwt start(Security.java) step(2.) go to userLogin method -> BaseUserService

    // any user(DomainAdmin,SubAdmin,Faculty,Student) Login by domain + Email + Password

    // working first need email(username) & password and call userLogin method -> BaseUserService
    // @GetMapping("/user_login")
    @PostMapping("/user_login")
    public ResponseEntity<?> userLogin(@PathVariable String domain,
         @RequestBody LoginRequestDTO loginRequest){

            loginRequest.setDomain(domain);
            LoginResponseDTO response = baseUserService.userLogin(loginRequest);
            return ResponseEntity.ok(new ApiResponse<>(true, "Login Successful", response));
    }

}
