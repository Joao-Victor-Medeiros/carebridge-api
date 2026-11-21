package com.bcndeigth.carebridgeapi.shared;

import com.bcndeigth.carebridgeapi.users.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> tratarValidacaoDto(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getField)
                .distinct()
                .map(campo -> {
                    String msg = ex.getBindingResult().getFieldErrors().stream()
                            .filter(err -> err.getField().equals(campo))
                            .map(FieldError::getDefaultMessage)
                            .filter(m -> m != null && !m.isBlank())
                            .findFirst()
                            .orElse("valor inválido");
                    return campo + ": " + msg;
                })
                .collect(Collectors.joining("; "));

        String message = detalhes.isBlank()
                ? "Dados inválidos na requisição."
                : "Dados inválidos: " + detalhes;

        return resposta(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> tratarViolacaoConstraint(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        String detalhes = ex.getConstraintViolations().stream()
                .map(this::formatarViolacao)
                .collect(Collectors.joining("; "));

        String message = detalhes.isBlank()
                ? "Parâmetros inválidos na requisição."
                : "Dados inválidos: " + detalhes;

        return resposta(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> tratarResponseStatus(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        HttpStatus resolved = status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
        String message = (ex.getReason() == null || ex.getReason().isBlank())
                ? mensagemPadrao(resolved)
                : ex.getReason();

        return resposta(resolved, message, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> tratarNaoAutenticado(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        return resposta(HttpStatus.UNAUTHORIZED, "Sessão expirada ou credenciais inválidas.", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> tratarSemPermissao(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return resposta(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este recurso.", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> tratarRotaInexistente(
            NoResourceFoundException ex,
            HttpServletRequest request
    ) {
        return resposta(HttpStatus.NOT_FOUND, "Recurso não encontrado.", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> tratarConflitoIntegridade(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        log.warn("Violação de integridade de dados: {}", ex.getMostSpecificCause().getMessage());
        return resposta(
                HttpStatus.CONFLICT,
                "Já existe um registro cadastrado com os dados informados ou conflito de integridade.",
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> tratarJsonInvalido(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return resposta(
                HttpStatus.BAD_REQUEST,
                "O corpo da requisição está ausente ou possui formato JSON inválido.",
                request
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> tratarArquivoGrande(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request
    ) {
        return resposta(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "O arquivo ou a requisição excede o tamanho máximo permitido.",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> tratarErroInesperado(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Erro inesperado na requisição", ex);
        return resposta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Não foi possível concluir a solicitação.",
                request
        );
    }

    private String formatarViolacao(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath() != null ? violation.getPropertyPath().toString() : "campo";
        String msg = (violation.getMessage() == null || violation.getMessage().isBlank())
                ? "valor inválido"
                : violation.getMessage();
        return path + ": " + msg;
    }

    private ResponseEntity<ErrorResponse> resposta(HttpStatus status, String message, HttpServletRequest request) {
        String requestId = ApiErrorWriter.resolveRequestId(request);
        return ResponseEntity.status(status).body(new ErrorResponse(
                status.value(),
                message,
                nomeErro(status),
                requestId
        ));
    }

    private String mensagemPadrao(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Requisição inválida.";
            case UNAUTHORIZED -> "Sessão expirada ou credenciais inválidas.";
            case FORBIDDEN -> "Você não tem permissão para acessar este recurso.";
            case NOT_FOUND -> "Recurso não encontrado.";
            case CONFLICT -> "Conflito com o estado atual do recurso.";
            case UNPROCESSABLE_ENTITY -> "Não foi possível processar os dados enviados.";
            case TOO_MANY_REQUESTS -> "Muitas requisições em pouco tempo. Tente novamente mais tarde.";
            case PAYLOAD_TOO_LARGE -> "O arquivo ou a requisição excede o tamanho máximo permitido.";
            case INTERNAL_SERVER_ERROR -> "Não foi possível concluir a solicitação.";
            default -> status.getReasonPhrase();
        };
    }

    private String nomeErro(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Bad Request";
            case UNAUTHORIZED -> "Unauthorized";
            case FORBIDDEN -> "Forbidden";
            case NOT_FOUND -> "Not Found";
            case CONFLICT -> "Conflict";
            case UNPROCESSABLE_ENTITY -> "Unprocessable Entity";
            case TOO_MANY_REQUESTS -> "Too Many Requests";
            case PAYLOAD_TOO_LARGE -> "Payload Too Large";
            case INTERNAL_SERVER_ERROR -> "Internal Server Error";
            default -> status.getReasonPhrase();
        };
    }
}