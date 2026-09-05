package com.demoproject.Controller.Common.GetAllRole;

import com.demoproject.DTO.FacultyDTO.FacultyResponseDTO;
import com.demoproject.Service.FacultyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/{domain}/{role}")
@PreAuthorize("""
        hasAnyRole("ADMIN","DOMAIN_ADMIN","SUB_ADMIN","FACULTY") and
        #domain.equalsIgnoreCase(authentication.principal.domain)
        """)
public class GetAllFaculty {

    @Autowired
    private FacultyService facultyService;

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
