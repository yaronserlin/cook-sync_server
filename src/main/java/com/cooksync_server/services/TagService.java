package com.cooksync_server.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cooksync_server.entities.Tag;
import com.cooksync_server.exceptions.ResourceAllReadyExistsException;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.repositories.TagRepository;

/**
 * Service class responsible for managing tag entities, including retrieval,
 * creation, and deletion operations with transaction support.
 *
 * @author Yaron Serlin
 * @version Last Updated: 06/07/2026
 */
@Service
public class TagService {

    private final TagRepository tagRepository;

    /**
     * Initializes the tag service with the required tag repository.
     *
     * @param tagRepository Repository for accessing and persisting tag data.
     */
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

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
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
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
    public Tag getTagById(String id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
    }

    /**
     * Creates and persists a new tag in the system, ensuring no duplicates
     * exist by name.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * Tag newTag = new Tag();
     * newTag.setName("Vegan");
     * Tag savedTag = tagService.createTag(newTag);
     * }</pre>
     *
     * @param tag The {@link Tag} entity to be created and saved.
     * @return The persisted {@link Tag} entity containing its generated
     * identifier.
     * @throws ResourceAllReadyExistsException if a tag with the same name
     * (case-insensitive) already exists.
     */
    @Transactional
    public Tag createTag(Tag tag) {
        if (tagRepository.existsByNameIgnoreCase(tag.getName())) {
            Tag existingTag = tagRepository.findByNameIgnoreCase(tag.getName()).orElse(null);
            throw new ResourceAllReadyExistsException("Tag: '" + existingTag.getName() + "'", existingTag.getId());
        }
        return tagRepository.save(tag);
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
        Tag existingTag = getTagById(id);
        tagRepository.delete(existingTag);
    }
}
