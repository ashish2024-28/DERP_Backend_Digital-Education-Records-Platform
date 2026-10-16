package com.demoproject.Controller.Common.GetAllRole;

import com.demoproject.DTO.FacultyDTO.FacultyResponseDTO;
import com.demoproject.DTO.StudentDTO.StudentResponseWithProfileDTO;
import com.demoproject.Service.FacultyService;
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
        hasAnyRole("ADMIN","DOMAIN_ADMIN","SUB_ADMIN","FACULTY") and
        #domain.equalsIgnoreCase(authentication.principal.domain)
        """)
public class GetAllFaculty {

    @Autowired
    private FacultyService facultyService;

    @GetMapping("/faculty")
    public FacultyResponseDTO getAllFaculty(@PathVariable String domain , @RequestParam String email  , Authentication authentication ) {

        Set<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet());
        return facultyService.getFacultyWithProfile(domain, email, roles );
    }

    // ------ READ ALL faculty for specific university ------
    @GetMapping("/allFaculty")
    public List<FacultyResponseDTO> getAllFaculty(
            @PathVariable String domain,
            Authentication authentication ) {

        String role = authentication.getAuthorities().toString();

        List<FacultyResponseDTO> faculty = facultyService.getAllFaculty(domain);

        return faculty;
    }




}
