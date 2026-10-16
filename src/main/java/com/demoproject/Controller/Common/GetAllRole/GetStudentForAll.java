package com.demoproject.Controller.Common.GetAllRole;


import com.demoproject.DTO.StudentDTO.StudentResponseDTO;
import com.demoproject.DTO.StudentDTO.StudentResponseWithProfileDTO;
import com.demoproject.Service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/{domain}/{role}")
@PreAuthorize("""
        hasAnyRole("ADMIN","DOMAIN_ADMIN","SUB_ADMIN","FACULTY","FEES_ADMIN") and
        #domain.equalsIgnoreCase(authentication.principal.domain)
        """)
public class GetStudentForAll {
    @Autowired
    private StudentService studentService;


    @GetMapping("/student")
    public StudentResponseWithProfileDTO getAllStudents(@PathVariable String domain , @RequestParam String email  , Authentication authentication ) {

        Set<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet());
        return studentService.getStudentsWithProfile(domain, email, roles );
    }

    // ------ READ ALL student for specific university ------
    @GetMapping("/all_student")
    public List<StudentResponseDTO> getAllStudents(
            @PathVariable String domain,
            Authentication authentication) {

        Set<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet());

        return studentService.getAllStudent(domain, roles);
    }

}
