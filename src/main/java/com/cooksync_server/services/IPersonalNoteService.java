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

public interface IPersonalNoteService {
    void saveNote(NoteRequestDTO request, String userEmail);
    NoteResponse getNote(String recipeId, String userEmail);
    com.dtos.response.PagedResponse<NoteResponse> getNotesForRecipe(String recipeId, String userEmail, int page, int size);
    com.dtos.response.PagedResponse<NoteResponse> getMyNotes(String userEmail, int page, int size);
    void deleteNote(String noteId, String userEmail);
}