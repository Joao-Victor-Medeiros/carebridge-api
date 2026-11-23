package com.bcndeigth.carebridgeapi.users;

import com.bcndeigth.carebridgeapi.paciente.PacienteRepository;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaude;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaudeRepository;
import com.bcndeigth.carebridgeapi.profissional_saude.SituacaoRegistro;
import com.bcndeigth.carebridgeapi.users.dto.MeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {
    private final PacienteRepository pacienteRepository;
    private final ProfissionalSaudeRepository profissionalSaudeRepository;

    public UsuarioService(PacienteRepository pacienteRepository,
                          ProfissionalSaudeRepository profissionalSaudeRepository) {
        this.pacienteRepository = pacienteRepository;
        this.profissionalSaudeRepository = profissionalSaudeRepository;
    }

    @Transactional(readOnly = true)
    public MeResponse obterPerfil(Usuario usuario) {
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão expirada ou credenciais inválidas.");
        }
        return switch (usuario.getTipoPerfil()) {
            case PACIENTE -> pacienteRepository.findById(usuario.getId())
                    .map(paciente -> MeResponse.de(usuario, paciente))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Perfil de paciente não encontrado."));
            case PROFISSIONAL_SAUDE -> MeResponse.de(profissional(usuario));
            case ADMIN -> MeResponse.de(usuario);
        };
    }

    @Transactional(readOnly = true)
    public boolean validadoInstitucionalmente(Usuario usuario) {
        return usuario.getTipoPerfil() == TipoPerfil.PROFISSIONAL_SAUDE
                && profissional(usuario).getSituacaoRegistro() == SituacaoRegistro.VERIFICADO;
    }

    private ProfissionalSaude profissional(Usuario usuario) {
        return profissionalSaudeRepository.findById(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Perfil de profissional de saúde não encontrado."));
    }
}
