package com.cooksync_server.dtos.response.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String userId;
    private String name;
    private boolean isAdmin;
}
