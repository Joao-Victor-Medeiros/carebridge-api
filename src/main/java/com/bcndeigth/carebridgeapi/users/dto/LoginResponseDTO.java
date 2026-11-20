package com.bcndeigth.carebridgeapi.users.dto;

public record LoginResponseDTO(
        String token,
        long expiresIn,
        UserProfileResponse user
) {
}