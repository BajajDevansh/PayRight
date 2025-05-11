package com.codecatalysts.payright.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class AuthResponse {
    private String jwtToken;
    private String userId;
    private String username;

    public AuthResponse(String jwtToken, String userId, String username) {
        this.jwtToken = jwtToken;
        this.userId = userId;
        this.username = username;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
