package com.zorvyn.finance.dto;

import com.zorvyn.finance.model.Role;
import com.zorvyn.finance.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

public class UserDto {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;

        @NotNull(message = "Role is required")
        private Role role;
    }

    @Data
    public static class UpdateRequest {
        @Email(message = "Invalid email format")
        private String email;

        private Role role;

        private Boolean active;
    }

    @Data
    public static class Response {
        private Long id;
        private String username;
        private String email;
        private String role;
        private boolean active;
        private LocalDateTime createdAt;

        public static Response from(User user) {
            Response r = new Response();
            r.id = user.getId();
            r.username = user.getUsername();
            r.email = user.getEmail();
            r.role = user.getRole().name();
            r.active = user.isActive();
            r.createdAt = user.getCreatedAt();
            return r;
        }
    }
}
