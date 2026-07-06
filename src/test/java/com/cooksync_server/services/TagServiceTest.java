package com.cooksync_server.services;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cooksync_server.entities.Tag;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.repositories.TagRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("🧪 Business Logic Tests - TagService")
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    @Test
    @DisplayName("✅ getAllTags returns all tags")
    void shouldReturnAllTags() {
        Tag tag = Tag.builder().id("t1").name("Vegan").build();
        when(tagRepository.findAll()).thenReturn(List.of(tag));

        var result = tagService.getAllTags();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Vegan");
    }

    @Test
    @DisplayName("✅ getTagById returns a tag when present")
    void shouldReturnTagById() {
        Tag tag = Tag.builder().id("t1").name("Dessert").build();
        when(tagRepository.findById("t1")).thenReturn(Optional.of(tag));

        Tag result = tagService.getTagById("t1");

        assertThat(result.getName()).isEqualTo("Dessert");
    }

    @Test
    @DisplayName("✅ getTagById throws when not found")
    void shouldThrowWhenTagMissing() {
        when(tagRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.getTagById("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tag not found: missing");
    }

    @Test
    @DisplayName("✅ createTag saves a new tag")
    void shouldCreateTag() {
        Tag tag = Tag.builder().name("Quick").build();
        when(tagRepository.existsByNameIgnoreCase("Quick")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> {
            Tag saved = invocation.getArgument(0);
            saved.setId("t1");
            return saved;
        });

        Tag result = tagService.createTag(tag);

        assertThat(result.getId()).isEqualTo("t1");
        assertThat(result.getName()).isEqualTo("Quick");
        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    @Test
    @DisplayName("✅ createTag rejects duplicate names")
    void shouldRejectDuplicateTagNames() {
        Tag tag = Tag.builder().name("Quick").build();
        when(tagRepository.existsByNameIgnoreCase("Quick")).thenReturn(true);

        assertThatThrownBy(() -> tagService.createTag(tag))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tag already exists");
    }
}
