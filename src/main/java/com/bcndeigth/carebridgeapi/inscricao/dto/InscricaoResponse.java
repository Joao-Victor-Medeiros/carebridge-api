package com.bcndeigth.carebridgeapi.inscricao.dto;

import com.bcndeigth.carebridgeapi.inscricao.DocumentoInscricao;
import com.bcndeigth.carebridgeapi.inscricao.Inscricao;
import com.bcndeigth.carebridgeapi.inscricao.RespostaTriagem;
import com.bcndeigth.carebridgeapi.inscricao.StatusInscricao;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InscricaoResponse(
        UUID id,
        String protocolo,
        StatusInscricao status,
        Oportunidade oportunidade,
        Ofertante ofertante,
        Paciente paciente,
        Instant dataInscricao,
        Instant prazoResposta,
        Instant decididaEm,
        String motivoDecisao,
        PedidoInformacao pedidoInformacao,
        Combinado combinado,
        List<RespostaTriagem> respostasTriagem,
        List<Documento> documentos,
        Instant consentimentoDadosSaudeEm,
        long mensagensNaoLidas,
        MensagemResponse ultimaMensagem,
        boolean podeTrocarMensagens,
        List<MensagemResponse> mensagens
) {
    public record Oportunidade(UUID id, String titulo, String especialidade) {
    }

    public record Ofertante(String nome, String ondeAtende) {
    }

    public record Paciente(String nome) {
    }

    public record PedidoInformacao(String texto, Instant prazo) {
    }

    public record Combinado(String quando, String onde, String levar) {
    }

    public record Documento(UUID id, String documentoId, String nomeArquivo, long tamanhoBytes, Instant enviadoEm) {
        static Documento de(DocumentoInscricao documento) {
            return new Documento(documento.getId(), documento.getDocumentoId(), documento.getNomeArquivo(),
                    documento.getTamanhoBytes(), documento.getEnviadoEm());
        }
    }

    public static InscricaoResponse de(Inscricao i, long naoLidas, MensagemResponse ultima,
                                       List<MensagemResponse> mensagens) {
        var oportunidade = i.getOportunidade();
        var ofertante = oportunidade.getOfertante();
        return new InscricaoResponse(
                i.getId(), i.getProtocolo(), i.getStatus(),
                new Oportunidade(oportunidade.getId(), oportunidade.getTitulo(), oportunidade.getEspecialidade()),
                new Ofertante(ofertante.getNome(), ofertante.getOndeAtende()),
                new Paciente(i.getPaciente().getNome()),
                i.getDataInscricao(), i.getPrazoResposta(), i.getDecididaEm(), i.getMotivoDecisao(),
                i.getPedidoInformacaoTexto() == null ? null
                        : new PedidoInformacao(i.getPedidoInformacaoTexto(), i.getPedidoInformacaoPrazo()),
                i.getStatus() == StatusInscricao.APROVADA
                        ? new Combinado(i.getCombinadoQuando(), i.getCombinadoOnde(), i.getCombinadoLevar()) : null,
                i.getRespostasTriagem(),
                i.getDocumentos().stream().map(Documento::de).toList(),
                i.getConsentimentoDadosSaudeEm(), naoLidas, ultima,
                i.getStatus() == StatusInscricao.APROVADA, mensagens
        );
    }
}
