package com.bcndeigth.carebridgeapi.oportunidade.dto;

import com.bcndeigth.carebridgeapi.oportunidade.Oportunidade;

import java.time.Instant;
import java.util.UUID;

public record OportunidadeResponse(
        UUID id,
        UUID ofertanteId,
        String titulo,
        String descricao,
        String tipoServico,
        String publicoAlvo,
        String modalidade,
        String condicoesAcesso,
        boolean statusAtiva,
        Instant ultimaAtualizacao
) {
    public static OportunidadeResponse from(Oportunidade oportunidade) {
        return new OportunidadeResponse(
                oportunidade.getId(), oportunidade.getOfertante().getId(), oportunidade.getTitulo(),
                oportunidade.getDescricao(), oportunidade.getTipoServico(), oportunidade.getPublicoAlvo(),
                oportunidade.getModalidade(), oportunidade.getCondicoesAcesso(), oportunidade.isStatusAtiva(),
                oportunidade.getUltimaAtualizacao()
        );
    }
}