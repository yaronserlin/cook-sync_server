package com.cooksync_server.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cooksync_server.dtos.request.CreateTagRequest;
import com.cooksync_server.entities.Tag;
import com.cooksync_server.services.TagService;

import lombok.RequiredArgsConstructor;

/**
 * REST controller handling administrative endpoints for system tag management.
 * Restricts access to users possessing administrative privileges.
 *
 * @author Yaron Serlin
 * @version Last Updated: 06/07/2026
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final TagService tagService;

    /**
     * Retrieves all tags currently available in the system.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * ResponseEntity<List<Tag>> response = adminController.getAllTags();
     * }</pre>
     *
     * @return A {@link ResponseEntity} containing a {@link List} of all
     * {@link Tag} entities and an HTTP 200 OK status.
     */
    @GetMapping("/tags")
    public ResponseEntity<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    /**
     * Retrieves a specific tag by its unique identifier.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * ResponseEntity<Tag> response = adminController.getTagById("uuid-1234");
     * }</pre>
     *
     * @param id The unique string identifier of the tag to retrieve.
     * @return A {@link ResponseEntity} containing the requested {@link Tag} and
     * an HTTP 200 OK status.
     */
    @GetMapping("/tags/{id}")
    public ResponseEntity<Tag> getTagById(@PathVariable String id) {
        Tag tag = tagService.getTagById(id);
        return ResponseEntity.ok(tag);
    }

    /**
     * Creates and stores a new tag in the system.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * CreateTagRequest req = new CreateTagRequest("Healthy");
     * ResponseEntity<Tag> response = adminController.createTag(req);
     * }</pre>
     *
     * @param request The validated data transfer object containing the
     * necessary details to create a new tag.
     * @return A {@link ResponseEntity} containing the newly persisted
     * {@link Tag} and an HTTP 201 Created status.
     */
    @PostMapping("/tags")
    public ResponseEntity<Tag> createTag(@RequestBody CreateTagRequest request) {
        Tag createdTag = tagService.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
    }

    /**
     * Deletes a specific tag from the system by its unique identifier.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * ResponseEntity<Void> response = adminController.deleteTag("uuid-1234");
     * }</pre>
     *
     * @param id The unique string identifier of the tag targeted for deletion.
     * @return A {@link ResponseEntity} with no content and an HTTP 204 No
     * Content status upon successful deletion.
     */
    @DeleteMapping("/tags/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable String id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
