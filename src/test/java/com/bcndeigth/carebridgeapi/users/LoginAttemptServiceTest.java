package com.bcndeigth.carebridgeapi.users;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAttemptServiceTest {

    private static final String IP = "200.1.1.1";

    private final RelogioAjustavel relogio = new RelogioAjustavel();
    private final LoginAttemptService service = new LoginAttemptService(relogio);

    @Test
    void bloqueiaNaQuintaFalhaSeguidaPorQuinzeMinutos() {
        falhar("ana@email.com", 4);
        assertDoesNotThrow(() -> service.validarPermitido("ana@email.com", IP));

        falhar("ana@email.com", 1);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.validarPermitido("ANA@email.com ", IP));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatusCode());

        relogio.avancar(Duration.ofMinutes(15));
        assertDoesNotThrow(() -> service.validarPermitido("ana@email.com", IP));
    }

    @Test
    void falhasEspacadasNaoSomam() {
        falhar("ana@email.com", 4);
        relogio.avancar(Duration.ofMinutes(16));
        falhar("ana@email.com", 1);

        assertDoesNotThrow(() -> service.validarPermitido("ana@email.com", IP));
    }

    @Test
    void loginCertoZeraAsFalhas() {
        falhar("ana@email.com", 4);
        service.registrarSucesso("ana@email.com", IP);
        falhar("ana@email.com", 4);

        assertDoesNotThrow(() -> service.validarPermitido("ana@email.com", IP));
    }

    @Test
    void mapaNaoCresceSemLimite() {
        for (int i = 0; i < LoginAttemptService.LIMITE_ENTRADAS; i++) {
            service.registrarFalha("robo" + i + "@x.com", IP);
        }
        relogio.avancar(Duration.ofMinutes(20));
        service.registrarFalha("mais-um@x.com", IP);

        assertTrue(service.entradas() < 10);
    }

    private void falhar(String email, int vezes) {
        for (int i = 0; i < vezes; i++) {
            service.registrarFalha(email, IP);
        }
    }

    private static final class RelogioAjustavel extends Clock {
        private Instant agora = Instant.parse("2026-11-21T12:00:00Z");

        void avancar(Duration duracao) {
            agora = agora.plus(duracao);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return agora;
        }
    }
}
