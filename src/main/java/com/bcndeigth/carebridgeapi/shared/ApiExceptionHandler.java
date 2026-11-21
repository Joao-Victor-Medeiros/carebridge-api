package com.bcndeigth.carebridgeapi.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> regraDeNegocio(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());
        return resposta(status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status, exception.getReason());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> naoAutenticado(AuthenticationException exception) {
        return resposta(HttpStatus.UNAUTHORIZED, "Sessão expirada ou credenciais inválidas.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> semPermissao(AccessDeniedException exception) {
        return resposta(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este recurso.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> conflitoDeIntegridade(DataIntegrityViolationException exception) {
        return resposta(HttpStatus.CONFLICT, "Os dados informados conflitam com um registro existente.");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> arquivoGrande(MaxUploadSizeExceededException exception) {
        return resposta(HttpStatus.PAYLOAD_TOO_LARGE, "O arquivo ou a requisição excede o tamanho máximo permitido.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> corpoInvalido(HttpMessageNotReadableException exception) {
        return resposta(HttpStatus.BAD_REQUEST, "O corpo da requisição está ausente ou possui formato inválido.");
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> parametroInvalido(Exception exception) {
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetro ausente ou com formato inválido.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> rotaInexistente(NoResourceFoundException exception) {
        return resposta(HttpStatus.NOT_FOUND, "Recurso não encontrado.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> metodoNaoSuportado(HttpRequestMethodNotSupportedException exception) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, "Método não suportado para este recurso.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> formatoNaoSuportado(HttpMediaTypeNotSupportedException exception) {
        return resposta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Formato de conteúdo não suportado.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> erroInesperado(Exception exception) {
        log.error("Erro inesperado", exception);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, null);
    }

    private ResponseEntity<ErrorResponse> resposta(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status, message));
    }
}
