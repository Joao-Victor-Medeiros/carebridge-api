package com.bcndeigth.carebridgeapi.profissional_saude;

import com.bcndeigth.carebridgeapi.users.dto.MeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ProfissionalAdminService {

    private final ProfissionalSaudeRepository repository;

    public ProfissionalAdminService(ProfissionalSaudeRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<MeResponse> pendentes() {
        return repository.findBySituacaoRegistroOrderByNomeAsc(SituacaoRegistro.EM_VERIFICACAO)
                .stream().map(MeResponse::de).toList();
    }

    @Transactional
    public MeResponse alterarSituacao(UUID id, SituacaoRegistro situacao) {
        ProfissionalSaude profissional = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profissional não encontrado."));
        if (profissional.getSituacaoRegistro() != SituacaoRegistro.EM_VERIFICACAO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O registro deste profissional já foi avaliado.");
        }
        profissional.setSituacaoRegistro(situacao);
        return MeResponse.de(profissional);
    }
}
