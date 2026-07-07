package com.cooksync_server.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cooksync_server.dtos.request.tags.TagRequestDTO;
import com.cooksync_server.dtos.response.tags.TagResponse;
import com.cooksync_server.entities.Tag;
import com.cooksync_server.exceptions.ResourceAllReadyExistsException;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.repositories.TagRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<TagResponse> getAllTags() {
        return TagResponse.fromEntities(tagRepository.findAll());
    }

    public TagResponse getTagById(String id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
        return TagResponse.fromEntity(tag);
    }

    @Transactional
    public TagResponse createTag(TagRequestDTO request) {
        String formattedName = request.name().trim().toLowerCase();

        Optional<Tag> existingTag = tagRepository.findByNameIgnoreCase(formattedName);
        if (existingTag.isPresent()) {
            throw new ResourceAllReadyExistsException("Tag: '" + formattedName + "'", existingTag.get().getId());
        }

        Tag newTag = Tag.builder()
                .name(formattedName)
                .build();

        return TagResponse.fromEntity(tagRepository.save(newTag));
    }

    @Transactional
    public TagResponse updateTag(String id, TagRequestDTO request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
        
        String formattedName = request.name().trim().toLowerCase();
        
        Optional<Tag> existingTag = tagRepository.findByNameIgnoreCase(formattedName);
        if (existingTag.isPresent() && !existingTag.get().getId().equals(id)) {
            throw new ResourceAllReadyExistsException("Tag: '" + formattedName + "'", existingTag.get().getId());
        }

        tag.setName(formattedName);
        return TagResponse.fromEntity(tagRepository.save(tag));
    }

    @Transactional
    public void deleteTag(String id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
        tagRepository.delete(tag);
    }
}