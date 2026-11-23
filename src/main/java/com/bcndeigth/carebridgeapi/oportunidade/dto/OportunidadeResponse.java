package com.bcndeigth.carebridgeapi.oportunidade.dto;

import com.bcndeigth.carebridgeapi.oportunidade.FormaContato;
import com.bcndeigth.carebridgeapi.oportunidade.FormularioTriagem;
import com.bcndeigth.carebridgeapi.oportunidade.Modalidade;
import com.bcndeigth.carebridgeapi.oportunidade.ModalidadeAcesso;
import com.bcndeigth.carebridgeapi.oportunidade.Oportunidade;
import com.bcndeigth.carebridgeapi.oportunidade.PublicoAlvo;
import com.bcndeigth.carebridgeapi.oportunidade.StatusOportunidade;
import com.bcndeigth.carebridgeapi.oportunidade.TipoServico;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.DocumentoExigido;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.ModoTriagem;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.Pergunta;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaude;
import com.bcndeigth.carebridgeapi.profissional_saude.SituacaoRegistro;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record OportunidadeResponse(
        UUID id,
        String titulo,
        String descricao,
        TipoServico tipoServico,
        String especialidade,
        Modalidade modalidade,
        ModalidadeAcesso modalidadeAcesso,
        BigDecimal preco,
        List<PublicoAlvo> publicoAlvo,
        String publicoAlvoObservacao,
        Integer vagasTotais,
        int vagasPreenchidas,
        String cep,
        String endereco,
        String cidade,
        String uf,
        Double distanciaKm,
        LocalDate inscricoesDe,
        LocalDate inscricoesAte,
        String criteriosElegibilidade,
        boolean semRestricoes,
        FormaContato formaContato,
        int prazoRespostaDias,
        StatusOportunidade status,
        Instant ultimaAtualizacao,
        Ofertante ofertante,
        Triagem triagem,
        Integer totalInscricoes
) {
    public record Ofertante(UUID id, String nome, String conselho, String uf, String numeroRegistro,
                            String ondeAtende, boolean verificado) {
        static Ofertante de(ProfissionalSaude profissional) {
            return new Ofertante(profissional.getId(), profissional.getNome(), profissional.getConselho(),
                    profissional.getUf(), profissional.getNumeroRegistro(), profissional.getOndeAtende(),
                    profissional.getSituacaoRegistro() == SituacaoRegistro.VERIFICADO);
        }
    }

    public record Triagem(ModoTriagem modo, List<Pergunta> perguntas, List<DocumentoExigido> documentosExigidos) {
        static Triagem de(FormularioTriagem formulario) {
            return formulario == null ? null
                    : new Triagem(formulario.getModo(), formulario.perguntasEfetivas(), formulario.getDocumentosExigidos());
        }
    }

    public static OportunidadeResponse publica(Oportunidade oportunidade) {
        return de(oportunidade, null);
    }

    public static OportunidadeResponse doDono(Oportunidade oportunidade) {
        return de(oportunidade, oportunidade.getTotalInscricoes());
    }

    private static OportunidadeResponse de(Oportunidade o, Integer totalInscricoes) {
        return new OportunidadeResponse(
                o.getId(), o.getTitulo(), o.getDescricao(), o.getTipoServico(), o.getEspecialidade(),
                o.getModalidade(), o.getModalidadeAcesso(), o.getPreco(), o.getPublicoAlvo(),
                o.getPublicoAlvoObservacao(), o.getVagasTotais(), o.getVagasPreenchidas(), o.getCep(),
                o.getEndereco(), o.getCidade(), o.getUf(), null, o.getInscricoesDe(), o.getInscricoesAte(),
                o.getCriteriosElegibilidade(), o.isSemRestricoes(), o.getFormaContato(), o.getPrazoRespostaDias(),
                o.getStatus(), o.getUltimaAtualizacao(), Ofertante.de(o.getOfertante()),
                Triagem.de(o.getFormularioTriagem()), totalInscricoes
        );
    }
}
