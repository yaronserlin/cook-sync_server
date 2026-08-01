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

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        return ResponseEntity.ok(new ApiResponse<>(true, adminService.getStats(), null, "Stats retrieved successfully"));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        return ResponseEntity.ok(new ApiResponse<>(true, adminService.getAllUsers(page, size), null, "Users retrieved successfully"));
    }

    @GetMapping("/reviews/reported")
    public ResponseEntity<ApiResponse<List<ReportedReviewResponse>>> getReportedReviews() {
        return ResponseEntity.ok(new ApiResponse<>(true, adminService.getReportedReviews(), null, "Reported reviews retrieved successfully"));
    }

    @PostMapping("/reviews/{id}/dismiss")
    public ResponseEntity<ApiResponse<Void>> dismissReport(@PathVariable String id) {
        adminService.dismissReport(id);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Report dismissed"));
    }

    @PatchMapping("/users/{id}/disable")
    public ResponseEntity<ApiResponse<Void>> disableUser(@PathVariable String id) {
        adminService.disableUser(id);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "User disabled"));
    }

    @PatchMapping("/users/{id}/enable")
    public ResponseEntity<ApiResponse<Void>> enableUser(@PathVariable String id) {
        adminService.enableUser(id);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "User enabled"));
    }

    @GetMapping("/tags/duplicates")
    public ResponseEntity<ApiResponse<List<DuplicateTagGroupResponse>>> getDuplicateTagGroups() {
        return ResponseEntity.ok(new ApiResponse<>(true, adminService.getDuplicateTagGroups(), null, "Duplicate tag groups retrieved successfully"));
    }

    @PostMapping("/tags/merge")
    public ResponseEntity<ApiResponse<Void>> mergeTags(@Valid @RequestBody TagMergeRequestDTO request) {
        adminService.mergeTags(request);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Tags merged successfully"));
    }
}
