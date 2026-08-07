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

public interface ITagService {
    com.dtos.response.PagedResponse<TagResponse> getAllTags(int page, int size);
    TagResponse getTagById(String id);
    TagResponse getOrCreateTag(TagRequestDTO request);
    TagResponse createTag(TagRequestDTO request);
    TagResponse updateTag(String id, TagRequestDTO request);
    void deleteTag(String id);
}