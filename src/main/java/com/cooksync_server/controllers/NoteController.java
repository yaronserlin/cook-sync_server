package com.cooksync_server.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.cooksync_server.dtos.request.note.NoteRequestDTO;
import com.cooksync_server.dtos.response.ApiResponse;
import com.cooksync_server.services.PersonalNoteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final PersonalNoteService noteService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> saveNote(
            @Valid @RequestBody NoteRequestDTO request, 
            Authentication authentication) {
        noteService.saveNote(request, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Note saved successfully"));
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @PathVariable String noteId, 
            Authentication authentication) {
        noteService.deleteNote(noteId, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Note deleted successfully"));
    }
}