package com.cooksync_server.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cooksync_server.entities.Review;
import com.cooksync_server.entities.Tag;
import com.cooksync_server.entities.User;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.mappers.AdminMapper;
import com.cooksync_server.mappers.TagMapper;
import com.cooksync_server.repositories.RecipeRepository;
import com.cooksync_server.repositories.ReviewRepository;
import com.cooksync_server.repositories.TagRepository;
import com.cooksync_server.repositories.UserRepository;
import com.dtos.request.tags.TagMergeRequestDTO;
import com.dtos.response.PagedResponse;
import com.dtos.response.admin.AdminStatsResponse;
import com.dtos.response.admin.DuplicateTagGroupResponse;
import com.dtos.response.admin.ReportedReviewResponse;
import com.dtos.response.tags.TagResponse;
import com.dtos.response.user.UserResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ReviewRepository reviewRepository;
    private final RecipeRepository recipeRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                reviewRepository.countByReportedTrue(),
                recipeRepository.count(),
                reviewRepository.count(),
                tagRepository.count(),
                userRepository.count()
        );
    }

    public PagedResponse<UserResponse> getAllUsers(int page, int size) {
        Page<User> result = userRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        List<UserResponse> content = result.getContent().stream()
                .map(com.cooksync_server.mappers.UserMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.isLast());
    }

    public List<ReportedReviewResponse> getReportedReviews() {
        return reviewRepository.findByReportedTrue().stream()
                .map(AdminMapper::toReportedReviewResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void dismissReport(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));
        review.setReported(false);
        review.setReportReason(null);
        review.setReportedAt(null);
        reviewRepository.save(review);
    }

    @Transactional
    public void disableUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Transactional
    public void enableUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setEnabled(true);
        userRepository.save(user);
    }

    public List<DuplicateTagGroupResponse> getDuplicateTagGroups() {
        Map<String, List<Tag>> byNormalizedName = new LinkedHashMap<>();
        for (Tag tag : tagRepository.findAll()) {
            String normalized = normalize(tag.getName());
            byNormalizedName.computeIfAbsent(normalized, k -> new ArrayList<>()).add(tag);
        }

        List<DuplicateTagGroupResponse> groups = new ArrayList<>();
        for (Map.Entry<String, List<Tag>> entry : byNormalizedName.entrySet()) {
            if (entry.getValue().size() < 2) {
                continue;
            }
            List<TagResponse> variants = entry.getValue().stream()
                    .map(TagMapper::toResponse)
                    .collect(Collectors.toList());
            groups.add(new DuplicateTagGroupResponse(entry.getKey(), variants));
        }
        return groups;
    }

    /**
     * Merges the source tag into the target tag and deletes the source.
     *
     * Implemented with direct SQL against the recipe_tags join table rather
     * than through the JPA entity graph: both {@code Recipe.tags} and
     * {@code Tag.recipes} independently declare themselves the owning side
     * of that same join table (neither uses mappedBy), so mutating the
     * association only through one entity's in-memory collection risked
     * leaving stale rows behind — which is exactly the previously-reported
     * symptom of the old and new tag both still showing on Home/Filters
     * after a merge. Raw SQL sidesteps that ambiguity entirely.
     */
    @Transactional
    public void mergeTags(TagMergeRequestDTO request) {
        if (request.sourceTagId().equals(request.targetTagId())) {
            throw new IllegalArgumentException("Source and target tags must be different.");
        }
        if (!tagRepository.existsById(request.sourceTagId())) {
            throw new ResourceNotFoundException("Tag", request.sourceTagId());
        }
        if (!tagRepository.existsById(request.targetTagId())) {
            throw new ResourceNotFoundException("Tag", request.targetTagId());
        }

        // Drop join rows that would become duplicates (recipe already has both tags).
        jdbcTemplate.update(
                "DELETE rt FROM recipe_tags rt JOIN recipe_tags rt2 ON rt.recipe_id = rt2.recipe_id " +
                        "WHERE rt.tag_id = ? AND rt2.tag_id = ?",
                request.sourceTagId(), request.targetTagId());

        // Re-point every remaining source-tagged row at the target tag.
        jdbcTemplate.update(
                "UPDATE recipe_tags SET tag_id = ? WHERE tag_id = ?",
                request.targetTagId(), request.sourceTagId());

        // The source tag now has zero references; safe to delete outright.
        jdbcTemplate.update("DELETE FROM tags WHERE id = ?", request.sourceTagId());
    }

    private String normalize(String name) {
        if (name == null) {
            return "";
        }
        return name.toLowerCase().trim().replaceAll("[-_\\s]+", " ");
    }
}
