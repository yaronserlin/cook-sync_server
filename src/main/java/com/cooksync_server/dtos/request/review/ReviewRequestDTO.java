package com.cooksync_server.dtos.request.review;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating or updating a recipe review.
 */
public record ReviewRequestDTO(
    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1.0", message = "Minimum rating is 1.0")
    @DecimalMax(value = "5.0", message = "Maximum rating is 5.0")
    Double rating,
    
    @NotBlank(message = "Review title is required") 
    String title,
    
    String comment
) {}