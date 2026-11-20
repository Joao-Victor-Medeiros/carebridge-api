package com.bcndeigth.carebridgeapi.users;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    private static final int LIMITE_FALHAS = 5;
    private static final Duration DURACAO_BLOQUEIO = Duration.ofMinutes(15);

    private final ConcurrentHashMap<String, Tentativas> tentativasPorChave = new ConcurrentHashMap<>();

    public void validarPermitido(String email, String ip) {
        String chave = chave(email, ip);
        Tentativas tentativas = tentativasPorChave.get(chave);
        if (tentativas == null) {
            return;
        }
        if (tentativas.bloqueadoAte() != null && tentativas.bloqueadoAte().isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Muitas tentativas de login. Tente novamente mais tarde.");
        }
        if (tentativas.bloqueadoAte() != null) {
            tentativasPorChave.remove(chave);
        }
    }

    public void registrarFalha(String email, String ip) {
        String chave = chave(email, ip);
        tentativasPorChave.compute(chave, (ignorada, atual) -> {
            int falhas = atual == null ? 1 : atual.falhas() + 1;
            Instant bloqueadoAte = falhas >= LIMITE_FALHAS ? Instant.now().plus(DURACAO_BLOQUEIO) : null;
            return new Tentativas(falhas, bloqueadoAte);
        });
    }

    public void registrarSucesso(String email, String ip) {
        tentativasPorChave.remove(chave(email, ip));
    }

    private String chave(String email, String ip) {
        String emailNormalizado = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        return emailNormalizado + '|' + (ip == null ? "" : ip);
    }

    private record Tentativas(int falhas, Instant bloqueadoAte) {
    }
}