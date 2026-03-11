package com.demoproject.Service.ProfileInformation.Common;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.demoproject.Entity.BaseUser;
import com.demoproject.Entity.ProfileInformation.Common.Notepad;
import com.demoproject.Repository.ProfileInformation.Common.NotepadRepository;
import com.demoproject.Service.BaseUserService;

@Service
@RequiredArgsConstructor
public class NotepadService {

    private final NotepadRepository noteRepository;
    private final BaseUserService baseUserService;

    private final String UPLOAD_DIR = "uploads/notes/";


    public Notepad addNote(String role, String domain, String email, String title, String noteText, MultipartFile file) throws IOException {

        BaseUser user = baseUserService.findUserByDomainAndEmail(domain, email);

        Files.createDirectories(Paths.get(UPLOAD_DIR));

        String filePathString = null;

        if (file != null && !file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.write(filePath, file.getBytes());
            filePathString = "uploads/notes/" + fileName;
        }

        Notepad note = new Notepad();
        note.setTitle(title);
        note.setNoteText(noteText);
        note.setAttachmentPath(filePathString);

        note.setOwnerEmailId(user.getEmail());
        note.setOwnerRole(user.getRole());
        return noteRepository.save(note);
    }


    public List<Notepad> getMyNotes(String role, String domain, String email ) {
        BaseUser user = baseUserService.findUserByDomainAndEmail(domain, email);

        return noteRepository.findByOwnerEmailId( user.getEmail() );
    }

    public void deleteNote(String email, Long id) {

        Notepad note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (!note.getOwnerEmailId().equals(email)) {
            throw new RuntimeException("You are not allowed to delete this note");
        }

        noteRepository.delete(note);
    }



}
