package com.bcndeigth.carebridgeapi.demo;

import com.bcndeigth.carebridgeapi.inscricao.DocumentoInscricao;
import com.bcndeigth.carebridgeapi.inscricao.Inscricao;
import com.bcndeigth.carebridgeapi.inscricao.InscricaoRepository;
import com.bcndeigth.carebridgeapi.inscricao.Mensagem;
import com.bcndeigth.carebridgeapi.inscricao.MensagemRepository;
import com.bcndeigth.carebridgeapi.oportunidade.FormularioTriagem;
import com.bcndeigth.carebridgeapi.oportunidade.Oportunidade;
import com.bcndeigth.carebridgeapi.oportunidade.OportunidadeRepository;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.ModoTriagem;
import com.bcndeigth.carebridgeapi.paciente.Paciente;
import com.bcndeigth.carebridgeapi.paciente.PacienteRepository;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaude;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaudeRepository;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import com.bcndeigth.carebridgeapi.users.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Só roda com o profile "demo" e DEMO_PASSWORD definida, e só se o banco ainda
 * não tem as contas de demonstração. Todas as contas usam a mesma senha.
 */
@Component
@Profile("demo")
public class DemoSeeder implements ApplicationRunner {

    static final String CONTA_SENTINELA = "paciente@teste.com";
    private static final Logger log = LoggerFactory.getLogger(DemoSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalSaudeRepository profissionalRepository;
    private final OportunidadeRepository oportunidadeRepository;
    private final InscricaoRepository inscricaoRepository;
    private final MensagemRepository mensagemRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbc;
    private final String senha;

    public DemoSeeder(UsuarioRepository usuarioRepository, PacienteRepository pacienteRepository,
                      ProfissionalSaudeRepository profissionalRepository, OportunidadeRepository oportunidadeRepository,
                      InscricaoRepository inscricaoRepository, MensagemRepository mensagemRepository,
                      PasswordEncoder passwordEncoder, JdbcTemplate jdbc,
                      @Value("${app.demo.password:}") String senha) {
        this.usuarioRepository = usuarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.oportunidadeRepository = oportunidadeRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.mensagemRepository = mensagemRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbc = jdbc;
        this.senha = senha;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
        if (senha == null || senha.isBlank() || usuarioRepository.existsByEmailIgnoreCase(CONTA_SENTINELA)) {
            return;
        }
        DemoSeed seed;
        try (InputStream json = new ClassPathResource("seed/demo.json").getInputStream()) {
            seed = JsonMapper.builder().build().readValue(json, DemoSeed.class);
        }
        Deslocamento tempo = new Deslocamento(seed.referencia());
        String hash = passwordEncoder.encode(senha);

        Map<String, ProfissionalSaude> profissionais = new HashMap<>();
        seed.profissionais().forEach(p -> profissionais.put(p.email(), profissional(p, hash)));
        Map<String, Paciente> pacientes = new HashMap<>();
        seed.pacientes().forEach(p -> pacientes.put(p.email(), paciente(p, hash)));
        Map<String, Oportunidade> oportunidades = new HashMap<>();
        seed.oportunidades().forEach(o -> oportunidades.put(o.chave(), oportunidade(o, profissionais, tempo)));
        seed.inscricoes().forEach(i -> inscricao(i, pacientes, oportunidades, tempo));

        log.info("Demonstração carregada: {} profissionais, {} pacientes, {} oportunidades, {} solicitações",
                profissionais.size(), pacientes.size(), oportunidades.size(), seed.inscricoes().size());
    }

    private ProfissionalSaude profissional(DemoSeed.Profissional p, String hash) {
        Usuario usuario = usuarioRepository.save(new Usuario(p.email(), hash, TipoPerfil.PROFISSIONAL_SAUDE, Instant.now()));
        ProfissionalSaude profissional = new ProfissionalSaude(p.nome(), p.conselho(), p.uf(), p.numeroRegistro(),
                p.especialidade(), p.ondeAtende(), List.of(), null, usuario);
        profissional.setSituacaoRegistro(p.situacaoRegistro());
        return profissionalRepository.save(profissional);
    }

    private Paciente paciente(DemoSeed.Paciente p, String hash) {
        Usuario usuario = usuarioRepository.save(new Usuario(p.email(), hash, TipoPerfil.PACIENTE, Instant.now()));
        return pacienteRepository.save(new Paciente(p.nome(), p.dataNascimento(), p.telefone(), null, usuario));
    }

    private Oportunidade oportunidade(DemoSeed.Oportunidade d, Map<String, ProfissionalSaude> profissionais,
                                      Deslocamento tempo) {
        Oportunidade o = new Oportunidade();
        o.setOfertante(profissionais.get(d.ofertante()));
        o.setTitulo(d.titulo());
        o.setDescricao(d.descricao());
        o.setTipoServico(d.tipoServico());
        o.setEspecialidade(d.especialidade());
        o.setModalidade(d.modalidade());
        o.setModalidadeAcesso(d.modalidadeAcesso());
        o.setPreco(d.preco());
        o.setPublicoAlvo(d.publicoAlvo() == null ? List.of() : d.publicoAlvo());
        o.setPublicoAlvoObservacao(d.publicoAlvoObservacao());
        o.setVagasTotais(d.vagasTotais());
        o.setVagasPreenchidas(d.vagasPreenchidas());
        o.setCep(d.cep());
        o.setEndereco(d.endereco());
        o.setCidade(d.cidade());
        o.setUf(d.uf());
        o.setInscricoesDe(tempo.data(d.inscricoesDe()));
        o.setInscricoesAte(tempo.data(d.inscricoesAte()));
        o.setCriteriosElegibilidade(d.criteriosElegibilidade());
        o.setSemRestricoes(d.semRestricoes());
        o.setFormaContato(d.formaContato());
        o.setPrazoRespostaDias(d.prazoRespostaDias() == null ? Oportunidade.PRAZO_RESPOSTA_PADRAO : d.prazoRespostaDias());
        o.setStatus(d.status());
        if (d.triagem() != null) {
            FormularioTriagem triagem = new FormularioTriagem();
            triagem.setModo(d.triagem().modo());
            triagem.setPerguntas(d.triagem().modo() == ModoTriagem.PERSONALIZADO ? d.triagem().perguntas() : List.of());
            triagem.setDocumentosExigidos(d.triagem().documentosExigidos() == null ? List.of() : d.triagem().documentosExigidos());
            o.definirTriagem(triagem);
        }
        Oportunidade salva = oportunidadeRepository.saveAndFlush(o);
        jdbc.update("update oportunidades set ultima_atualizacao = ? where id = ?",
                Timestamp.from(tempo.instante(d.ultimaAtualizacao())), salva.getId());
        return salva;
    }

    private void inscricao(DemoSeed.Inscricao d, Map<String, Paciente> pacientes, Map<String, Oportunidade> oportunidades,
                           Deslocamento tempo) {
        Inscricao i = new Inscricao();
        i.setProtocolo(String.valueOf(inscricaoRepository.proximoProtocolo()));
        i.setPaciente(pacientes.get(d.paciente()));
        i.setOportunidade(oportunidades.get(d.oportunidade()));
        i.setStatus(d.status());
        i.setRespostasTriagem(d.respostasTriagem());
        i.setConsentimentoDadosSaudeEm(tempo.instante(d.consentimentoDadosSaudeEm()));
        i.setDataInscricao(tempo.instante(d.dataInscricao()));
        i.setPrazoResposta(tempo.instante(d.prazoResposta()));
        i.setDecididaEm(tempo.instante(d.decididaEm()));
        i.setMotivoDecisao(d.motivoDecisao());
        if (d.pedidoInformacao() != null) {
            i.setPedidoInformacaoTexto(d.pedidoInformacao().texto());
            i.setPedidoInformacaoPrazo(tempo.instante(d.pedidoInformacao().prazo()));
        }
        if (d.combinado() != null) {
            i.setCombinadoQuando(d.combinado().quando());
            i.setCombinadoOnde(d.combinado().onde());
            i.setCombinadoLevar(d.combinado().levar());
        }
        d.documentos().forEach(doc -> i.adicionarDocumento(new DocumentoInscricao(doc.documentoId(), doc.nomeArquivo(),
                doc.tamanhoBytes(), tempo.instante(doc.enviadoEm()))));
        Inscricao salva = inscricaoRepository.save(i);
        d.mensagens().forEach(m -> {
            Mensagem mensagem = new Mensagem(salva, m.autor(), m.texto(), tempo.instante(m.enviadaEm()));
            mensagem.setLida(m.lida());
            mensagemRepository.save(mensagem);
        });
    }

    private record Deslocamento(Duration diferenca) {
        Deslocamento(Instant referencia) {
            this(Duration.between(referencia, Instant.now().truncatedTo(ChronoUnit.SECONDS)));
        }

        Instant instante(Instant original) {
            return original == null ? null : original.plus(diferenca).truncatedTo(ChronoUnit.SECONDS);
        }

        LocalDate data(LocalDate original) {
            return original == null ? null : original.plusDays(diferenca.toDays());
        }
    }
}
