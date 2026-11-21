package com.bcndeigth.carebridgeapi.inscricao.dto;

import com.bcndeigth.carebridgeapi.inscricao.RespostaTriagem;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record NovaInscricaoRequest(
        UUID oportunidadeId,
        List<RespostaTriagem> respostasTriagem,
        List<DocumentoEnviado> documentos,
        Instant consentimentoDadosSaudeEm
) {
    public record DocumentoEnviado(String documentoId, String nomeArquivo, Long tamanhoBytes) {
    }
}
