package com.cooksync_server.dtos.response.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.cooksync_server.entities.User;

class UserResponseTest {

    @Test
    void fromEntity_shouldMapFirstAndLastNames() {
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .passwordHash("hashed")
                .build();

        UserResponse response = UserResponse.fromEntity(user);

        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
    }
}
