package com.bcndeigth.carebridgeapi.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Codigo de 6 digitos da verificacao de e-mail e da troca de senha. Com
 * app.codigo-fixo preenchido (padrao 218218) o codigo e sempre esse e nenhum
 * e-mail sai: o envio fica simulado, ja que o Render gratuito bloqueia SMTP.
 */
@Component
public class CodigoAcesso {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final String codigoFixo;

    public CodigoAcesso(@Value("${app.codigo-fixo:218218}") String codigoFixo) {
        this.codigoFixo = codigoFixo == null ? "" : codigoFixo.trim();
    }

    public String gerar() {
        return simulado() ? codigoFixo : "%06d".formatted(RANDOM.nextInt(1_000_000));
    }

    public boolean simulado() {
        return !codigoFixo.isEmpty();
    }
}
