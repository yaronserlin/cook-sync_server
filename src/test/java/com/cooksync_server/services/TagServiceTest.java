package com.cooksync_server.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cooksync_server.entities.Tag;
import com.cooksync_server.exceptions.ResourceAllReadyExistsException;
import com.cooksync_server.repositories.TagRepository;
import com.dtos.request.tags.TagRequestDTO;
import com.dtos.response.tags.TagResponse;

/**
 * Covers the "create a custom tag on the fly" flow added for the recipe
 * wizard: unlike the admin-only {@code createTag}, {@code getOrCreateTag}
 * must treat an existing tag as success (reuse it) rather than a conflict.
 */
@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    private TagService tagService;

    @BeforeEach
    void setUp() {
        tagService = new TagService(tagRepository);
    }

    @Test
    void getOrCreateTag_newName_createsAndSavesNormalizedTag() {
        when(tagRepository.findByNameIgnoreCase("vegan")).thenReturn(Optional.empty());
        Tag saved = Tag.builder().id("tag-1").name("vegan").build();
        when(tagRepository.save(any(Tag.class))).thenReturn(saved);

        TagResponse response = tagService.getOrCreateTag(new TagRequestDTO("  Vegan  "));

        assertThat(response.id()).isEqualTo("tag-1");
        assertThat(response.name()).isEqualTo("vegan");
    }

    @Test
    void getOrCreateTag_existingName_reusesExistingTagInsteadOfCreating() {
        Tag existing = Tag.builder().id("tag-existing").name("vegan").build();
        when(tagRepository.findByNameIgnoreCase("vegan")).thenReturn(Optional.of(existing));

        TagResponse response = tagService.getOrCreateTag(new TagRequestDTO("VEGAN"));

        assertThat(response.id()).isEqualTo("tag-existing");
        verify(tagRepository, never()).save(any());
    }

    @Test
    void createTag_duplicateName_throwsInsteadOfReusing() {
        Tag existing = Tag.builder().id("tag-existing").name("vegan").build();
        when(tagRepository.findByNameIgnoreCase("vegan")).thenReturn(Optional.of(existing));

        assertThrows(ResourceAllReadyExistsException.class, () -> tagService.createTag(new TagRequestDTO("Vegan")));

        verify(tagRepository, never()).save(any());
    }

    @Test
    void createTag_newName_createsIt() {
        when(tagRepository.findByNameIgnoreCase("quick")).thenReturn(Optional.empty());
        Tag saved = Tag.builder().id("tag-2").name("quick").build();
        when(tagRepository.save(any(Tag.class))).thenReturn(saved);

        TagResponse response = tagService.createTag(new TagRequestDTO("Quick"));

        assertThat(response.id()).isEqualTo("tag-2");
        assertThat(response.name()).isEqualTo("quick");
    }
}
