package com.cooksync_server.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cooksync_server.entities.Review;
import com.cooksync_server.entities.ReviewReport;
import com.cooksync_server.entities.Tag;
import com.cooksync_server.entities.User;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.mappers.AdminMapper;
import com.cooksync_server.mappers.TagMapper;
import com.cooksync_server.mappers.UserMapper;
import com.cooksync_server.repositories.RecipeRepository;
import com.cooksync_server.repositories.ReviewReportRepository;
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

public interface IAdminService {
    AdminStatsResponse getStats();
    PagedResponse<UserResponse> getAllUsers(int page, int size, String q, Boolean enabled, String sortBy, String direction);
    PagedResponse<ReportedReviewResponse> getReportedReviews(int page, int size);
    void dismissReport(String reviewId);
    void disableUser(String userId);
    void enableUser(String userId);
    PagedResponse<DuplicateTagGroupResponse> getDuplicateTagGroups(int page, int size);
    void mergeTags(TagMergeRequestDTO request);
}