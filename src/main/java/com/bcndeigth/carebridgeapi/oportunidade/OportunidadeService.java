package com.bcndeigth.carebridgeapi.oportunidade;

import com.bcndeigth.carebridgeapi.oportunidade.dto.OportunidadeRequest;
import com.bcndeigth.carebridgeapi.oportunidade.dto.OportunidadeResponse;
import com.bcndeigth.carebridgeapi.oportunidade.dto.TriagemRequest;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.DocumentoExigido;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.ModoTriagem;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.Pergunta;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.TipoPergunta;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaude;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaudeRepository;
import com.bcndeigth.carebridgeapi.profissional_saude.SituacaoRegistro;
import com.bcndeigth.carebridgeapi.shared.PaginaResponse;
import com.bcndeigth.carebridgeapi.users.Usuario;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class OportunidadeService {

    static final String NAO_VERIFICADO =
            "Seu cadastro ainda está em verificação. Salve como rascunho; publicar fica liberado depois da verificação.";
    private static final int TAMANHO_PAGINA_MAXIMO = 50;
    private static final Set<String> SIM_NAO = Set.of("Sim", "Não");
    private static final Set<Modalidade> PRESENCIAIS = Set.of(Modalidade.PRESENCIAL, Modalidade.HIBRIDO);

    private final OportunidadeRepository oportunidadeRepository;
    private final ProfissionalSaudeRepository profissionalSaudeRepository;

    public OportunidadeService(OportunidadeRepository oportunidadeRepository,
                               ProfissionalSaudeRepository profissionalSaudeRepository) {
        this.oportunidadeRepository = oportunidadeRepository;
        this.profissionalSaudeRepository = profissionalSaudeRepository;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<OportunidadeResponse> buscar(BuscaOportunidades busca, int pagina, int tamanho) {
        var pageable = PageRequest.of(Math.max(pagina, 0), Math.clamp(tamanho, 1, TAMANHO_PAGINA_MAXIMO),
                Sort.by(Sort.Direction.DESC, "ultimaAtualizacao"));
        return PaginaResponse.de(oportunidadeRepository.findAll(busca.ativas(), pageable), OportunidadeResponse::publica);
    }

    @Transactional(readOnly = true)
    public OportunidadeResponse detalhar(UUID id, Usuario usuario) {
        Oportunidade oportunidade = encontrar(id);
        boolean dono = usuario != null && oportunidade.pertenceA(usuario.getId());
        if (oportunidade.getStatus() == StatusOportunidade.RASCUNHO && !dono) {
            throw naoEncontrada();
        }
        return dono ? OportunidadeResponse.doDono(oportunidade) : OportunidadeResponse.publica(oportunidade);
    }

    @Transactional(readOnly = true)
    public List<OportunidadeResponse> minhas(Usuario usuario) {
        return oportunidadeRepository.findByOfertanteIdOrderByUltimaAtualizacaoDesc(usuario.getId())
                .stream().map(OportunidadeResponse::doDono).toList();
    }

    @Transactional
    public OportunidadeResponse criar(OportunidadeRequest request, Usuario usuario) {
        exigirCorpo(request);
        ProfissionalSaude ofertante = profissional(usuario);
        StatusOportunidade status = request.status() == StatusOportunidade.RASCUNHO
                ? StatusOportunidade.RASCUNHO : StatusOportunidade.ATIVA;
        if (status == StatusOportunidade.ATIVA) {
            exigirVerificado(ofertante);
        }
        validar(request, status);

        Oportunidade oportunidade = new Oportunidade();
        oportunidade.setOfertante(ofertante);
        preencher(oportunidade, request, status);
        return OportunidadeResponse.doDono(oportunidadeRepository.save(oportunidade));
    }

    @Transactional
    public OportunidadeResponse atualizar(UUID id, OportunidadeRequest request, Usuario usuario) {
        exigirCorpo(request);
        Oportunidade oportunidade = encontrarDoDono(id, usuario);
        if (oportunidade.getStatus() == StatusOportunidade.ENCERRADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Oportunidade encerrada não pode ser editada.");
        }
        boolean continuaRascunho = oportunidade.getStatus() == StatusOportunidade.RASCUNHO
                && request.status() == StatusOportunidade.RASCUNHO;
        StatusOportunidade status = continuaRascunho ? StatusOportunidade.RASCUNHO : StatusOportunidade.ATIVA;
        if (oportunidade.getStatus() == StatusOportunidade.RASCUNHO && status == StatusOportunidade.ATIVA) {
            exigirVerificado(oportunidade.getOfertante());
        }
        validar(request, status);
        if (request.vagasTotais() != null && request.vagasTotais() < oportunidade.getVagasPreenchidas()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O número de vagas não pode ficar abaixo das vagas já preenchidas.");
        }

        preencher(oportunidade, request, status);
        return OportunidadeResponse.doDono(oportunidadeRepository.saveAndFlush(oportunidade));
    }

    @Transactional
    public void encerrar(UUID id, Usuario usuario) {
        Oportunidade oportunidade = encontrarDoDono(id, usuario);
        if (oportunidade.getStatus() != StatusOportunidade.ATIVA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Só uma oportunidade ativa pode ser encerrada.");
        }
        oportunidade.setStatus(StatusOportunidade.ENCERRADA);
    }

    @Transactional
    public void excluir(UUID id, Usuario usuario) {
        Oportunidade oportunidade = encontrarDoDono(id, usuario);
        if (oportunidade.getStatus() != StatusOportunidade.RASCUNHO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Só rascunho pode ser excluído.");
        }
        oportunidadeRepository.delete(oportunidade);
    }

    static void validar(OportunidadeRequest r, StatusOportunidade status) {
        if (vazio(r.titulo())) {
            throw invalido("Informe o título.");
        }
        validarTriagem(r.triagem());
        if (status == StatusOportunidade.RASCUNHO) {
            return;
        }
        int descricao = r.descricao() == null ? 0 : r.descricao().trim().length();
        if (descricao < 40 || descricao > 600) {
            throw invalido("A descrição precisa ter de 40 a 600 caracteres.");
        }
        if (r.modalidade() == null) {
            throw invalido("Informe a modalidade de atendimento.");
        }
        if (PRESENCIAIS.contains(r.modalidade()) && (vazio(r.cep()) || vazio(r.endereco()))) {
            throw invalido("CEP e endereço são obrigatórios no atendimento presencial ou híbrido.");
        }
        if (r.modalidadeAcesso() == null) {
            throw invalido("Informe a modalidade de acesso.");
        }
        boolean precoSocial = r.modalidadeAcesso() == ModalidadeAcesso.PRECO_SOCIAL;
        if (precoSocial && (r.preco() == null || r.preco().compareTo(BigDecimal.ZERO) <= 0)) {
            throw invalido("Informe um preço maior que zero.");
        }
        if (!precoSocial && r.preco() != null) {
            throw invalido("Só a modalidade preço social tem preço.");
        }
        if (r.vagasTotais() == null || r.vagasTotais() < 1) {
            throw invalido("O número de vagas precisa ser 1 ou mais.");
        }
        if (r.inscricoesDe() == null || r.inscricoesAte() == null) {
            throw invalido("Informe o período de inscrições.");
        }
        if (!r.inscricoesAte().isAfter(r.inscricoesDe())) {
            throw invalido("O encerramento precisa ser depois do início.");
        }
        if (!Boolean.TRUE.equals(r.semRestricoes()) && vazio(r.criteriosElegibilidade())) {
            throw invalido("Informe os critérios de elegibilidade ou marque sem restrições.");
        }
        if (r.prazoRespostaDias() != null && r.prazoRespostaDias() < 1) {
            throw invalido("O prazo de resposta precisa ser de 1 dia ou mais.");
        }
    }

    private static void validarTriagem(TriagemRequest triagem) {
        if (triagem == null || triagem.modo() != ModoTriagem.PERSONALIZADO) {
            return;
        }
        List<Pergunta> perguntas = triagem.perguntas() == null ? List.of() : triagem.perguntas();
        if (perguntas.isEmpty()) {
            throw invalido("A triagem personalizada precisa de ao menos uma pergunta.");
        }
        Map<String, Pergunta> anteriores = new HashMap<>();
        for (Pergunta pergunta : perguntas) {
            if (vazio(pergunta.id()) || vazio(pergunta.rotulo()) || pergunta.tipo() == null) {
                throw invalido("Cada pergunta precisa de id, texto e tipo.");
            }
            if (anteriores.containsKey(pergunta.id())) {
                throw invalido("Há perguntas com o mesmo id.");
            }
            if (pergunta.tipo() == TipoPergunta.MULTIPLA && (pergunta.opcoes() == null || pergunta.opcoes().size() < 2)) {
                throw invalido("Pergunta de múltipla escolha precisa de duas opções ou mais.");
            }
            validarDependencia(pergunta, anteriores);
            anteriores.put(pergunta.id(), pergunta);
        }
    }

    private static void validarDependencia(Pergunta pergunta, Map<String, Pergunta> anteriores) {
        Pergunta.Dependencia dependencia = pergunta.dependeDe();
        if (dependencia == null) {
            return;
        }
        Pergunta origem = anteriores.get(dependencia.perguntaId());
        if (origem == null) {
            throw invalido("\"" + pergunta.rotulo() + "\" depende de uma pergunta que precisa vir antes dela.");
        }
        if (vazio(dependencia.resposta())) {
            throw invalido("Informe qual resposta de \"" + origem.rotulo() + "\" mostra \"" + pergunta.rotulo() + "\".");
        }
        boolean respostaPossivel = switch (origem.tipo()) {
            case SIM_NAO -> SIM_NAO.contains(dependencia.resposta());
            case MULTIPLA -> origem.opcoes().contains(dependencia.resposta());
            case TEXTO_CURTO, TEXTO_LONGO -> false;
        };
        if (!respostaPossivel) {
            throw invalido("\"" + pergunta.rotulo() + "\" depende de uma resposta que \"" + origem.rotulo() + "\" não oferece.");
        }
    }

    private void preencher(Oportunidade o, OportunidadeRequest r, StatusOportunidade status) {
        boolean presencial = r.modalidade() == null || PRESENCIAIS.contains(r.modalidade());
        o.setTitulo(r.titulo().trim());
        o.setDescricao(texto(r.descricao()));
        o.setTipoServico(r.tipoServico());
        o.setEspecialidade(texto(r.especialidade()));
        o.setModalidade(r.modalidade());
        o.setModalidadeAcesso(r.modalidadeAcesso());
        o.setPreco(r.preco() != null && r.preco().signum() > 0 ? r.preco() : null);
        o.setPublicoAlvo(r.publicoAlvo() == null ? List.of() : r.publicoAlvo());
        o.setPublicoAlvoObservacao(texto(r.publicoAlvoObservacao()));
        o.setVagasTotais(r.vagasTotais());
        o.setCep(presencial ? texto(r.cep()) : null);
        o.setEndereco(presencial ? texto(r.endereco()) : null);
        o.setCidade(texto(r.cidade()));
        o.setUf(r.uf() == null ? null : texto(r.uf().toUpperCase()));
        o.setInscricoesDe(r.inscricoesDe());
        o.setInscricoesAte(r.inscricoesAte());
        o.setCriteriosElegibilidade(texto(r.criteriosElegibilidade()));
        o.setSemRestricoes(Boolean.TRUE.equals(r.semRestricoes()));
        o.setFormaContato(r.formaContato());
        o.setPrazoRespostaDias(r.prazoRespostaDias() == null ? Oportunidade.PRAZO_RESPOSTA_PADRAO : r.prazoRespostaDias());
        o.setStatus(status);
        if (r.triagem() != null) {
            o.definirTriagem(triagem(r.triagem(), o.getFormularioTriagem()));
        }
    }

    private FormularioTriagem triagem(TriagemRequest request, FormularioTriagem atual) {
        FormularioTriagem formulario = atual == null ? new FormularioTriagem() : atual;
        ModoTriagem modo = request.modo() == null ? ModoTriagem.PADRAO : request.modo();
        List<DocumentoExigido> documentos = request.documentosExigidos() == null ? List.of() : request.documentosExigidos();
        formulario.setModo(modo);
        formulario.setPerguntas(modo == ModoTriagem.PERSONALIZADO ? request.perguntas() : List.of());
        formulario.setDocumentosExigidos(documentos);
        return formulario;
    }

    private ProfissionalSaude profissional(Usuario usuario) {
        return profissionalSaudeRepository.findById(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Perfil de profissional de saúde não encontrado."));
    }

    private Oportunidade encontrar(UUID id) {
        return oportunidadeRepository.findById(id).orElseThrow(OportunidadeService::naoEncontrada);
    }

    private Oportunidade encontrarDoDono(UUID id, Usuario usuario) {
        Oportunidade oportunidade = encontrar(id);
        if (!oportunidade.pertenceA(usuario.getId())) {
            throw naoEncontrada();
        }
        return oportunidade;
    }

    private static void exigirVerificado(ProfissionalSaude profissional) {
        if (profissional.getSituacaoRegistro() != SituacaoRegistro.VERIFICADO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, NAO_VERIFICADO);
        }
    }

    private static void exigirCorpo(OportunidadeRequest request) {
        if (request == null) {
            throw invalido("Corpo inválido.");
        }
    }

    private static ResponseStatusException naoEncontrada() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Oportunidade não encontrada.");
    }

    private static ResponseStatusException invalido(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }

    private static boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static String texto(String valor) {
        return vazio(valor) ? null : valor.trim();
    }
}
