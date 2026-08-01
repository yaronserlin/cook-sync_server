package com.cooksync_server.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtos.request.note.NoteRequestDTO;
import com.dtos.response.note.NoteResponse;
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

    /**
     * A recipe can carry both one general note (instructionId == null) and
     * independent notes per step, so the lookup for an existing row to
     * update must match on instructionId too — matching only on
     * (user, recipe) would let a per-step note silently overwrite the
     * general note (or vice versa).
     */
    @Transactional
    public void saveNote(NoteRequestDTO request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        Recipe recipe = recipeRepository.findById(request.recipeId().toString())
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", request.recipeId().toString()));

        String instructionId = request.instructionId() != null ? request.instructionId().toString() : null;
        Optional<PersonalInstructionNote> existingNote = instructionId == null
                ? noteRepository.findByUserIdAndRecipeIdAndInstructionIdIsNull(user.getId(), request.recipeId().toString())
                : noteRepository.findByUserIdAndRecipeIdAndInstructionId(user.getId(), request.recipeId().toString(), instructionId);

        PersonalInstructionNote note = existingNote.orElse(PersonalInstructionNote.builder()
                .user(user)
                .recipe(recipe)
                .build());

        note.setNote(request.note());
        note.setInstructionId(instructionId);

        noteRepository.save(note);
    }

    /** The recipe-level note only (not pinned to any instruction step). */
    public NoteResponse getNote(String recipeId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        return noteRepository.findByUserIdAndRecipeIdAndInstructionIdIsNull(user.getId(), recipeId)
                .map(PersonalNoteService::toResponse)
                .orElse(null);
    }

    /** All of the caller's notes on this recipe: the general note (if any) plus one per annotated step. */
    public List<NoteResponse> getNotesForRecipe(String recipeId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        return noteRepository.findAllByUserIdAndRecipeId(user.getId(), recipeId).stream()
                .map(PersonalNoteService::toResponse)
                .collect(Collectors.toList());
    }

    private static NoteResponse toResponse(PersonalInstructionNote n) {
        return new NoteResponse(
                n.getId(),
                n.getRecipe().getId(),
                n.getInstruction() != null ? n.getInstruction().getId() : null,
                n.getNote());
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
