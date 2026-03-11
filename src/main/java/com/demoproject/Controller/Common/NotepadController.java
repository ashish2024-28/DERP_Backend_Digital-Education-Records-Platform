package com.demoproject.Controller.Common;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.demoproject.DTO.ApiResponse;
import com.demoproject.Service.ProfileInformation.Common.NotepadService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/{domain}/{role}/notepad")

@RequiredArgsConstructor
public class NotepadController {

    private final NotepadService noteService;

    @GetMapping
    public ResponseEntity<?> getMyNotes(@PathVariable String domain, @PathVariable String role, Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(
                new ApiResponse<>(true, null,
                        noteService.getMyNotes(role, domain, email))
        );
    }

    @PostMapping("/add")
    public ResponseEntity<?> addNote(@PathVariable String domain, @PathVariable String role,
            Authentication authentication,
            @RequestParam String title,
            @RequestParam String noteText,
            @RequestParam(required = false) MultipartFile file) throws IOException {
        // 👉 It returns email stored inside JWT.
        String email = authentication.getName();
        return ResponseEntity.ok(
                new ApiResponse<>(true, null,
                        noteService.addNote(role, domain, email, title, noteText, file))
        );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable String domain, @PathVariable String role,
                                     Authentication authentication,
                                     @PathVariable Long id ) throws IOException {
        // 👉 It returns email stored inside JWT.
        String email = authentication.getName();
        noteService.deleteNote(email, id);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Note deleted successfully", null)
        );
    }


}



// 🧠 What is Authentication authentication?
// Spring Security automatically gives you the logged-in user.
// After login:
// JWT token is created
// Every request sends token
// Spring verifies token
// Spring puts user info inside Authentication
// So:
// authentication.getName();
// 👉 Returns logged-in user's email.
// Why use this?
// ❌ We do NOT trust frontend to send email
// ✅ We trust JWT token
// More secure.