package com.bcndeigth.carebridgeapi.oportunidade.dto;

import com.bcndeigth.carebridgeapi.oportunidade.FormaContato;
import com.bcndeigth.carebridgeapi.oportunidade.Modalidade;
import com.bcndeigth.carebridgeapi.oportunidade.ModalidadeAcesso;
import com.bcndeigth.carebridgeapi.oportunidade.PublicoAlvo;
import com.bcndeigth.carebridgeapi.oportunidade.StatusOportunidade;
import com.bcndeigth.carebridgeapi.oportunidade.TipoServico;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Oportunidade sem id, ofertante, vagasPreenchidas, distanciaKm, ultimaAtualizacao e totalInscricoes.")
public record OportunidadeRequest(
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
        String cep,
        String endereco,
        String cidade,
        String uf,
        LocalDate inscricoesDe,
        LocalDate inscricoesAte,
        String criteriosElegibilidade,
        Boolean semRestricoes,
        FormaContato formaContato,
        Integer prazoRespostaDias,
        @Schema(description = "RASCUNHO salva; qualquer outro valor publica.")
        StatusOportunidade status,
        TriagemRequest triagem
) {
}
