package com.finance.finance_backend.dto;

import com.finance.finance_backend.enums.Role;
import lombok.Builder;
import lombok.Getter;

//Response DTO returned after successful authentication

@Getter
@Builder
public class AuthResponse {
    private String token;
    private String name;
    private String email;
    private Role role;
    private String tokenType;
}
