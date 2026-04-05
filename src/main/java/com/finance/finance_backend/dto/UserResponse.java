package com.finance.finance_backend.dto;

import com.finance.finance_backend.entity.User;
import com.finance.finance_backend.enums.Role;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

// Response DTO for user data.

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static UserResponse fromEntity(User user){
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();

    }
}
