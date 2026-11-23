package com.bcndeigth.carebridgeapi.users;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PoliticaSenhaTest {

    @Test
    void aceitaSenhaComTodasAsRegras() {
        assertThatCode(() -> PoliticaSenha.validar("Senha123!")).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Sn1!", "senha123!", "SENHA123!", "SenhaForte!", "Senha1234"})
    void recusaSenhaFraca(String senha) {
        assertThatThrownBy(() -> PoliticaSenha.validar(senha))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(erro -> ((ResponseStatusException) erro).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
