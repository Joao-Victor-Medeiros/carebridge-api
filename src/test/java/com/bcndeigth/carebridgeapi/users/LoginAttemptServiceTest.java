package com.bcndeigth.carebridgeapi.users;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginAttemptServiceTest {

    private static final String IP = "200.1.1.1";

    private final LoginAttemptService service = new LoginAttemptService();

    @Test
    void bloqueiaDepoisDeCincoFalhasSeguidas() {
        falhar("ana@email.com", 4);
        assertDoesNotThrow(() -> service.validarPermitido("ana@email.com", IP));

        falhar("ana@email.com", 1);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.validarPermitido("ana@email.com", IP));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatusCode());
    }

    @Test
    void contaPorEmailSemDiferenciarMaiusculas() {
        falhar("ANA@email.com ", 5);

        assertThrows(ResponseStatusException.class, () -> service.validarPermitido("ana@email.com", IP));
        assertDoesNotThrow(() -> service.validarPermitido("outra@email.com", IP));
    }

    @Test
    void loginCertoZeraAsFalhas() {
        falhar("ana@email.com", 4);
        service.registrarSucesso("ana@email.com", IP);
        falhar("ana@email.com", 4);

        assertDoesNotThrow(() -> service.validarPermitido("ana@email.com", IP));
    }

    private void falhar(String email, int vezes) {
        for (int i = 0; i < vezes; i++) {
            service.registrarFalha(email, IP);
        }
    }
}
