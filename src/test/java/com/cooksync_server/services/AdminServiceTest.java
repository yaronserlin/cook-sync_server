package com.cooksync_server.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;

import com.cooksync_server.entities.Tag;
import com.cooksync_server.entities.User;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.repositories.RecipeRepository;
import com.cooksync_server.repositories.ReviewRepository;
import com.cooksync_server.repositories.TagRepository;
import com.cooksync_server.repositories.UserRepository;
import com.dtos.request.tags.TagMergeRequestDTO;
import com.dtos.response.PagedResponse;
import com.dtos.response.admin.DuplicateTagGroupResponse;
import com.dtos.response.user.UserResponse;

/**
 * Unit test for AdminService verifying administrative user management, account enabling/disabling, and tag merging operations.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JdbcTemplate jdbcTemplate;

    private AdminService adminService;

    /**
     * Initializes test fixtures and mocks before each test execution.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @BeforeEach
    void setUp() {
        adminService = new AdminService(reviewRepository, recipeRepository, tagRepository, userRepository, jdbcTemplate);
    }

    /**
     * Verifies that getAllUsers returns requested page results with metadata.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void getAllUsers_returnsRequestedPageWithMetadata() {
        User u1 = User.builder().id("u1").firstName("Ada").lastName("Lovelace").email("ada@example.com").build();
        User u2 = User.builder().id("u2").firstName("Chef").lastName("John").email("chef@example.com").build();
        Pageable pageable = PageRequest.of(1, 2);
        when(userRepository.search(any(), any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(u1, u2), pageable, 5));

        PagedResponse<UserResponse> result = adminService.getAllUsers(1, 2, null, null, "createdAt", "desc");

        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).email()).isEqualTo("ada@example.com");
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.totalElements()).isEqualTo(5);
        assertThat(result.last()).isFalse();
    }

    /**
     * Verifies normalization and trimming of user search queries.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void getAllUsers_normalizesSearchQueryToLowercaseAndTrimsWhitespace() {
        Pageable pageable = PageRequest.of(0, 30);
        when(userRepository.search(eq("ada"), any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        adminService.getAllUsers(0, 30, "  Ada  ", null, "createdAt", "desc");

        verify(userRepository).search(eq("ada"), eq(null), any(Pageable.class));
    }

    /**
     * Verifies that blank search queries pass null into the search specification.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void getAllUsers_blankSearchQuery_passesNullToRepository() {
        Pageable pageable = PageRequest.of(0, 30);
        when(userRepository.search(eq(null), any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        adminService.getAllUsers(0, 30, "   ", true, "createdAt", "desc");

        verify(userRepository).search(eq(null), eq(true), any(Pageable.class));
    }

    /**
     * Verifies filtering by enabled user status.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void getAllUsers_passesEnabledFilterThrough() {
        Pageable pageable = PageRequest.of(0, 30);
        when(userRepository.search(any(), eq(false), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        adminService.getAllUsers(0, 30, null, false, "createdAt", "desc");

        verify(userRepository).search(eq(null), eq(false), any(Pageable.class));
    }

    /**
     * Verifies fallback to createdAt sorting when provided with invalid sort fields.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void getAllUsers_unsortableFieldFallsBackToCreatedAt() {
        Pageable pageable = PageRequest.of(0, 30);
        when(userRepository.search(any(), any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        adminService.getAllUsers(0, 30, null, null, "passwordHash", "desc");

        verify(userRepository).search(eq(null), eq(null), argThat(p ->
                p.getSort().getOrderFor("createdAt") != null));
    }

    /**
     * Verifies ascending direction sort configuration.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void getAllUsers_ascendingDirection_sortsAscending() {
        Pageable pageable = PageRequest.of(0, 30);
        when(userRepository.search(any(), any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        adminService.getAllUsers(0, 30, null, null, "email", "asc");

        verify(userRepository).search(eq(null), eq(null), argThat(p ->
                p.getSort().getOrderFor("email").isAscending()));
    }

    /**
     * Verifies disabling a user account.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void disableUser_setsEnabledFalseAndSaves() {
        User user = User.builder().id("u1").enabled(true).build();
        when(userRepository.findById("u1")).thenReturn(java.util.Optional.of(user));

        adminService.disableUser("u1");

        assertThat(user.isEnabled()).isFalse();
        verify(userRepository).save(user);
    }

    /**
     * Verifies enabling a user account.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void enableUser_setsEnabledTrueAndSaves() {
        User user = User.builder().id("u1").enabled(false).build();
        when(userRepository.findById("u1")).thenReturn(java.util.Optional.of(user));

        adminService.enableUser("u1");

        assertThat(user.isEnabled()).isTrue();
        verify(userRepository).save(user);
    }

    /**
     * Verifies exception thrown when targeting unknown user ID.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void disableUser_unknownId_throws() {
        when(userRepository.findById("missing")).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.disableUser("missing"));
    }

    /**
     * Verifies duplicate tag grouping across casing and whitespace variations.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void getDuplicateTagGroups_groupsNameVariantsIgnoringCaseWhitespaceAndSeparators() {
        Tag vegan1 = Tag.builder().id("t1").name("vegan").build();
        Tag vegan2 = Tag.builder().id("t2").name("Vegan ").build();
        Tag vegan3 = Tag.builder().id("t3").name("VEGAN").build();
        Tag quick = Tag.builder().id("t4").name("quick").build();
        when(tagRepository.findAll()).thenReturn(List.of(vegan1, vegan2, vegan3, quick));

        List<DuplicateTagGroupResponse> groups = adminService.getDuplicateTagGroups();

        assertThat(groups).hasSize(1);
        assertThat(groups.get(0).normalizedName()).isEqualTo("vegan");
        assertThat(groups.get(0).variants()).hasSize(3);
    }

    /**
     * Verifies empty duplicate tag groups when all tags are unique.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void getDuplicateTagGroups_noDuplicates_returnsEmpty() {
        when(tagRepository.findAll()).thenReturn(List.of(
                Tag.builder().id("t1").name("vegan").build(),
                Tag.builder().id("t2").name("quick").build()));

        assertThat(adminService.getDuplicateTagGroups()).isEmpty();
    }

    /**
     * Verifies validation check preventing tag merging onto itself.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void mergeTags_sameSourceAndTarget_throwsWithoutTouchingDatabase() {
        assertThrows(IllegalArgumentException.class, () ->
                adminService.mergeTags(new TagMergeRequestDTO("tag-1", "tag-1")));

        verify(jdbcTemplate, never()).update(anyString(), any(), any());
    }

    /**
     * Verifies exception thrown when source tag is missing.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void mergeTags_missingSourceTag_throwsNotFound() {
        when(tagRepository.existsById("missing-source")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                adminService.mergeTags(new TagMergeRequestDTO("missing-source", "tag-target")));

        verify(jdbcTemplate, never()).update(anyString(), any(), any());
    }

    /**
     * Verifies tag merge database updates reassigning recipes and deleting source tag.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void mergeTags_validRequest_reassignsRecipesThenDeletesSourceTag() {
        when(tagRepository.existsById("tag-source")).thenReturn(true);
        when(tagRepository.existsById("tag-target")).thenReturn(true);

        adminService.mergeTags(new TagMergeRequestDTO("tag-source", "tag-target"));

        verify(jdbcTemplate).update(eq(
                "DELETE rt FROM recipe_tags rt JOIN recipe_tags rt2 ON rt.recipe_id = rt2.recipe_id "
                        + "WHERE rt.tag_id = ? AND rt2.tag_id = ?"),
                eq("tag-source"), eq("tag-target"));
        verify(jdbcTemplate).update(eq("UPDATE recipe_tags SET tag_id = ? WHERE tag_id = ?"),
                eq("tag-target"), eq("tag-source"));
        verify(jdbcTemplate).update(eq("DELETE FROM tags WHERE id = ?"), eq("tag-source"));
        verifyNoMoreInteractions(jdbcTemplate);
    }
}
