package com.bcndeigth.carebridgeapi.shared;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public record ErrorResponse(
        int status,
        String message,
        String error,
        String path,
        Instant timestamp
) {
    private static final String MENSAGEM_PADRAO = "Não foi possível concluir a solicitação.";

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return new ErrorResponse(
                status.value(),
                message == null || message.isBlank() ? MENSAGEM_PADRAO : message,
                status.getReasonPhrase(),
                path,
                Instant.now()
        );
    }
}
