package com.bcndeigth.carebridgeapi.shared;

import com.bcndeigth.carebridgeapi.users.dto.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> tratarRegraDeNegocio(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());
        String error = status == null ? "Error" : nomeErro(status);
        return resposta(exception.getStatusCode().value(), error, exception.getReason());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> tratarConflitoDeIntegridade(DataIntegrityViolationException exception) {
        return resposta(HttpStatus.CONFLICT.value(), nomeErro(HttpStatus.CONFLICT),
                "Não foi possível concluir o cadastro porque os dados já estão em uso.");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> tratarArquivoGrande(MaxUploadSizeExceededException exception) {
        return resposta(HttpStatus.PAYLOAD_TOO_LARGE.value(), nomeErro(HttpStatus.PAYLOAD_TOO_LARGE),
                "O arquivo ou a requisição excede o tamanho máximo permitido.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> tratarJsonInvalido(HttpMessageNotReadableException exception) {
        return resposta(HttpStatus.BAD_REQUEST.value(), nomeErro(HttpStatus.BAD_REQUEST),
                "O corpo da requisição está ausente ou possui formato inválido.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> tratarErroInesperado(Exception exception) {
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR.value(), nomeErro(HttpStatus.INTERNAL_SERVER_ERROR),
                "Não foi possível concluir a solicitação.");
    }

    private ResponseEntity<ErrorResponse> resposta(int statusCode, String error, String message) {
        return ResponseEntity.status(statusCode).body(new ErrorResponse(
                statusCode,
                message == null || message.isBlank() ? "Não foi possível concluir a solicitação." : message,
                error,
                UUID.randomUUID().toString()
        ));
    }

    private String nomeErro(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Bad Request";
            case UNAUTHORIZED -> "Unauthorized";
            case FORBIDDEN -> "Forbidden";
            case NOT_FOUND -> "Not Found";
            case CONFLICT -> "Conflict";
            case UNPROCESSABLE_CONTENT -> "Unprocessable Entity";
            case TOO_MANY_REQUESTS -> "Too Many Requests";
            case PAYLOAD_TOO_LARGE -> "Payload Too Large";
            case INTERNAL_SERVER_ERROR -> "Internal Server Error";
            default -> "Error";
        };
    }
}