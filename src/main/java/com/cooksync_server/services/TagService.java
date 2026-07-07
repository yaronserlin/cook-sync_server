package com.cooksync_server.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.cooksync_server.dtos.request.tags.CreateTagRequest;
import com.cooksync_server.dtos.response.tags.TagResponse;
import com.cooksync_server.entities.Tag;
import com.cooksync_server.exceptions.ResourceAllReadyExistsException;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.repositories.TagRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service class responsible for managing tag entities, including retrieval,
 * creation, and deletion operations with transaction support.
 *
 * @author Yaron Serlin
 * @version Last Updated: 06/07/2026
 */
@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    /**
     * Retrieves all tags currently stored in the system.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * List<Tag> tags = tagService.getAllTags();
     * }</pre>
     *
     * @return A {@link List} of all {@link Tag} entities.
     */
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(this::mapToResponse) // פונקציית המרה
                .toList();
    }

    /**
     * Retrieves a specific tag by its unique identifier.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * Tag tag = tagService.getTagById("uuid-1234");
     * }</pre>
     *
     * @param id The unique identifier of the tag to retrieve.
     * @return The requested {@link Tag} entity.
     * @throws ResourceNotFoundException if no tag is found with the provided
     * identifier.
     */
    public TagResponse getTagById(String id) {

        return mapToResponse(tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id)));
    }

    /**
     * Creates and persists a new tag in the system, ensuring no duplicates
     * exist by name.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * CreateTagRequest request = new CreateTagRequest("Vegan");
     * Tag savedTag = tagService.createTag(request);
     * }</pre>
     *
     * @param request The data transfer object containing the new tag's details.
     * @return The persisted {@link Tag} entity containing its generated
     * identifier.
     * @throws ResourceAllReadyExistsException if a tag with the same name
     * (case-insensitive) already exists.
     * @throws IllegalArgumentException if the request or name is null.
     */
    @Transactional
    public TagResponse createTag(CreateTagRequest request) {
        // System.out.println("Creating tag with request: " + request);

        String rawName = request.name().toLowerCase().trim();
        String formattedName = StringUtils.capitalize(rawName);

        Optional<Tag> existingTagOpt = tagRepository.findByNameIgnoreCase(formattedName);
        if (existingTagOpt.isPresent()) {
            Tag existingTag = existingTagOpt.get();
            throw new ResourceAllReadyExistsException("Tag: '" + existingTag.getName() + "'", existingTag.getId());
        }

        Tag newTag = Tag.builder().name(formattedName).build();
        return mapToResponse(tagRepository.save(newTag));
    }

    /**
     * Deletes a specific tag from the system by its unique identifier.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * tagService.deleteTag("uuid-1234");
     * }</pre>
     *
     * @param id The unique identifier of the tag to delete.
     * @throws ResourceNotFoundException if the tag to be deleted does not
     * exist.
     */
    @Transactional
    public void deleteTag(String id) {
        TagResponse existingTag = getTagById(id);
        tagRepository.delete(mapToEntity(existingTag));
    }

    private Tag mapToEntity(TagResponse existingTag) {
        return Tag.builder()
                .id(existingTag.getId())
                .name(existingTag.getName())
                .build();
    }

    private TagResponse mapToResponse(Tag tag) {
        return new TagResponse(
                tag.getId(),
                tag.getName(),
                tag.getCreatedAt().toString()
        );
    }
}
