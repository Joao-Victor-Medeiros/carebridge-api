package com.bcndeigth.carebridgeapi.oportunidade;

import com.bcndeigth.carebridgeapi.oportunidade.dto.OportunidadeRequest;
import com.bcndeigth.carebridgeapi.oportunidade.dto.OportunidadeResponse;
import com.bcndeigth.carebridgeapi.oportunidade.dto.TriagemRequest;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.DocumentoExigido;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.ModoTriagem;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.Pergunta;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.TipoPergunta;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.TriagemPadrao;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaude;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaudeRepository;
import com.bcndeigth.carebridgeapi.profissional_saude.SituacaoRegistro;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OportunidadeServiceTest {

    @Mock
    private OportunidadeRepository oportunidadeRepository;

    @Mock
    private ProfissionalSaudeRepository profissionalSaudeRepository;

    private OportunidadeService service;
    private Usuario usuario;
    private ProfissionalSaude profissional;

    @BeforeEach
    void setUp() {
        service = new OportunidadeService(oportunidadeRepository, profissionalSaudeRepository);
        usuario = new Usuario("mariana@clinica.com", "hash", TipoPerfil.PROFISSIONAL_SAUDE, Instant.now());
        usuario.setId(UUID.randomUUID());
        profissional = new ProfissionalSaude("Mariana Costa", "CRM", "DF", "123456", "cardiologia",
                "Instituto Movida", List.of(), null, usuario);
        profissional.setId(usuario.getId());
        profissional.setSituacaoRegistro(SituacaoRegistro.VERIFICADO);
    }

    @Test
    void profissionalEmVerificacaoNaoPublica() {
        profissional.setSituacaoRegistro(SituacaoRegistro.EM_VERIFICACAO);
        when(profissionalSaudeRepository.findById(usuario.getId())).thenReturn(Optional.of(profissional));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.criar(publicavel(r -> r), usuario));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(oportunidadeRepository, never()).save(any());
    }

    @Test
    void profissionalEmVerificacaoSalvaRascunhoIncompleto() {
        profissional.setSituacaoRegistro(SituacaoRegistro.EM_VERIFICACAO);
        when(profissionalSaudeRepository.findById(usuario.getId())).thenReturn(Optional.of(profissional));
        when(oportunidadeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OportunidadeResponse resposta = service.criar(rascunho("Fisioterapia"), usuario);

        assertEquals(StatusOportunidade.RASCUNHO, resposta.status());
        assertEquals(0, resposta.totalInscricoes());
    }

    @Test
    void publicarComTudoPreenchidoUsaATriagemPadrao() {
        when(profissionalSaudeRepository.findById(usuario.getId())).thenReturn(Optional.of(profissional));
        when(oportunidadeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OportunidadeResponse resposta = service.criar(publicavel(r -> r), usuario);

        assertEquals(StatusOportunidade.ATIVA, resposta.status());
        assertEquals(TriagemPadrao.PERGUNTAS, resposta.triagem().perguntas());
        assertEquals("rg", resposta.triagem().documentosExigidos().getFirst().id());
        assertEquals(true, resposta.ofertante().verificado());
    }

    @Test
    void publicarValidaAsRegrasDoContrato() {
        assertInvalido(publicavel(r -> com(r, "descricao", "curta")), "40 a 600");
        assertInvalido(publicavel(r -> com(r, "cep", null)), "CEP e endereço");
        assertInvalido(publicavel(r -> com(r, "preco", null)), "preço maior que zero");
        assertInvalido(publicavel(r -> com(r, "modalidadeAcesso", ModalidadeAcesso.GRATUITO)), "Só a modalidade preço social");
        assertInvalido(publicavel(r -> com(r, "vagasTotais", 0)), "vagas");
        assertInvalido(publicavel(r -> com(r, "inscricoesAte", LocalDate.of(2026, 11, 1))), "depois do início");
        assertInvalido(publicavel(r -> com(r, "criteriosElegibilidade", " ")), "critérios");
    }

    @Test
    void triagemPersonalizadaPrecisaDePerguntasValidas() {
        Pergunta multiplaSemOpcoes = new Pergunta("p1", "Qual?", TipoPergunta.MULTIPLA, true, List.of("Só uma"),
                null, null, null, null, null);
        assertInvalido(publicavel(r -> com(r, "triagem", new TriagemRequest(ModoTriagem.PERSONALIZADO, List.of(), List.of()))),
                "ao menos uma pergunta");
        assertInvalido(publicavel(r -> com(r, "triagem",
                new TriagemRequest(ModoTriagem.PERSONALIZADO, List.of(multiplaSemOpcoes), List.of()))), "duas opções");
    }

    @Test
    void dependenciaDaTriagemPrecisaApontarParaPerguntaAnteriorEResposta() {
        Pergunta escola = new Pergunta("escola", "Frequenta a escola?", TipoPergunta.SIM_NAO, true, null, null, null, null, null, null);
        Pergunta semResposta = condicional("serie", "escola", null);
        Pergunta paraFrente = condicional("serie", "turno", "Sim");
        Pergunta respostaImpossivel = condicional("serie", "escola", "Talvez");

        assertInvalido(publicavel(r -> com(r, "triagem", personalizada(escola, semResposta))), "Informe qual resposta");
        assertInvalido(publicavel(r -> com(r, "triagem", personalizada(paraFrente, escola))), "precisa vir antes");
        assertInvalido(publicavel(r -> com(r, "triagem", personalizada(escola, respostaImpossivel))), "não oferece");
        OportunidadeService.validar(publicavel(r -> com(r, "triagem", personalizada(escola, condicional("serie", "escola", "Sim")))),
                StatusOportunidade.ATIVA);
    }

    @Test
    void editarSemTriagemMantemAQueExiste() {
        Oportunidade ativa = oportunidade(StatusOportunidade.ATIVA);
        FormularioTriagem triagem = new FormularioTriagem();
        triagem.setDocumentosExigidos(List.of(new DocumentoExigido("rg", "RG")));
        ativa.definirTriagem(triagem);
        when(oportunidadeRepository.findById(ativa.getId())).thenReturn(Optional.of(ativa));
        when(oportunidadeRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        OportunidadeResponse resposta = service.atualizar(ativa.getId(), publicavel(r -> com(r, "triagem", null)), usuario);

        assertEquals("rg", resposta.triagem().documentosExigidos().getFirst().id());
    }

    @Test
    void naoEncontraOportunidadeDeOutroProfissional() {
        Oportunidade alheia = oportunidade(StatusOportunidade.ATIVA);
        alheia.getOfertante().setId(UUID.randomUUID());
        when(oportunidadeRepository.findById(alheia.getId())).thenReturn(Optional.of(alheia));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.encerrar(alheia.getId(), usuario));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void rascunhoSoApareceParaODono() {
        Oportunidade rascunho = oportunidade(StatusOportunidade.RASCUNHO);
        when(oportunidadeRepository.findById(rascunho.getId())).thenReturn(Optional.of(rascunho));

        assertThrows(ResponseStatusException.class, () -> service.detalhar(rascunho.getId(), null));
        assertEquals(StatusOportunidade.RASCUNHO, service.detalhar(rascunho.getId(), usuario).status());
    }

    @Test
    void buscaPublicaNaoMostraTotalDeInscricoes() {
        Oportunidade ativa = oportunidade(StatusOportunidade.ATIVA);
        when(oportunidadeRepository.findById(ativa.getId())).thenReturn(Optional.of(ativa));

        assertNull(service.detalhar(ativa.getId(), null).totalInscricoes());
    }

    @Test
    void transicoesDeEstado() {
        Oportunidade ativa = oportunidade(StatusOportunidade.ATIVA);
        Oportunidade encerrada = oportunidade(StatusOportunidade.ENCERRADA);
        when(oportunidadeRepository.findById(ativa.getId())).thenReturn(Optional.of(ativa));
        when(oportunidadeRepository.findById(encerrada.getId())).thenReturn(Optional.of(encerrada));

        assertConflito(() -> service.excluir(ativa.getId(), usuario));
        assertConflito(() -> service.encerrar(encerrada.getId(), usuario));
        assertConflito(() -> service.atualizar(encerrada.getId(), publicavel(r -> r), usuario));

        service.encerrar(ativa.getId(), usuario);
        assertEquals(StatusOportunidade.ENCERRADA, ativa.getStatus());
    }

    @Test
    void edicaoNaoBaixaVagasAbaixoDasPreenchidas() {
        Oportunidade ativa = oportunidade(StatusOportunidade.ATIVA);
        ativa.setVagasPreenchidas(4);
        when(oportunidadeRepository.findById(ativa.getId())).thenReturn(Optional.of(ativa));

        assertConflito(() -> service.atualizar(ativa.getId(), publicavel(r -> com(r, "vagasTotais", 3)), usuario));
    }

    @Test
    void publicadaNaoVoltaARascunho() {
        Oportunidade ativa = oportunidade(StatusOportunidade.ATIVA);
        when(oportunidadeRepository.findById(ativa.getId())).thenReturn(Optional.of(ativa));
        when(oportunidadeRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        OportunidadeResponse resposta = service.atualizar(ativa.getId(),
                publicavel(r -> com(r, "status", StatusOportunidade.RASCUNHO)), usuario);

        assertEquals(StatusOportunidade.ATIVA, resposta.status());
    }

    private static TriagemRequest personalizada(Pergunta... perguntas) {
        return new TriagemRequest(ModoTriagem.PERSONALIZADO, List.of(perguntas), List.of());
    }

    private static Pergunta condicional(String id, String origem, String resposta) {
        return new Pergunta(id, "Qual a série?", TipoPergunta.TEXTO_CURTO, true, null, null, null, null, null,
                new Pergunta.Dependencia(origem, resposta));
    }

    private void assertInvalido(OportunidadeRequest request, String trecho) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> OportunidadeService.validar(request, StatusOportunidade.ATIVA));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals(true, ex.getReason().contains(trecho), ex.getReason());
    }

    private void assertConflito(Runnable acao) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, acao::run);
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    private Oportunidade oportunidade(StatusOportunidade status) {
        Oportunidade oportunidade = new Oportunidade();
        oportunidade.setId(UUID.randomUUID());
        oportunidade.setOfertante(profissional);
        oportunidade.setTitulo("Cardiologia, vaga social");
        oportunidade.setStatus(status);
        oportunidade.setVagasTotais(10);
        return oportunidade;
    }

    private OportunidadeRequest rascunho(String titulo) {
        return new OportunidadeRequest(titulo, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, StatusOportunidade.RASCUNHO, null);
    }

    private OportunidadeRequest publicavel(UnaryOperator<OportunidadeRequest> ajuste) {
        return ajuste.apply(new OportunidadeRequest(
                "Cardiologia, vaga social",
                "Avaliação cardiológica com ECG, voltada a pacientes sem convênio. Requer triagem prévia.",
                TipoServico.CONSULTA, "cardiologia", Modalidade.HIBRIDO, ModalidadeAcesso.PRECO_SOCIAL,
                new BigDecimal("40.00"), List.of(PublicoAlvo.ADULTOS), null, 10, "70000-000",
                "Rua das Flores, 120", "Brasília", "DF", LocalDate.of(2026, 11, 20), LocalDate.of(2026, 12, 15),
                "Adultos sem convênio, moradores do DF.", false, FormaContato.PLATAFORMA, 15,
                StatusOportunidade.ATIVA,
                new TriagemRequest(ModoTriagem.PADRAO, null, List.of(new DocumentoExigido("rg", "RG")))
        ));
    }

    private static OportunidadeRequest com(OportunidadeRequest r, String campo, Object valor) {
        return new OportunidadeRequest(
                r.titulo(),
                campo.equals("descricao") ? (String) valor : r.descricao(),
                r.tipoServico(), r.especialidade(), r.modalidade(),
                campo.equals("modalidadeAcesso") ? (ModalidadeAcesso) valor : r.modalidadeAcesso(),
                campo.equals("preco") ? (BigDecimal) valor : r.preco(),
                r.publicoAlvo(), r.publicoAlvoObservacao(),
                campo.equals("vagasTotais") ? (Integer) valor : r.vagasTotais(),
                campo.equals("cep") ? (String) valor : r.cep(),
                r.endereco(), r.cidade(), r.uf(), r.inscricoesDe(),
                campo.equals("inscricoesAte") ? (LocalDate) valor : r.inscricoesAte(),
                campo.equals("criteriosElegibilidade") ? (String) valor : r.criteriosElegibilidade(),
                r.semRestricoes(), r.formaContato(), r.prazoRespostaDias(),
                campo.equals("status") ? (StatusOportunidade) valor : r.status(),
                campo.equals("triagem") ? (TriagemRequest) valor : r.triagem()
        );
    }
}
