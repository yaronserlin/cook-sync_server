package com.cooksync_server.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dtos.request.note.NoteRequestDTO;
import com.dtos.response.ApiResponse;
import com.dtos.response.note.NoteResponse;
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

    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<ApiResponse<NoteResponse>> getNote(
            @PathVariable String recipeId,
            Authentication authentication) {
        NoteResponse note = noteService.getNote(recipeId, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, note, null, "OK"));
    }

    /** All of the caller's notes on a recipe: the general note plus any per-step notes. */
    @GetMapping("/recipe/{recipeId}/all")
    public ResponseEntity<ApiResponse<List<NoteResponse>>> getNotesForRecipe(
            @PathVariable String recipeId,
            Authentication authentication) {
        List<NoteResponse> notes = noteService.getNotesForRecipe(recipeId, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, notes, null, "OK"));
    }
}
