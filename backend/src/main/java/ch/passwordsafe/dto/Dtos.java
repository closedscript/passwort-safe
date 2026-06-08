package ch.passwordsafe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// ── Auth ──────────────────────────────────────────────────────────────────────

public class AuthDto {

    @Data
    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
    }

    @Data
    public static class LoginResponse {
        private String token;
        private String username;
        public LoginResponse(String token, String username) {
            this.token = token;
            this.username = username;
        }
    }
}

// ── Category ──────────────────────────────────────────────────────────────────

class CategoryDto {

    @Data
    public static class Request {
        @NotBlank @Size(max = 100)
        private String name;
    }

    @Data
    public static class Response {
        private Long id;
        private String name;
        public Response(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}

// ── PasswordEntry ─────────────────────────────────────────────────────────────

class PasswordEntryDto {

    @Data
    public static class Request {
        @NotBlank private String title;
        private String url;
        private String username;
        @NotBlank private String password;
        private String email;
        private String notes;
        private Long categoryId;
    }

    @Data
    public static class Response {
        private Long id;
        private String title;
        private String url;
        private String username;
        private String password;
        private String email;
        private String notes;
        private Long categoryId;
        private String categoryName;
        private String createdAt;
        private String updatedAt;
    }
}
