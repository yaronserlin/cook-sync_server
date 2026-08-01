package com.cooksync_server.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cooksync_server.services.TagService;
import com.dtos.request.tags.TagRequestDTO;
import com.dtos.response.ApiResponse;
import com.dtos.response.tags.TagResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing recipe tags. Reading tags is public, but
 * creating, updating, and deleting requires 'ADMIN' privileges.
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagsController {

    private final TagService tagService;

    // Public endpoints for autocomplete and search features
    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> getAllTags() {
        List<TagResponse> tags = tagService.getAllTags();
        return ResponseEntity.ok(new ApiResponse<>(true, tags, null, "All tags retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TagResponse>> getTagById(@PathVariable String id) {
        TagResponse tag = tagService.getTagById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, tag, null, "Tag retrieved successfully"));
    }

    /**
     * Lets any authenticated user create a custom tag on the fly from the
     * recipe creation wizard, rather than the admin-only {@link #createTag}
     * flow below (which errors on a duplicate name instead of reusing it).
     */
    @PostMapping("/custom")
    public ResponseEntity<ApiResponse<TagResponse>> createCustomTag(@Valid @RequestBody TagRequestDTO request) {
        TagResponse tag = tagService.getOrCreateTag(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, tag, null, "Tag ready"));
    }

    // Admin restricted endpoints
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<TagResponse>> createTag(@Valid @RequestBody TagRequestDTO request) {
        TagResponse createdTag = tagService.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, createdTag, null, "Tag created successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TagResponse>> updateTag(
            @PathVariable String id,
            @Valid @RequestBody TagRequestDTO request) {
        TagResponse updatedTag = tagService.updateTag(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, updatedTag, null, "Tag updated successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable String id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Tag deleted successfully"));
    }
}
