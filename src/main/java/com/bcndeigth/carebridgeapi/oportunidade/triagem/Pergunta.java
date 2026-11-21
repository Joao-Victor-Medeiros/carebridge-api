package com.bcndeigth.carebridgeapi.oportunidade.triagem;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Pergunta(
        String id,
        String rotulo,
        TipoPergunta tipo,
        boolean obrigatoria,
        List<String> opcoes,
        Boolean permiteVarias,
        Integer minCaracteres,
        Integer maxCaracteres,
        String ajuda,
        Dependencia dependeDe
) {
    public record Dependencia(String perguntaId, String resposta) {
    }

    static Pergunta texto(String id, String rotulo, TipoPergunta tipo, Integer min, Integer max) {
        return new Pergunta(id, rotulo, tipo, true, null, null, min, max, null, null);
    }

    static Pergunta simNao(String id, String rotulo) {
        return new Pergunta(id, rotulo, TipoPergunta.SIM_NAO, true, null, null, null, null, null, null);
    }

    static Pergunta condicional(String id, String rotulo, String perguntaId) {
        return new Pergunta(id, rotulo, TipoPergunta.TEXTO_CURTO, true, null, null, null, null, null,
                new Dependencia(perguntaId, "Sim"));
    }

    static Pergunta multipla(String id, String rotulo, boolean obrigatoria, boolean permiteVarias,
                             String ajuda, String... opcoes) {
        return new Pergunta(id, rotulo, TipoPergunta.MULTIPLA, obrigatoria, List.of(opcoes),
                permiteVarias ? Boolean.TRUE : null, null, null, ajuda, null);
    }
}
