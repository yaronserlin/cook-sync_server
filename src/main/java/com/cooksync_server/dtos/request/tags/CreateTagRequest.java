package com.cooksync_server.dtos.request.tags;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTagRequest(
        @NotBlank(message = "Tag name cannot be blank")
        @Size(min = 2, max = 50, message = "Tag name must be between 2 and 50 characters")
        String name
        ) {

}
