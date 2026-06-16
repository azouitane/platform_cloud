package com.virtacore.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 50 characters")
        String firstName,


        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 50 characters")
        String lastName,
        

        @NotBlank(message = "Password confirmation is required")
        String password
) {
}
