package com.finance.finance_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


//Request DTO for user login
@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "Email is Required")
    @Email(message = "Invalid Email format")
    private String email;
    @NotBlank(message = "Password is Required")
    private String password;
}
