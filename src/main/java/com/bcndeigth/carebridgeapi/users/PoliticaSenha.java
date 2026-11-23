package com.bcndeigth.carebridgeapi.users;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class PoliticaSenha {

    public static final String MENSAGEM =
            "A senha deve ter ao menos 8 caracteres, com maiúscula, minúscula, número e caractere especial.";

    private PoliticaSenha() {
    }

    public static void validar(String senha) {
        if (senha == null || senha.length() < 8
                || !senha.matches(".*[A-Z].*")
                || !senha.matches(".*[a-z].*")
                || !senha.matches(".*\\d.*")
                || !senha.matches(".*[^A-Za-z0-9].*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MENSAGEM);
        }
    }
}
