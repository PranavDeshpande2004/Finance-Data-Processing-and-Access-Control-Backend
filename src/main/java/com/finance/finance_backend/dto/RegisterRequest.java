package com.finance.finance_backend.dto;

import com.finance.finance_backend.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Request DTO for registering a new user
@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message = "Name is Required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;
    @NotBlank(message = "Email is Required")
    @Email(message = "Invalid email format")
    private String email;
    @NotBlank(message = "Password is Required")
    @Size(min = 8,message = "Password must be at least 8 characters")
    private String password;

//    @NotNull(message = "Role is Required")
//    private Role role;
}
