package com.bcndeigth.carebridgeapi.shared;

import com.bcndeigth.carebridgeapi.users.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void deveRetornarErroPadronizadoEmResponseStatusException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-Id", "req-409-test");

        ResponseEntity<ErrorResponse> response = handler.tratarResponseStatus(
                new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um usuário cadastrado com este e-mail."),
                request
        );

        assertEquals(409, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().statusCode());
        assertEquals("Já existe um usuário cadastrado com este e-mail.", response.getBody().message());
        assertEquals("Conflict", response.getBody().error());
        assertEquals("req-409-test", response.getBody().requestId());
    }

    @Test
    void deveResumirCamposInvalidosEmMethodArgumentNotValidException() throws NoSuchMethodException {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "cadastroDto");
        bindingResult.addError(new FieldError("cadastroDto", "email", "deve ser um e-mail válido"));
        bindingResult.addError(new FieldError("cadastroDto", "senha", "não deve estar em branco"));

        Method dummyMethod = DummyController.class.getDeclaredMethod("dummy", String.class);
        org.springframework.core.MethodParameter param = new org.springframework.core.MethodParameter(dummyMethod, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, bindingResult);

        MockHttpServletRequest request = new MockHttpServletRequest();
        ResponseEntity<ErrorResponse> response = handler.tratarValidacaoDto(ex, request);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().statusCode());
        assertEquals("Bad Request", response.getBody().error());
        assertTrue(response.getBody().message().contains("email: deve ser um e-mail válido"));
        assertTrue(response.getBody().message().contains("senha: não deve estar em branco"));
        assertFalse(response.getBody().requestId().isBlank());
    }

    @Test
    void deveTratarIntegridadeSegurancaEErroInesperadoComContratoPadrao() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<ErrorResponse> conflict = handler.tratarConflitoIntegridade(
                new DataIntegrityViolationException("duplicate key"), request);
        ResponseEntity<ErrorResponse> unauthorized = handler.tratarNaoAutenticado(
                new BadCredentialsException("bad credentials"), request);
        ResponseEntity<ErrorResponse> forbidden = handler.tratarSemPermissao(
                new AccessDeniedException("denied"), request);
        ResponseEntity<ErrorResponse> internal = handler.tratarErroInesperado(
                new RuntimeException("boom"), request);

        assertEquals(409, conflict.getBody().statusCode());
        assertFalse(conflict.getBody().message().isBlank());
        assertEquals("Conflict", conflict.getBody().error());

        assertEquals(401, unauthorized.getBody().statusCode());
        assertFalse(unauthorized.getBody().message().isBlank());
        assertEquals("Unauthorized", unauthorized.getBody().error());

        assertEquals(403, forbidden.getBody().statusCode());
        assertFalse(forbidden.getBody().message().isBlank());
        assertEquals("Forbidden", forbidden.getBody().error());

        assertEquals(500, internal.getBody().statusCode());
        assertFalse(internal.getBody().message().isBlank());
        assertEquals("Internal Server Error", internal.getBody().error());
    }

    static class DummyController {
        void dummy(String body) {
        }
    }
}