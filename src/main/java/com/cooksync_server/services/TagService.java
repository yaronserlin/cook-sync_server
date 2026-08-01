package com.cooksync_server.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtos.request.tags.TagRequestDTO;
import com.dtos.response.tags.TagResponse;
import com.cooksync_server.entities.Tag;
import com.cooksync_server.exceptions.ResourceAllReadyExistsException;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.mappers.TagMapper;
import com.cooksync_server.repositories.TagRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream().map(TagMapper::toResponse).toList();
    }

    public TagResponse getTagById(String id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
        return TagMapper.toResponse(tag);
    }

    /**
     * Used by the "create a tag on the fly" flow in the recipe wizard: unlike
     * {@link #createTag}, an existing tag with the same (normalized) name is
     * treated as success rather than a conflict, since the user just wants a
     * tag they can attach to the recipe — not to be told it already exists.
     */
    @Transactional
    public TagResponse getOrCreateTag(TagRequestDTO request) {
        String formattedName = request.name().trim().toLowerCase();
        return tagRepository.findByNameIgnoreCase(formattedName)
                .map(TagMapper::toResponse)
                .orElseGet(() -> TagMapper.toResponse(
                        tagRepository.save(Tag.builder().name(formattedName).build())));
    }

    @Transactional
    public TagResponse createTag(TagRequestDTO request) {
        String formattedName = request.name().trim().toLowerCase();
        ensureNameAvailable(formattedName, null);

        Tag newTag = Tag.builder()
                .name(formattedName)
                .build();

        return TagMapper.toResponse(tagRepository.save(newTag));
    }

    @Transactional
    public TagResponse updateTag(String id, TagRequestDTO request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));

        String formattedName = request.name().trim().toLowerCase();
        ensureNameAvailable(formattedName, id);

        tag.setName(formattedName);
        return TagMapper.toResponse(tagRepository.save(tag));
    }

    @Transactional
    public void deleteTag(String id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
        tagRepository.delete(tag);
    }

    /**
     * Shared by {@link #createTag} and {@link #updateTag}: rejects a name that
     * collides (case-insensitively) with another tag. {@code excludeId} lets an
     * update keep its own current name without tripping over itself.
     */
    private void ensureNameAvailable(String formattedName, String excludeId) {
        tagRepository.findByNameIgnoreCase(formattedName)
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .ifPresent(existing -> {
                    throw new ResourceAllReadyExistsException("Tag: '" + formattedName + "'", existing.getId());
                });
    }
}
