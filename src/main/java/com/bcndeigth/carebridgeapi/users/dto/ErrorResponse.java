package com.bcndeigth.carebridgeapi.users.dto;

public record ErrorResponse(
        int statusCode,
        String message,
        String error,
        String requestId
) {
}
