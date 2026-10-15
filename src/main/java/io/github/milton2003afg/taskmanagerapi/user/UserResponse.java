package io.github.milton2003afg.taskmanagerapi.user;

import java.time.LocalDateTime;

public record UserResponse(Long id, String email, LocalDateTime createdAt) {
    public static UserResponse from(User user){
        return new UserResponse(user.getId(), user.getEmail(), user.getCreatedAt());
    }
}
