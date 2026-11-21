package com.bcndeigth.carebridgeapi.shared;

import com.bcndeigth.carebridgeapi.oportunidade.dto.OportunidadeRequest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApiExceptionHandlerTest {

    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void enumInvalidoDizOCampoEOsValoresAceitos() {
        Exception erro = assertThrows(Exception.class,
                () -> json.readValue("{\"titulo\":\"x\",\"modalidade\":\"Híbrido\"}", OportunidadeRequest.class));

        assertEquals("Valor inválido em modalidade. Use um destes: PRESENCIAL, ONLINE, HIBRIDO.",
                ApiExceptionHandler.mensagemDeCorpoInvalido(erro));
    }

    @Test
    void jsonQuebradoContinuaComAMensagemGeral() {
        Exception erro = assertThrows(Exception.class, () -> json.readValue("{quebrado", OportunidadeRequest.class));

        assertEquals("O corpo da requisição está ausente ou possui formato inválido.",
                ApiExceptionHandler.mensagemDeCorpoInvalido(erro));
    }
}
