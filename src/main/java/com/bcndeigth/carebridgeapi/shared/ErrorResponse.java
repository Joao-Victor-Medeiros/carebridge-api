package com.bcndeigth.carebridgeapi.shared;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public record ErrorResponse(
        int statusCode,
        String message,
        String error,
        String requestId
) {
    private static final String MENSAGEM_PADRAO = "Não foi possível concluir a solicitação.";

    public static ErrorResponse of(HttpStatus status, String message) {
        return new ErrorResponse(
                status.value(),
                message == null || message.isBlank() ? MENSAGEM_PADRAO : message,
                status.getReasonPhrase(),
                UUID.randomUUID().toString()
        );
    }
}
