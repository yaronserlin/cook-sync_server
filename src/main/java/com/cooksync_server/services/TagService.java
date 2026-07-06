package com.cooksync_server.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cooksync_server.entities.Tag;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.repositories.TagRepository;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public Tag getTagById(String id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
    }

    @Transactional
    public Tag createTag(Tag tag) {
        if (tagRepository.existsByNameIgnoreCase(tag.getName())) {
            throw new IllegalArgumentException("Tag already exists: " + tag.getName());
        }
        return tagRepository.save(tag);
    }

    @Transactional
    public void deleteTag(String id) {
        Tag existingTag = getTagById(id);
        tagRepository.delete(existingTag);
    }
}
