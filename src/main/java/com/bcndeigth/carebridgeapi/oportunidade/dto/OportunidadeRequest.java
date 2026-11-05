package com.bcndeigth.carebridgeapi.oportunidade.dto;

public record OportunidadeRequest(
        String titulo,
        String descricao,
        String tipoServico,
        String publicoAlvo,
        String modalidade,
        String condicoesAcesso,
        boolean statusAtiva
) {
}