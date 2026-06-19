package com.cooksync_server.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {

    private String token; // כאן נשמור את טוקן ההתחברות (JWT)
    private String userId;
    private String name;
    private boolean isAdmin;
}
