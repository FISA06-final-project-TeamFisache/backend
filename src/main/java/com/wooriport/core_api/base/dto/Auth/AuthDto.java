package com.wooriport.core_api.base.dto.Auth;

import lombok.Getter;

public class AuthDto {

    @Getter
    public static class SignupRequest {
        private String email;
        private String password;
        private String name;
        private String phone;
    }

    @Getter
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Getter
    public static class TokenResponse {
        private String accessToken;

        public TokenResponse(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}