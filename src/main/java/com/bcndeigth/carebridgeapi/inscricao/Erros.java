package com.bcndeigth.carebridgeapi.inscricao;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

final class Erros {

    static final String JA_DECIDIDA = "Esta solicitação já foi decidida.";

    private Erros() {
    }

    static ResponseStatusException invalido(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }

    static ResponseStatusException conflito(String mensagem) {
        return new ResponseStatusException(HttpStatus.CONFLICT, mensagem);
    }

    static ResponseStatusException naoEncontrada() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada.");
    }
}
