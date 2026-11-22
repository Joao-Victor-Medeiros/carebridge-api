package com.bcndeigth.carebridgeapi.auth.dto;

public record PasswordResetRequest(
        String email,
        String code,
        String newPassword
) {
}