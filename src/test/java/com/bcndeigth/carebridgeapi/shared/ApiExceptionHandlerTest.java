package com.bcndeigth.carebridgeapi.shared;

import com.bcndeigth.carebridgeapi.oportunidade.dto.OportunidadeRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApiExceptionHandlerTest {

    private final JsonMapper json = JsonMapper.builder().build();
    private final ApiExceptionHandler handler = new ApiExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oportunidades");

    @Test
    void acessoNegadoViraForbiddenEnaoErroInterno() {
        var resposta = handler.semPermissao(new AuthorizationDeniedException("negado"), request);

        assertEquals(403, resposta.getStatusCode().value());
        assertEquals(403, resposta.getBody().status());
        assertEquals("/oportunidades", resposta.getBody().path());
    }

    @Test
    void falhaDeAutenticacaoViraUnauthorized() {
        var resposta = handler.naoAutenticado(new BadCredentialsException("senha"), request);

        assertEquals(401, resposta.getStatusCode().value());
        assertEquals("Sessão expirada ou credenciais inválidas.", resposta.getBody().message());
    }

    @Test
    void rotaInexistenteViraNotFound() {
        var resposta = handler.rotaInexistente(new NoResourceFoundException(HttpMethod.GET, "/nada", "nada"), request);

        assertEquals(404, resposta.getStatusCode().value());
    }

    @Test
    void enumInvalidoDizOCampoEOsValoresAceitos() {
        Exception erro = assertThrows(Exception.class,
                () -> json.readValue("{\"titulo\":\"x\",\"modalidade\":\"Híbrido\"}", OportunidadeRequest.class));

        assertEquals("Valor inválido em modalidade. Use um destes: PRESENCIAL, ONLINE, HIBRIDO.",
                ApiExceptionHandler.mensagemDeCorpoInvalido(erro));
    }

    @Test
    void jsonQuebradoContinuaComAMensagemGeral() {
        Exception erro = assertThrows(Exception.class, () -> json.readValue("{quebrado", OportunidadeRequest.class));

        assertEquals("O corpo da requisição está ausente ou possui formato inválido.",
                ApiExceptionHandler.mensagemDeCorpoInvalido(erro));
    }
}
