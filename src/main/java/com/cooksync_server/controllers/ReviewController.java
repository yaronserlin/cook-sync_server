package com.cooksync_server.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cooksync_server.dtos.request.review.ReviewRequestDTO;
import com.cooksync_server.dtos.response.ApiResponse;
import com.cooksync_server.services.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/recipes/{recipeId}/reviews")
    public ResponseEntity<ApiResponse<Void>> addReview(
            @PathVariable String recipeId, 
            @Valid @RequestBody ReviewRequestDTO request, 
            Authentication authentication) {
        String userEmail = authentication.getName();
        reviewService.addReview(recipeId, request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, null, null, "Review added successfully"));
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable String reviewId, 
            Authentication authentication) {
        String userEmail = authentication.getName();
        reviewService.deleteReview(reviewId, userEmail);
        return ResponseEntity.ok(new ApiResponse<>(true, null, null, "Review deleted successfully"));
    }
}