package com.finance.finance_backend.dto;

import com.finance.finance_backend.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

//Request DTO for updating an existing user
@Getter
@Setter
@Builder
public class UserRequest {
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;
    @NotNull(message = "Role is required")
    private Role role;
    @NotNull(message = "Active status is Required")
    private Boolean isActive;
}
