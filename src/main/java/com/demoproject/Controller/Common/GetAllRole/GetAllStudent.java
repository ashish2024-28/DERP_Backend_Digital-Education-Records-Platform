package com.demoproject.Controller.Common.GetAllRole;


import com.demoproject.DTO.StudentDTO.StudentResponseDTO;
import com.demoproject.Service.StudentService;
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
public class GetAllStudent {
    @Autowired
    private StudentService studentService;


    // ------ READ ALL student for specific university ------
    @GetMapping("/allStudent")
    public List<StudentResponseDTO> getAllStudents(
            @PathVariable String domain,
            Authentication authentication ) {

        String role = authentication.getAuthorities().toString();

        List<StudentResponseDTO> students = studentService.getAllStudent(domain);

        if(!role.contains("DOMAIN_ADMIN")){
            students.forEach(s -> s.setPassword(null));
        }

        return students;
    }

}
