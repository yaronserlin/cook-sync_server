package com.cooksync_server.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cooksync_server.entities.Instruction;
import com.cooksync_server.entities.PersonalInstructionNote;
import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.User;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.repositories.PersonalInstructionNoteRepository;
import com.cooksync_server.repositories.RecipeRepository;
import com.cooksync_server.repositories.UserRepository;
import com.dtos.request.note.NoteRequestDTO;
import com.dtos.response.note.NoteResponse;

/**
 * Unit test for PersonalNoteService verifying creation, retrieval, and deletion of general and step-specific personal notes.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@ExtendWith(MockitoExtension.class)
class PersonalNoteServiceTest {

    @Mock
    private PersonalInstructionNoteRepository noteRepository;
    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private UserRepository userRepository;

    private PersonalNoteService service;

    private final String userId = "user-1";
    private final String userEmail = "ada@example.com";
    private final UUID recipeUuid = UUID.randomUUID();
    private final String recipeId = recipeUuid.toString();

    private User user;
    private Recipe recipe;

    /**
     * Initializes test fixtures and service instance before each test.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @BeforeEach
    void setUp() {
        service = new PersonalNoteService(noteRepository, recipeRepository, userRepository);
        user = User.builder().id(userId).email(userEmail).firstName("Ada").lastName("Lovelace").build();
        recipe = Recipe.builder().id(recipeId).title("Pasta").build();

        lenient().when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        lenient().when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
    }

    /**
     * Verifies that saving a general note searches for null instructionId.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void savingGeneralNote_looksUpByInstructionIdIsNull() {
        when(noteRepository.findByUserIdAndRecipeIdAndInstructionIdIsNull(userId, recipeId)).thenReturn(Optional.empty());

        service.saveNote(new NoteRequestDTO(recipeUuid, null, "Use less salt"), userEmail);

        verify(noteRepository).findByUserIdAndRecipeIdAndInstructionIdIsNull(userId, recipeId);
        verify(noteRepository, never()).findByUserIdAndRecipeIdAndInstructionId(any(), any(), any());

        ArgumentCaptor<PersonalInstructionNote> captor = ArgumentCaptor.forClass(PersonalInstructionNote.class);
        verify(noteRepository).save(captor.capture());
        assertThat(captor.getValue().getNote()).isEqualTo("Use less salt");
        assertThat(captor.getValue().getInstruction()).isNull();
    }

    /**
     * Verifies that saving a step note searches by instructionId specifically.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void savingStepNote_looksUpByInstructionId_notGeneralLookup() {
        UUID instructionUuid = UUID.randomUUID();
        when(noteRepository.findByUserIdAndRecipeIdAndInstructionId(userId, recipeId, instructionUuid.toString()))
                .thenReturn(Optional.empty());

        service.saveNote(new NoteRequestDTO(recipeUuid, instructionUuid, "Cook 2 extra minutes"), userEmail);

        verify(noteRepository).findByUserIdAndRecipeIdAndInstructionId(userId, recipeId, instructionUuid.toString());
        verify(noteRepository, never()).findByUserIdAndRecipeIdAndInstructionIdIsNull(any(), any());

        ArgumentCaptor<PersonalInstructionNote> captor = ArgumentCaptor.forClass(PersonalInstructionNote.class);
        verify(noteRepository).save(captor.capture());
        assertThat(captor.getValue().getNote()).isEqualTo("Cook 2 extra minutes");
        assertThat(captor.getValue().getInstruction()).isNotNull();
        assertThat(captor.getValue().getInstruction().getId()).isEqualTo(instructionUuid.toString());
    }

    /**
     * Verifies that saving a step note does not touch an existing general note.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void savingStepNote_afterGeneralNoteExists_doesNotTouchOrOverwriteGeneralNote() {
        PersonalInstructionNote existingGeneralNote = PersonalInstructionNote.builder()
                .id("note-general")
                .user(user)
                .recipe(recipe)
                .note("General note")
                .build();
        UUID instructionUuid = UUID.randomUUID();
        when(noteRepository.findByUserIdAndRecipeIdAndInstructionId(userId, recipeId, instructionUuid.toString()))
                .thenReturn(Optional.empty());

        service.saveNote(new NoteRequestDTO(recipeUuid, instructionUuid, "Step note"), userEmail);

        verify(noteRepository, never()).findByUserIdAndRecipeIdAndInstructionIdIsNull(any(), any());

        ArgumentCaptor<PersonalInstructionNote> captor = ArgumentCaptor.forClass(PersonalInstructionNote.class);
        verify(noteRepository).save(captor.capture());
        PersonalInstructionNote saved = captor.getValue();
        assertThat(saved.getNote()).isEqualTo("Step note");
        assertThat(saved.getInstruction().getId()).isEqualTo(instructionUuid.toString());
        assertThat(saved.getId()).isNotEqualTo(existingGeneralNote.getId());
    }

    /**
     * Verifies that saving a general note updates existing text in place.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void savingGeneralNote_whenOneAlreadyExists_updatesInPlaceRatherThanCreatingANewRow() {
        PersonalInstructionNote existing = PersonalInstructionNote.builder()
                .id("note-general")
                .user(user)
                .recipe(recipe)
                .note("Old text")
                .build();
        when(noteRepository.findByUserIdAndRecipeIdAndInstructionIdIsNull(userId, recipeId)).thenReturn(Optional.of(existing));

        service.saveNote(new NoteRequestDTO(recipeUuid, null, "New text"), userEmail);

        ArgumentCaptor<PersonalInstructionNote> captor = ArgumentCaptor.forClass(PersonalInstructionNote.class);
        verify(noteRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo("note-general");
        assertThat(captor.getValue().getNote()).isEqualTo("New text");
    }

    /**
     * Verifies exception when saving a note for a non-existent recipe.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void saveNote_recipeNotFound_throwsAndNeverSaves() {
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.saveNote(new NoteRequestDTO(recipeUuid, null, "text"), userEmail));

        verify(noteRepository, never()).save(any());
    }

    /**
     * Verifies exception when saving a note for a non-existent user.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void saveNote_userNotFound_throws() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.saveNote(new NoteRequestDTO(recipeUuid, null, "text"), userEmail));
    }

    /**
     * Verifies retrieval of general note for a recipe.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void getNote_returnsOnlyTheGeneralNote() {
        PersonalInstructionNote general = PersonalInstructionNote.builder()
                .id("n1").user(user).recipe(recipe).note("General").build();
        when(noteRepository.findByUserIdAndRecipeIdAndInstructionIdIsNull(userId, recipeId)).thenReturn(Optional.of(general));

        NoteResponse response = service.getNote(recipeId, userEmail);

        assertThat(response.note()).isEqualTo("General");
        assertThat(response.instructionId()).isNull();
    }

    /**
     * Verifies null response when no general note exists.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void getNote_whenNoneExists_returnsNull() {
        when(noteRepository.findByUserIdAndRecipeIdAndInstructionIdIsNull(userId, recipeId)).thenReturn(Optional.empty());

        assertThat(service.getNote(recipeId, userEmail)).isNull();
    }

    /**
     * Verifies retrieval of all notes (both general and per-step) for a recipe.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void getNotesForRecipe_returnsBothGeneralAndPerStepNotes() {
        Instruction instruction = Instruction.builder().id("instr-1").build();
        PersonalInstructionNote general = PersonalInstructionNote.builder()
                .id("n1").user(user).recipe(recipe).note("General").build();
        PersonalInstructionNote step = PersonalInstructionNote.builder()
                .id("n2").user(user).recipe(recipe).instruction(instruction).note("Step").build();
        when(noteRepository.findAllByUserIdAndRecipeId(userId, recipeId)).thenReturn(List.of(general, step));

        List<NoteResponse> notes = service.getNotesForRecipe(recipeId, userEmail);

        assertThat(notes).hasSize(2);
        assertThat(notes).anySatisfy(n -> {
            assertThat(n.instructionId()).isNull();
            assertThat(n.note()).isEqualTo("General");
        });
        assertThat(notes).anySatisfy(n -> {
            assertThat(n.instructionId()).isEqualTo("instr-1");
            assertThat(n.note()).isEqualTo("Step");
        });
    }

    /**
     * Verifies note deletion by its owner.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void deleteNote_byOwner_deletes() {
        PersonalInstructionNote note = PersonalInstructionNote.builder().id("note-1").user(user).recipe(recipe).note("x").build();
        when(noteRepository.findById("note-1")).thenReturn(Optional.of(note));

        service.deleteNote("note-1", userEmail);

        verify(noteRepository).delete(note);
    }

    /**
     * Verifies security exception when attempting note deletion by non-owner.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void deleteNote_byNonOwner_throwsAndDoesNotDelete() {
        User owner = User.builder().id("other-user").email("owner@example.com").build();
        PersonalInstructionNote note = PersonalInstructionNote.builder().id("note-1").user(owner).recipe(recipe).note("x").build();
        when(noteRepository.findById("note-1")).thenReturn(Optional.of(note));

        assertThrows(RuntimeException.class, () -> service.deleteNote("note-1", userEmail));

        verify(noteRepository, never()).delete(any());
    }
}
