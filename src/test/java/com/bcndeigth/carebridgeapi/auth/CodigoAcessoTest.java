package com.bcndeigth.carebridgeapi.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CodigoAcessoTest {

    @Test
    void codigoFixoSimulaOEnvio() {
        CodigoAcesso codigo = new CodigoAcesso("218218");
        assertThat(codigo.simulado()).isTrue();
        assertThat(codigo.gerar()).isEqualTo("218218");
    }

    @Test
    void semCodigoFixoGeraSeisDigitosAleatorios() {
        CodigoAcesso codigo = new CodigoAcesso(" ");
        assertThat(codigo.simulado()).isFalse();
        assertThat(codigo.gerar()).matches("\\d{6}");
    }
}
