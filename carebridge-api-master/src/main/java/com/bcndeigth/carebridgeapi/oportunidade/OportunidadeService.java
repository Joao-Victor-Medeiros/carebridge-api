package com.bcndeigth.carebridgeapi.oportunidade;

import com.bcndeigth.carebridgeapi.oportunidade.dto.OportunidadeRequest;
import com.bcndeigth.carebridgeapi.oportunidade.dto.OportunidadeResponse;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaude;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaudeRepository;
import com.bcndeigth.carebridgeapi.users.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class OportunidadeService {
    private final OportunidadeRepository oportunidadeRepository;
    private final ProfissionalSaudeRepository profissionalSaudeRepository;

    public OportunidadeService(OportunidadeRepository oportunidadeRepository,
                               ProfissionalSaudeRepository profissionalSaudeRepository) {
        this.oportunidadeRepository = oportunidadeRepository;
        this.profissionalSaudeRepository = profissionalSaudeRepository;
    }

    @Transactional
    public OportunidadeResponse criar(OportunidadeRequest request, Usuario usuario) {
        ProfissionalSaude ofertante = profissional(usuario);
        Oportunidade oportunidade = new Oportunidade();
        oportunidade.setOfertante(ofertante);
        preencher(oportunidade, request);
        return OportunidadeResponse.from(oportunidadeRepository.save(oportunidade));
    }

    @Transactional(readOnly = true)
    public List<OportunidadeResponse> listar() {
        return oportunidadeRepository.findByStatusAtivaTrueOrderByUltimaAtualizacaoDesc()
                .stream().map(OportunidadeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public OportunidadeResponse buscar(UUID id) {
        return OportunidadeResponse.from(encontrar(id));
    }

    @Transactional
    public OportunidadeResponse atualizar(UUID id, OportunidadeRequest request, Usuario usuario) {
        Oportunidade oportunidade = encontrarDoUsuario(id, usuario);
        preencher(oportunidade, request);
        return OportunidadeResponse.from(oportunidadeRepository.save(oportunidade));
    }

    @Transactional
    public void excluir(UUID id, Usuario usuario) {
        Oportunidade oportunidade = encontrarDoUsuario(id, usuario);
        if (!oportunidade.getInscricoes().isEmpty() || oportunidade.getFormularioTriagem() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível excluir uma oportunidade que possui dados relacionados.");
        }
        oportunidadeRepository.delete(oportunidade);
    }

    private ProfissionalSaude profissional(Usuario usuario) {
        return profissionalSaudeRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Profissional de saúde não encontrado."));
    }

    private Oportunidade encontrar(UUID id) {
        return oportunidadeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Oportunidade não encontrada."));
    }

    private Oportunidade encontrarDoUsuario(UUID id, Usuario usuario) {
        Oportunidade oportunidade = encontrar(id);
        if (!oportunidade.getOfertante().getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A oportunidade não pertence ao usuário.");
        }
        return oportunidade;
    }

    private void preencher(Oportunidade oportunidade, OportunidadeRequest request) {
        if (request.titulo() == null || request.titulo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O título é obrigatório.");
        }
        oportunidade.setTitulo(request.titulo());
        oportunidade.setDescricao(request.descricao());
        oportunidade.setTipoServico(request.tipoServico());
        oportunidade.setPublicoAlvo(request.publicoAlvo());
        oportunidade.setModalidade(request.modalidade());
        oportunidade.setCondicoesAcesso(request.condicoesAcesso());
        oportunidade.setStatusAtiva(request.statusAtiva());
    }
}