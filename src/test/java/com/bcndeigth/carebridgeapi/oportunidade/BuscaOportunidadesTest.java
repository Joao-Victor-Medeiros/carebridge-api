package com.bcndeigth.carebridgeapi.oportunidade;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuscaOportunidadesTest {

    @Test
    void termoSemAcentoECurinasEscapados() {
        assertEquals("psicologia", BuscaOportunidades.normalizar("  PSICOLÓGIA "));
        assertEquals("100\\%", BuscaOportunidades.escaparCuringas("100%"));
        assertEquals("a\\_b\\\\c", BuscaOportunidades.escaparCuringas("a_b\\c"));
    }
}
