package com.bcndeigth.carebridgeapi.inscricao;

import com.bcndeigth.carebridgeapi.inscricao.dto.InscricaoResponse;
import com.bcndeigth.carebridgeapi.inscricao.dto.MensagemResponse;
import com.bcndeigth.carebridgeapi.inscricao.dto.NovaInscricaoRequest;
import com.bcndeigth.carebridgeapi.inscricao.dto.RespostaPedidoRequest;
import com.bcndeigth.carebridgeapi.oportunidade.Oportunidade;
import com.bcndeigth.carebridgeapi.oportunidade.OportunidadeRepository;
import com.bcndeigth.carebridgeapi.oportunidade.StatusOportunidade;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.DocumentoExigido;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.Pergunta;
import com.bcndeigth.carebridgeapi.paciente.Paciente;
import com.bcndeigth.carebridgeapi.paciente.PacienteRepository;
import com.bcndeigth.carebridgeapi.shared.Datas;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PacienteInscricaoService {

    private static final long TAMANHO_MAXIMO_DOCUMENTO = 10L * 1024 * 1024;
    private static final String ID_RESPOSTA_PEDIDO = "pedido-informacao";
    private static final String SOLICITACAO_ABERTA = "Você já tem uma solicitação aberta para esta oportunidade.";
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final InscricaoRepository inscricaoRepository;
    private final OportunidadeRepository oportunidadeRepository;
    private final PacienteRepository pacienteRepository;
    private final InscricaoLeitura leitura;

    public PacienteInscricaoService(InscricaoRepository inscricaoRepository,
                                    OportunidadeRepository oportunidadeRepository,
                                    PacienteRepository pacienteRepository, InscricaoLeitura leitura) {
        this.inscricaoRepository = inscricaoRepository;
        this.oportunidadeRepository = oportunidadeRepository;
        this.pacienteRepository = pacienteRepository;
        this.leitura = leitura;
    }

    @Transactional(readOnly = true)
    public List<InscricaoResponse> listar(Usuario usuario) {
        return inscricaoRepository.findByPacienteIdOrderByDataInscricaoDesc(usuario.getId())
                .stream().map(i -> leitura.resumo(i, TipoPerfil.PACIENTE)).toList();
    }

    @Transactional(readOnly = true)
    public InscricaoResponse detalhar(UUID id, Usuario usuario) {
        return leitura.detalhe(daPaciente(id, usuario), TipoPerfil.PACIENTE);
    }

    @Transactional
    public InscricaoResponse inscrever(NovaInscricaoRequest request, Usuario usuario) {
        if (request == null || request.oportunidadeId() == null) {
            throw Erros.invalido("Informe a oportunidade.");
        }
        Oportunidade oportunidade = oportunidadeRepository.findById(request.oportunidadeId())
                .filter(o -> o.getStatus() != StatusOportunidade.RASCUNHO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oportunidade não encontrada."));
        if (oportunidade.getStatus() != StatusOportunidade.ATIVA) {
            throw Erros.conflito("Esta oportunidade foi encerrada.");
        }
        exigirPeriodoAberto(oportunidade);
        if (oportunidade.getVagasPreenchidas() >= oportunidade.getVagasTotais()) {
            throw Erros.conflito("Não há vagas nesta oportunidade.");
        }
        if (inscricaoRepository.existsByPacienteIdAndOportunidadeIdAndStatusIn(usuario.getId(), oportunidade.getId(),
                StatusInscricao.ABERTAS)) {
            throw Erros.conflito(SOLICITACAO_ABERTA);
        }
        if (request.consentimentoDadosSaudeEm() == null) {
            throw Erros.invalido("É preciso consentir com o uso dos dados de saúde.");
        }

        var triagem = oportunidade.getFormularioTriagem();
        List<Pergunta> perguntas = triagem == null ? List.of() : triagem.perguntasEfetivas();
        List<DocumentoExigido> exigidos = triagem == null ? List.of() : triagem.getDocumentosExigidos();
        List<RespostaTriagem> respostas = ValidadorTriagem.validar(perguntas, request.respostasTriagem());
        List<NovaInscricaoRequest.DocumentoEnviado> documentos =
                request.documentos() == null ? List.of() : request.documentos();
        exigirDocumentos(exigidos, documentos);

        Instant agora = InscricaoLeitura.agora();
        Inscricao inscricao = new Inscricao();
        inscricao.setProtocolo(String.valueOf(inscricaoRepository.proximoProtocolo()));
        inscricao.setPaciente(paciente(usuario));
        inscricao.setOportunidade(oportunidade);
        inscricao.setStatus(StatusInscricao.EM_ANALISE);
        inscricao.setRespostasTriagem(respostas);
        inscricao.setConsentimentoDadosSaudeEm(request.consentimentoDadosSaudeEm().truncatedTo(ChronoUnit.SECONDS));
        inscricao.setDataInscricao(agora);
        inscricao.setPrazoResposta(agora.plus(oportunidade.getPrazoRespostaDias(), ChronoUnit.DAYS));
        documentos.forEach(d -> inscricao.adicionarDocumento(
                new DocumentoInscricao(d.documentoId(), d.nomeArquivo().trim(), d.tamanhoBytes(), agora)));
        try {
            return leitura.resumo(inscricaoRepository.saveAndFlush(inscricao), TipoPerfil.PACIENTE);
        } catch (DataIntegrityViolationException ex) {
            throw Erros.conflito(SOLICITACAO_ABERTA);
        }
    }

    private static void exigirPeriodoAberto(Oportunidade oportunidade) {
        LocalDate hoje = Datas.hoje();
        if (oportunidade.getInscricoesDe() != null && hoje.isBefore(oportunidade.getInscricoesDe())) {
            throw Erros.conflito("As inscrições abrem em " + oportunidade.getInscricoesDe().format(DIA) + ".");
        }
        if (oportunidade.getInscricoesAte() != null && hoje.isAfter(oportunidade.getInscricoesAte())) {
            throw Erros.conflito("As inscrições desta oportunidade foram encerradas.");
        }
    }

    @Transactional
    public void cancelar(UUID id, Usuario usuario) {
        Inscricao inscricao = daPaciente(id, usuario);
        if (!inscricao.getStatus().aberta()) {
            throw Erros.conflito(Erros.JA_DECIDIDA);
        }
        inscricao.setStatus(StatusInscricao.CANCELADA);
        inscricao.setDecididaEm(InscricaoLeitura.agora());
        inscricao.setRespostasTriagem(new ArrayList<>());
        inscricao.getDocumentos().clear();
        inscricao.setPedidoInformacaoTexto(null);
        inscricao.setPedidoInformacaoPrazo(null);
    }

    @Transactional
    public InscricaoResponse responderPedido(UUID id, RespostaPedidoRequest request, Usuario usuario) {
        if (request == null || request.texto() == null || request.texto().isBlank()) {
            throw Erros.invalido("Escreva a resposta.");
        }
        Inscricao inscricao = daPaciente(id, usuario);
        if (inscricao.getStatus() != StatusInscricao.AGUARDANDO_INFORMACOES) {
            throw Erros.conflito("Não há pedido de informação em aberto.");
        }
        List<RespostaTriagem> respostas = new ArrayList<>(inscricao.getRespostasTriagem());
        long respondidos = respostas.stream().filter(r -> r.perguntaId().startsWith(ID_RESPOSTA_PEDIDO)).count();
        respostas.add(new RespostaTriagem(ID_RESPOSTA_PEDIDO + "-" + (respondidos + 1), inscricao.getPedidoInformacaoTexto(),
                request.texto().trim()));
        inscricao.setRespostasTriagem(respostas);
        if (request.anexo() != null) {
            var anexo = request.anexo();
            validarArquivo(anexo.nomeArquivo(), anexo.tamanhoBytes());
            inscricao.adicionarDocumento(new DocumentoInscricao(null, anexo.nomeArquivo().trim(), anexo.tamanhoBytes(),
                    InscricaoLeitura.agora()));
        }
        inscricao.setStatus(StatusInscricao.EM_ANALISE);
        inscricao.setPedidoInformacaoTexto(null);
        inscricao.setPedidoInformacaoPrazo(null);
        return leitura.resumo(inscricaoRepository.saveAndFlush(inscricao), TipoPerfil.PACIENTE);
    }

    @Transactional
    public List<MensagemResponse> mensagens(UUID id, Usuario usuario) {
        Inscricao inscricao = daPaciente(id, usuario);
        leitura.marcarComoLidas(inscricao, TipoPerfil.PACIENTE);
        return leitura.mensagens(inscricao);
    }

    @Transactional
    public MensagemResponse enviarMensagem(UUID id, String texto, Usuario usuario) {
        return leitura.enviar(daPaciente(id, usuario), TipoPerfil.PACIENTE, texto);
    }

    @Transactional
    public void apagarDocumentos(UUID id, Usuario usuario) {
        daPaciente(id, usuario).getDocumentos().clear();
    }

    private void exigirDocumentos(List<DocumentoExigido> exigidos, List<NovaInscricaoRequest.DocumentoEnviado> enviados) {
        enviados.forEach(d -> validarArquivo(d.nomeArquivo(), d.tamanhoBytes()));
        Set<String> recebidos = enviados.stream().map(NovaInscricaoRequest.DocumentoEnviado::documentoId)
                .collect(Collectors.toSet());
        exigidos.stream().filter(d -> !recebidos.contains(d.id())).findFirst().ifPresent(d -> {
            throw Erros.invalido("Falta o documento: " + d.rotulo() + ".");
        });
    }

    private static void validarArquivo(String nome, Long tamanho) {
        if (nome == null || nome.isBlank() || tamanho == null || tamanho <= 0) {
            throw Erros.invalido("Documento sem nome ou sem conteúdo.");
        }
        if (tamanho > TAMANHO_MAXIMO_DOCUMENTO) {
            throw Erros.invalido("Cada documento pode ter no máximo 10 MB.");
        }
    }

    private Inscricao daPaciente(UUID id, Usuario usuario) {
        return inscricaoRepository.findById(id)
                .filter(i -> i.doPaciente(usuario.getId()))
                .orElseThrow(Erros::naoEncontrada);
    }

    private Paciente paciente(Usuario usuario) {
        return pacienteRepository.findById(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil de paciente não encontrado."));
    }
}
