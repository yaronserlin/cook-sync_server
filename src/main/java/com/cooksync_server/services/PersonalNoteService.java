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
import com.cooksync_server.exceptions.auth.UnauthorizedActionException;
import com.cooksync_server.repositories.PersonalInstructionNoteRepository;
import com.cooksync_server.repositories.RecipeRepository;
import com.cooksync_server.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service class managing user private notes on recipes and step-by-step instructions.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@Service
@RequiredArgsConstructor
public class PersonalNoteService {

    private final PersonalInstructionNoteRepository noteRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    /**
     * Saves or updates a personal private note for a recipe or specific instruction step.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param request note creation or update request DTO
     * @param userEmail user email address
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

    /**
     * Retrieves general recipe-level personal note (where instruction IS NULL).
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param recipeId target recipe ID
     * @param userEmail user email address
     * @return NoteResponse DTO or null if no note attached
     */
    public NoteResponse getNote(String recipeId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        return noteRepository.findByUserIdAndRecipeIdAndInstructionIdIsNull(user.getId(), recipeId)
                .map(PersonalNoteService::toResponse)
                .orElse(null);
    }

    /**
     * Retrieves all personal notes created by user for a recipe (general + step-specific notes).
     *
     * Complexity:
     * Time: O(N) where N is user note count for recipe
     * Space: O(N)
     *
     * @param recipeId target recipe ID
     * @param userEmail user email address
     * @return list of NoteResponse DTOs
     */
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

    /**
     * Deletes a personal note following author verification.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param noteId target note ID
     * @param userEmail user email address
     */
    @Transactional
    public void deleteNote(String noteId, String userEmail) {
        PersonalInstructionNote note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", noteId));

        if (!note.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedActionException("You are not allowed to delete this note.");
        }

        noteRepository.delete(note);
    }
}
