package com.cooksync_server.services;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtos.request.note.NoteRequestDTO;
import com.cooksync_server.entities.PersonalInstructionNote;
import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.User;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.repositories.PersonalInstructionNoteRepository;
import com.cooksync_server.repositories.RecipeRepository;
import com.cooksync_server.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PersonalNoteService {

    private final PersonalInstructionNoteRepository noteRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    @Transactional
    public void saveNote(NoteRequestDTO request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        Recipe recipe = recipeRepository.findById(request.recipeId().toString())
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", request.recipeId().toString()));

        // Check if note exists for this user/recipe/instruction combination
        Optional<PersonalInstructionNote> existingNote = noteRepository.findByUserIdAndRecipeId(user.getId(), request.recipeId().toString());

        PersonalInstructionNote note = existingNote.orElse(PersonalInstructionNote.builder()
                .user(user)
                .recipe(recipe)
                .build());

        note.setNote(request.note());
        note.setInstructionId(request.instructionId() != null ? request.instructionId().toString() : null);

        noteRepository.save(note);
    }

    @Transactional
    public void deleteNote(String noteId, String userEmail) {
        PersonalInstructionNote note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", noteId));

        if (!note.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized: Cannot delete other users' notes");
        }

        noteRepository.delete(note);
    }
}
