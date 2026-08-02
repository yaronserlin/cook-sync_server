package com.cooksync_server.controllers;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import com.cooksync_server.services.AdminService;
import com.dtos.request.tags.TagMergeRequestDTO;
import com.dtos.response.ApiResponse;
import com.dtos.response.PagedResponse;
import com.dtos.response.admin.AdminStatsResponse;
import com.dtos.response.admin.DuplicateTagGroupResponse;
import com.dtos.response.admin.ReportedReviewResponse;
import com.dtos.response.user.UserResponse;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller exposing administrative operations for system moderation, user management, and tag deduplication.
 * Protected by administrative role authorization.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * Retrieves aggregated system stats for the administrative dashboard.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @return response entity containing AdminStatsResponse payload
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        return ResponseEntity.ok(new ApiResponse<>(true, adminService.getStats(), null, "Stats retrieved successfully"));
    }

    /**
     * Retrieves paginated list of registered user accounts.
     *
     * Complexity:
     * Time: O(N) where N is page size
     * Space: O(N)
     *
     * @param page zero-based page index
     * @param size page size limit
     * @return response entity containing PagedResponse of UserResponse DTOs
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        return ResponseEntity.ok(new ApiResponse<>(true, adminService.getAllUsers(page, size), null, "Users retrieved successfully"));
    }

    /**
     * Retrieves list of review entries flagged as reported.
     *
     * Complexity:
     * Time: O(R) where R is reported review count
     * Space: O(R)
     *
     * @return response entity containing list of ReportedReviewResponse DTOs
     */
    @GetMapping("/reviews/reported")
    public ResponseEntity<ApiResponse<List<ReportedReviewResponse>>> getReportedReviews() {
        return ResponseEntity.ok(new ApiResponse<>(true, adminService.getReportedReviews(), null, "Reported reviews retrieved successfully"));
    }

    /**
     * Dismisses moderation report for specified review ID.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param id target review ID
     * @return response entity acknowledging report dismissal
     */
    @PostMapping("/reviews/{id}/dismiss")
    public ResponseEntity<ApiResponse<Void>> dismissReport(@PathVariable String id) {
        adminService.dismissReport(id);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Report dismissed"));
    }

    /**
     * Disables user account with specified ID.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param id target user ID
     * @return response entity acknowledging account disabling
     */
    @PatchMapping("/users/{id}/disable")
    public ResponseEntity<ApiResponse<Void>> disableUser(@PathVariable String id) {
        adminService.disableUser(id);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "User disabled"));
    }

    /**
     * Enables user account with specified ID.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param id target user ID
     * @return response entity acknowledging account enabling
     */
    @PatchMapping("/users/{id}/enable")
    public ResponseEntity<ApiResponse<Void>> enableUser(@PathVariable String id) {
        adminService.enableUser(id);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "User enabled"));
    }

    /**
     * Detects and groups potential duplicate tags for consolidation audit.
     *
     * Complexity:
     * Time: O(T) where T is total tag count
     * Space: O(T)
     *
     * @return response entity containing list of DuplicateTagGroupResponse DTOs
     */
    @GetMapping("/tags/duplicates")
    public ResponseEntity<ApiResponse<List<DuplicateTagGroupResponse>>> getDuplicateTagGroups() {
        return ResponseEntity.ok(new ApiResponse<>(true, adminService.getDuplicateTagGroups(), null, "Duplicate tag groups retrieved successfully"));
    }

    /**
     * Merges source duplicate tag into canonical target tag and deletes source.
     *
     * Complexity:
     * Time: O(R) where R is count of recipes tagged with source tag
     * Space: O(1)
     *
     * @param request tag merge payload containing source and target tag IDs
     * @return response entity acknowledging tag merge completion
     */
    @PostMapping("/tags/merge")
    public ResponseEntity<ApiResponse<Void>> mergeTags(@Valid @RequestBody TagMergeRequestDTO request) {
        adminService.mergeTags(request);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Tags merged successfully"));
    }
}
