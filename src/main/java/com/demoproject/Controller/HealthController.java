package com.demoproject.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demoproject.DTO.ApiResponse;

@RestController("/")
public class HealthController {

    @GetMapping
    public ResponseEntity<ApiResponse<String>> healthCheck() {

        ApiResponse<String> response =
                new ApiResponse<>(true, "Spring Boot Backend Running Successfully", "OK");

        return ResponseEntity.ok(response);
    }
}