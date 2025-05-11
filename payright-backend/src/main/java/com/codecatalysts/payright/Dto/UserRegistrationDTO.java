package com.codecatalysts.payright.Dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class UserRegistrationDTO {
    @NotBlank
    @Email
    private String username;
    public @NotBlank @Email String getUsername() {
        return username;
    }

    public void setUsername(@NotBlank @Email String username) {
        this.username = username;
    }

    public @NotBlank @Size(min = 6, message = "Password must be at least 6 characters long") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank @Size(min = 6, message = "Password must be at least 6 characters long") String password) {
        this.password = password;
    }

    public @NotBlank String getFullName() {
        return fullName;
    }

    public void setFullName(@NotBlank String fullName) {
        this.fullName = fullName;
    }

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotBlank
    private String fullName;
}

