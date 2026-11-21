package com.bcndeigth.carebridgeapi.shared;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void acessoNegadoViraForbiddenEnaoErroInterno() {
        var resposta = handler.tratarSemPermissao(new AuthorizationDeniedException("negado"));

        assertEquals(403, resposta.getStatusCode().value());
        assertEquals(403, resposta.getBody().statusCode());
    }

    @Test
    void falhaDeAutenticacaoViraUnauthorized() {
        var resposta = handler.tratarNaoAutenticado(new BadCredentialsException("senha"));

        assertEquals(401, resposta.getStatusCode().value());
        assertEquals("Sessão expirada ou credenciais inválidas.", resposta.getBody().message());
    }

    @Test
    void rotaInexistenteViraNotFound() {
        var resposta = handler.tratarRotaInexistente(new NoResourceFoundException(HttpMethod.GET, "/nada", "nada"));

        assertEquals(404, resposta.getStatusCode().value());
    }
}
