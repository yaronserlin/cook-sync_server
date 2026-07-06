package com.cooksync_server.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.cooksync_server.dtos.request.tags.CreateTagRequest;
import com.cooksync_server.dtos.response.ApiResponse;
import com.cooksync_server.dtos.response.tags.TagResponse;
import com.cooksync_server.services.TagService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller responsible for handling administrative endpoints related to
 * tag management. Restricts access to users possessing the 'ADMIN' role and
 * wraps outputs in a standardized API response.
 *
 * @author Yaron Serlin
 * @version Last Updated: 06/07/2026
 */
@RestController
@RequestMapping("/api/admin/tags")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class TagsController {

    private final TagService tagService;
    private static final Logger logger = LoggerFactory.getLogger(TagsController.class);

    /**
     * Retrieves a comprehensive list of all tags currently available in the
     * system.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * ResponseEntity<ApiResponse<List<TagResponse>>> response = tagsController.getAllTags();
     * }</pre>
     *
     * @return A {@link ResponseEntity} containing an {@link ApiResponse}
     * wrapping a list of {@link TagResponse} objects and an HTTP 200 OK status.
     */
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getAllTags() {
        logger.info("Fetching all tags from the system");
        List<TagResponse> tags = tagService.getAllTags();
        return ResponseEntity.ok(new ApiResponse<>(true, tags, null, "All tags retrieved successfully"));
    }

    /**
     * Retrieves a specific tag based on its unique identifier.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * ResponseEntity<ApiResponse<TagResponse>> response = tagsController.getTagById("uuid-1234");
     * }</pre>
     *
     * @param id The unique string identifier of the tag to retrieve.
     * @return A {@link ResponseEntity} containing an {@link ApiResponse}
     * wrapping the requested {@link TagResponse} and an HTTP 200 OK status.
     */
    @GetMapping("{id}")
    public ResponseEntity<ApiResponse<TagResponse>> getTagById(@PathVariable String id) {
        TagResponse tag = tagService.getTagById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, tag, null, "Tag retrieved successfully"));
    }

    /**
     * Creates and stores a new tag in the system based on the provided request
     * payload.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * CreateTagRequest request = new CreateTagRequest("Gluten-Free");
     * ResponseEntity<ApiResponse<TagResponse>> response = tagsController.createTag(request);
     * }</pre>
     *
     * @param request The validated data transfer object containing the
     * necessary details for the new tag.
     * @return A {@link ResponseEntity} containing an {@link ApiResponse}
     * wrapping the newly created {@link TagResponse} and an HTTP 201 Created
     * status.
     */
    @PostMapping("")
    public ResponseEntity<ApiResponse<TagResponse>> createTag(@Valid @RequestBody CreateTagRequest request) {
        TagResponse createdTag = tagService.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, createdTag, null, "Tag created successfully"));
    }

    /**
     * Deletes a specific tag from the system using its unique identifier.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * ResponseEntity<ApiResponse<Void>> response = tagsController.deleteTag("uuid-1234");
     * }</pre>
     *
     * @param id The unique string identifier of the tag targeted for deletion.
     * @return A {@link ResponseEntity} containing an {@link ApiResponse} with
     * no content payload and an HTTP 200 OK status upon successful deletion.
     */
    @DeleteMapping("{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable String id) {
        tagService.deleteTag(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, null, null, "Tag deleted successfully"));
    }
}
