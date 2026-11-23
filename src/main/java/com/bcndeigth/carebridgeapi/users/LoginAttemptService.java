package com.bcndeigth.carebridgeapi.users;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    private static final int LIMITE_FALHAS = 5;
    private static final Duration DURACAO_BLOQUEIO = Duration.ofMinutes(15);
    private static final Duration JANELA_FALHAS = Duration.ofMinutes(15);
    static final int LIMITE_ENTRADAS = 10_000;

    private final ConcurrentHashMap<String, Tentativas> tentativasPorChave = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginAttemptService() {
        this(Clock.systemUTC());
    }

    LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public void validarPermitido(String email, String ip) {
        Tentativas tentativas = tentativasPorChave.get(chave(email, ip));
        if (tentativas != null && tentativas.bloqueadoEm(clock.instant())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Muitas tentativas de login. Tente novamente mais tarde.");
        }
    }

    public void registrarFalha(String email, String ip) {
        Instant agora = clock.instant();
        tentativasPorChave.compute(chave(email, ip), (ignorada, atual) -> {
            int falhas = atual == null || atual.expirada(agora) ? 1 : atual.falhas() + 1;
            Instant bloqueadoAte = falhas >= LIMITE_FALHAS ? agora.plus(DURACAO_BLOQUEIO) : null;
            return new Tentativas(falhas, agora, bloqueadoAte);
        });
        if (tentativasPorChave.size() > LIMITE_ENTRADAS) {
            tentativasPorChave.values().removeIf(t -> t.expirada(agora));
        }
    }

    public void registrarSucesso(String email, String ip) {
        tentativasPorChave.remove(chave(email, ip));
    }

    int entradas() {
        return tentativasPorChave.size();
    }

    private String chave(String email, String ip) {
        String emailNormalizado = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        return emailNormalizado + '|' + (ip == null ? "" : ip);
    }

    private record Tentativas(int falhas, Instant ultimaFalha, Instant bloqueadoAte) {
        boolean bloqueadoEm(Instant agora) {
            return bloqueadoAte != null && bloqueadoAte.isAfter(agora);
        }

        boolean expirada(Instant agora) {
            Instant fim = bloqueadoAte != null ? bloqueadoAte : ultimaFalha.plus(JANELA_FALHAS);
            return !fim.isAfter(agora);
        }
    }
}
