package com.cooksync_server.services;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dtos.request.review.ReportReviewRequestDTO;
import com.dtos.request.review.ReviewRequestDTO;
import com.dtos.response.review.ReviewResponse;
import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.Review;
import com.cooksync_server.entities.ReviewReport;
import com.cooksync_server.entities.User;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.mappers.ReviewMapper;
import com.cooksync_server.repositories.RecipeRepository;
import com.cooksync_server.repositories.ReviewReportRepository;
import com.cooksync_server.repositories.ReviewRepository;
import com.cooksync_server.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

public interface IReviewService {
    com.dtos.response.PagedResponse<ReviewResponse> getReviewsForRecipe(String recipeId, int page, int size);
    void addReview(String recipeId, ReviewRequestDTO request, String userEmail);
    void deleteReview(String reviewId, String userEmail);
    void reportReview(String reviewId, ReportReviewRequestDTO request, String userEmail);
}