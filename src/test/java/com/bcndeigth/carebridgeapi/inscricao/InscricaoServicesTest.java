package com.bcndeigth.carebridgeapi.inscricao;

import com.bcndeigth.carebridgeapi.inscricao.dto.DecisaoRequest;
import com.bcndeigth.carebridgeapi.inscricao.dto.InscricaoResponse;
import com.bcndeigth.carebridgeapi.inscricao.dto.NovaInscricaoRequest;
import com.bcndeigth.carebridgeapi.oportunidade.FormularioTriagem;
import com.bcndeigth.carebridgeapi.oportunidade.Oportunidade;
import com.bcndeigth.carebridgeapi.oportunidade.OportunidadeRepository;
import com.bcndeigth.carebridgeapi.oportunidade.StatusOportunidade;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.DocumentoExigido;
import com.bcndeigth.carebridgeapi.paciente.Paciente;
import com.bcndeigth.carebridgeapi.paciente.PacienteRepository;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaude;
import com.bcndeigth.carebridgeapi.shared.Datas;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InscricaoServicesTest {

    @Mock
    private InscricaoRepository inscricaoRepository;
    @Mock
    private OportunidadeRepository oportunidadeRepository;
    @Mock
    private PacienteRepository pacienteRepository;
    @Mock
    private MensagemRepository mensagemRepository;

    private PacienteInscricaoService pacienteService;
    private ProfissionalInscricaoService profissionalService;
    private Usuario pacienteUsuario;
    private Usuario profissionalUsuario;
    private Oportunidade oportunidade;

    @BeforeEach
    void setUp() {
        InscricaoLeitura leitura = new InscricaoLeitura(mensagemRepository);
        pacienteService = new PacienteInscricaoService(inscricaoRepository, oportunidadeRepository, pacienteRepository, leitura);
        profissionalService = new ProfissionalInscricaoService(inscricaoRepository, oportunidadeRepository, leitura);

        pacienteUsuario = usuario("ana@email.com", TipoPerfil.PACIENTE);
        profissionalUsuario = usuario("mariana@clinica.com", TipoPerfil.PROFISSIONAL_SAUDE);
        Paciente paciente = new Paciente("Ana Souza", LocalDate.of(1988, 3, 12), null, null, pacienteUsuario);
        paciente.setId(pacienteUsuario.getId());
        ProfissionalSaude profissional = new ProfissionalSaude("Mariana Costa", "CRM", "DF", "123456", "cardiologia",
                "Instituto Movida", List.of(), null, profissionalUsuario);
        profissional.setId(profissionalUsuario.getId());

        oportunidade = new Oportunidade();
        oportunidade.setId(UUID.randomUUID());
        oportunidade.setTitulo("Cardiologia");
        oportunidade.setOfertante(profissional);
        oportunidade.setStatus(StatusOportunidade.ATIVA);
        oportunidade.setVagasTotais(1);
        FormularioTriagem triagem = new FormularioTriagem();
        triagem.setDocumentosExigidos(List.of(new DocumentoExigido("rg", "RG")));
        oportunidade.definirTriagem(triagem);

        when(oportunidadeRepository.findById(oportunidade.getId())).thenReturn(Optional.of(oportunidade));
        when(oportunidadeRepository.ocuparVaga(oportunidade.getId())).thenReturn(1);
        when(pacienteRepository.findById(pacienteUsuario.getId())).thenReturn(Optional.of(paciente));
        when(inscricaoRepository.proximoProtocolo()).thenReturn(20001L);
        when(inscricaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inscricaoRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mensagemRepository.findFirstByInscricaoIdOrderByEnviadaEmDesc(any())).thenReturn(Optional.empty());
    }

    @Test
    void inscricaoNasceEmAnaliseComPrazoDaOportunidade() {
        InscricaoResponse resposta = pacienteService.inscrever(pedido(true, true), pacienteUsuario);

        assertEquals(StatusInscricao.EM_ANALISE, resposta.status());
        assertEquals("20001", resposta.protocolo());
        assertEquals(15, ChronoUnit.DAYS.between(resposta.dataInscricao(), resposta.prazoResposta()));
        assertEquals("rg", resposta.documentos().getFirst().documentoId());
    }

    @Test
    void recusaInscricaoSemDocumentoSemConsentimentoOuRepetida() {
        assertStatus(HttpStatus.BAD_REQUEST, () -> pacienteService.inscrever(pedido(false, true), pacienteUsuario));
        assertStatus(HttpStatus.BAD_REQUEST, () -> pacienteService.inscrever(pedido(true, false), pacienteUsuario));

        when(inscricaoRepository.existsByPacienteIdAndOportunidadeIdAndStatusIn(eq(pacienteUsuario.getId()),
                eq(oportunidade.getId()), any())).thenReturn(true);
        assertStatus(HttpStatus.CONFLICT, () -> pacienteService.inscrever(pedido(true, true), pacienteUsuario));
    }

    @Test
    void semVagaOuEncerradaNaoRecebeInscricao() {
        oportunidade.setVagasPreenchidas(1);
        assertStatus(HttpStatus.CONFLICT, () -> pacienteService.inscrever(pedido(true, true), pacienteUsuario));

        oportunidade.setVagasPreenchidas(0);
        oportunidade.setStatus(StatusOportunidade.ENCERRADA);
        assertStatus(HttpStatus.CONFLICT, () -> pacienteService.inscrever(pedido(true, true), pacienteUsuario));
    }

    @Test
    void foraDoPeriodoDeInscricoesNaoRecebeSolicitacao() {
        oportunidade.setInscricoesAte(Datas.hoje().minusDays(1));
        assertStatus(HttpStatus.CONFLICT, () -> pacienteService.inscrever(pedido(true, true), pacienteUsuario));

        oportunidade.setInscricoesAte(null);
        oportunidade.setInscricoesDe(Datas.hoje().plusDays(1));
        assertStatus(HttpStatus.CONFLICT, () -> pacienteService.inscrever(pedido(true, true), pacienteUsuario));

        oportunidade.setInscricoesDe(Datas.hoje());
        oportunidade.setInscricoesAte(Datas.hoje());
        assertEquals(StatusInscricao.EM_ANALISE, pacienteService.inscrever(pedido(true, true), pacienteUsuario).status());
    }

    @Test
    void indiceUnicoViraConflitoQuandoDuasSolicitacoesChegamJuntas() {
        when(inscricaoRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("uk_inscricoes_aberta_por_paciente"));

        assertStatus(HttpStatus.CONFLICT, () -> pacienteService.inscrever(pedido(true, true), pacienteUsuario));
    }

    @Test
    void aprovarOcupaVagaECombinaPadrao() {
        Inscricao inscricao = inscricao(StatusInscricao.EM_ANALISE);

        InscricaoResponse resposta = profissionalService.decidir(inscricao.getId(),
                new DecisaoRequest(StatusInscricao.APROVADA, null, null), profissionalUsuario);

        verify(oportunidadeRepository).ocuparVaga(oportunidade.getId());
        assertEquals("A combinar", resposta.combinado().quando());
        assertEquals(true, resposta.podeTrocarMensagens());
    }

    @Test
    void regrasDaDecisao() {
        Inscricao aberta = inscricao(StatusInscricao.EM_ANALISE);
        assertStatus(HttpStatus.BAD_REQUEST, () -> profissionalService.decidir(aberta.getId(),
                new DecisaoRequest(StatusInscricao.RECUSADA, " ", null), profissionalUsuario));

        when(oportunidadeRepository.ocuparVaga(oportunidade.getId())).thenReturn(0);
        assertStatus(HttpStatus.CONFLICT, () -> profissionalService.decidir(aberta.getId(),
                new DecisaoRequest(StatusInscricao.APROVADA, null, null), profissionalUsuario));

        Inscricao recusada = inscricao(StatusInscricao.RECUSADA);
        assertStatus(HttpStatus.CONFLICT, () -> profissionalService.decidir(recusada.getId(),
                new DecisaoRequest(StatusInscricao.APROVADA, null, null), profissionalUsuario));
        assertStatus(HttpStatus.CONFLICT, () -> pacienteService.cancelar(recusada.getId(), pacienteUsuario));
    }

    @Test
    void pedidoDeInformacaoTemPrazoDeTresDiasEARespostaVoltaParaAnalise() {
        Inscricao inscricao = inscricao(StatusInscricao.EM_ANALISE);

        InscricaoResponse pedido = profissionalService.pedirInformacao(inscricao.getId(), "Envie o raio X", profissionalUsuario);
        assertEquals(StatusInscricao.AGUARDANDO_INFORMACOES, pedido.status());
        assertEquals(3, ChronoUnit.DAYS.between(Instant.now(), pedido.pedidoInformacao().prazo()) + 1);

        InscricaoResponse resposta = pacienteService.responderPedido(inscricao.getId(),
                new com.bcndeigth.carebridgeapi.inscricao.dto.RespostaPedidoRequest("Segue em anexo", null), pacienteUsuario);
        assertEquals(StatusInscricao.EM_ANALISE, resposta.status());
        assertEquals("Envie o raio X", resposta.respostasTriagem().getLast().pergunta());
        assertEquals("pedido-informacao-1", resposta.respostasTriagem().getLast().perguntaId());

        profissionalService.pedirInformacao(inscricao.getId(), "Envie o laudo", profissionalUsuario);
        InscricaoResponse segunda = pacienteService.responderPedido(inscricao.getId(),
                new com.bcndeigth.carebridgeapi.inscricao.dto.RespostaPedidoRequest("Segue o laudo", null), pacienteUsuario);
        assertEquals("pedido-informacao-2", segunda.respostasTriagem().getLast().perguntaId());
    }

    @Test
    void outrosNaoEnxergamASolicitacao() {
        Inscricao inscricao = inscricao(StatusInscricao.EM_ANALISE);
        Usuario outro = usuario("outro@clinica.com", TipoPerfil.PROFISSIONAL_SAUDE);

        assertStatus(HttpStatus.NOT_FOUND, () -> profissionalService.detalhar(inscricao.getId(), outro));
        assertStatus(HttpStatus.NOT_FOUND, () -> pacienteService.detalhar(inscricao.getId(), outro));
    }

    @Test
    void mensagemSoDepoisDaAprovacao() {
        Inscricao inscricao = inscricao(StatusInscricao.EM_ANALISE);

        assertStatus(HttpStatus.CONFLICT, () -> pacienteService.enviarMensagem(inscricao.getId(), "Olá", pacienteUsuario));
    }

    private Inscricao inscricao(StatusInscricao status) {
        Inscricao inscricao = new Inscricao();
        inscricao.setId(UUID.randomUUID());
        inscricao.setProtocolo("20002");
        inscricao.setStatus(status);
        inscricao.setOportunidade(oportunidade);
        inscricao.setPaciente(pacienteRepository.findById(pacienteUsuario.getId()).orElseThrow());
        inscricao.setDataInscricao(Instant.now());
        inscricao.setPrazoResposta(Instant.now());
        when(inscricaoRepository.findById(inscricao.getId())).thenReturn(Optional.of(inscricao));
        return inscricao;
    }

    private NovaInscricaoRequest pedido(boolean comDocumento, boolean comConsentimento) {
        return new NovaInscricaoRequest(oportunidade.getId(), ValidadorTriagemTest.respostas("Não", "Não"),
                comDocumento ? List.of(new NovaInscricaoRequest.DocumentoEnviado("rg", "RG.jpg", 1_200_000L)) : List.of(),
                comConsentimento ? Instant.now() : null);
    }

    private static Usuario usuario(String email, TipoPerfil perfil) {
        Usuario usuario = new Usuario(email, "hash", perfil, Instant.now());
        usuario.setId(UUID.randomUUID());
        return usuario;
    }

    private static void assertStatus(HttpStatus esperado, Runnable acao) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, acao::run);
        assertEquals(esperado, ex.getStatusCode(), ex.getReason());
    }
}
