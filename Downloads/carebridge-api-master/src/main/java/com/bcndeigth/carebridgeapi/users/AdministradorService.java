package com.bcndeigth.carebridgeapi.users;

import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaude;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaudeRepository;
import com.bcndeigth.carebridgeapi.users.dto.ProfissionalPendenteResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class AdministradorService {
    private final ProfissionalSaudeRepository profissionalSaudeRepository;

    public AdministradorService(ProfissionalSaudeRepository profissionalSaudeRepository) {
        this.profissionalSaudeRepository = profissionalSaudeRepository;
    }

    @Transactional(readOnly = true)
    public List<ProfissionalPendenteResponse> listarProfissionaisPendentes() {
        return profissionalSaudeRepository.findByValidadoInstitucionalmenteFalse()
                .stream()
                .map(ProfissionalPendenteResponse::from)
                .toList();
    }

    @Transactional
    public ProfissionalPendenteResponse aprovarProfissional(UUID profissionalId) {
        ProfissionalSaude profissional = profissionalSaudeRepository.findById(profissionalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Profissional de saúde não encontrado."));

        profissional.setValidadoInstitucionalmente(true);
        return ProfissionalPendenteResponse.from(profissionalSaudeRepository.save(profissional));
    }
}