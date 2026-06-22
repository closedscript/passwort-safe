package ch.passwordsafe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

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
