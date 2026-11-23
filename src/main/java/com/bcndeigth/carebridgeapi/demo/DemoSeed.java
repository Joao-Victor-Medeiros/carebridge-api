package com.bcndeigth.carebridgeapi.demo;

import com.bcndeigth.carebridgeapi.inscricao.RespostaTriagem;
import com.bcndeigth.carebridgeapi.inscricao.StatusInscricao;
import com.bcndeigth.carebridgeapi.inscricao.dto.InscricaoResponse;
import com.bcndeigth.carebridgeapi.oportunidade.FormaContato;
import com.bcndeigth.carebridgeapi.oportunidade.Modalidade;
import com.bcndeigth.carebridgeapi.oportunidade.ModalidadeAcesso;
import com.bcndeigth.carebridgeapi.oportunidade.PublicoAlvo;
import com.bcndeigth.carebridgeapi.oportunidade.StatusOportunidade;
import com.bcndeigth.carebridgeapi.oportunidade.TipoServico;
import com.bcndeigth.carebridgeapi.oportunidade.dto.TriagemRequest;
import com.bcndeigth.carebridgeapi.profissional_saude.SituacaoRegistro;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

record DemoSeed(
        Instant referencia,
        List<Profissional> profissionais,
        List<Paciente> pacientes,
        List<Oportunidade> oportunidades,
        List<Inscricao> inscricoes
) {
    record Profissional(String email, String nome, String conselho, String uf, String numeroRegistro,
                        String especialidade, String ondeAtende, SituacaoRegistro situacaoRegistro) {
    }

    record Paciente(String email, String nome, LocalDate dataNascimento, String telefone) {
    }

    record Oportunidade(String chave, String ofertante, String titulo, String descricao, TipoServico tipoServico,
                        String especialidade, Modalidade modalidade, ModalidadeAcesso modalidadeAcesso,
                        BigDecimal preco, List<PublicoAlvo> publicoAlvo, String publicoAlvoObservacao,
                        Integer vagasTotais, int vagasPreenchidas, String cep, String endereco, String cidade,
                        String uf, LocalDate inscricoesDe, LocalDate inscricoesAte, String criteriosElegibilidade,
                        boolean semRestricoes, FormaContato formaContato, Integer prazoRespostaDias,
                        StatusOportunidade status, Instant ultimaAtualizacao, TriagemRequest triagem) {
    }

    record Inscricao(String paciente, String oportunidade, StatusInscricao status, Instant dataInscricao,
                     Instant prazoResposta, Instant decididaEm, String motivoDecisao,
                     InscricaoResponse.PedidoInformacao pedidoInformacao, InscricaoResponse.Combinado combinado,
                     List<RespostaTriagem> respostasTriagem, Instant consentimentoDadosSaudeEm,
                     List<Documento> documentos, List<Mensagem> mensagens) {
    }

    record Documento(String documentoId, String nomeArquivo, long tamanhoBytes, Instant enviadoEm) {
    }

    record Mensagem(TipoPerfil autor, String texto, Instant enviadaEm, boolean lida) {
    }
}
